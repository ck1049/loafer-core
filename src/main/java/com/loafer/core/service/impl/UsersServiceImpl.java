package com.loafer.core.service.impl;

import cn.hutool.core.exceptions.ValidateException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.loafer.core.common.ErrorCode;
import com.loafer.core.common.Payload;
import com.loafer.core.common.exception.BusinessException;
import com.loafer.core.model.UserInfo;
import com.loafer.core.model.Users;
import com.loafer.core.mapper.UsersMapper;
import com.loafer.core.model.UsersToken;
import com.loafer.core.service.ISecretKeyService;
import com.loafer.core.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.loafer.core.service.IUsersTokenService;
import com.loafer.core.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.loafer.core.common.RedisSpecialValue.INVALID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author loafer
 * @since 2023-07-16
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    @Resource
    private ModelMapper modelMapper;

    @Resource(name = "secretKeyServiceImpl")
    private ISecretKeyService iSecretKeyService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUsersTokenService iUsersTokenService;

    /** 短期token60分钟 **/
    private final int TOKEN_EXPIRE = 60;
    /** 长期token30天 **/
    private final int TOKEN_LONG_EXPIRE = 30 * 24 * 60;

    /**  盐值，混淆密码 **/
    private static final String SALT = "loafer";

    /** 无效的tokenId最多允许查询mysql的次数 **/
    @Value("${jwt.ALLOW_INVALID_TOKEN_SEARCH_NUM}")
    private int ALLOW_INVALID_TOKEN_SEARCH_NUM;

    /** 无效的tokenId在redis中的缓存时长 **/
    @Value("${jwt.INVALID_EXPIRE}")
    private int INVALID_EXPIRE;

    @Override
    @Transactional
    public long register(Users user) throws ValidateException {
        // 1. 校验
        if (user.getPassword().length() < 5) {
            throw new ValidateException(ErrorCode.PARAMS_ERROR.getCode(), "用户密码过短");
        }
        synchronized (user.getUsername().intern()) {
            // 账户不能重复
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getUsername, user.getUsername());
            long count = baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new ValidateException(ErrorCode.PARAMS_ERROR.getCode(), "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + user.getPassword()).getBytes());
            // 3. 插入数据
            user.setPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new ValidateException(ErrorCode.SYSTEM_ERROR.getCode(), "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public void login(String username, String password) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, username)
                .eq(Users::getPassword, encodePassword(password));
        Users one = getOne(wrapper);
        Assert.state(ObjectUtils.isNotEmpty(one), "用户不存在！");

        // 生成双token
        generatorDoubleToken(modelMapper.map(one, UserInfo.class));
    }

    @Override
    public UserInfo getLoginUser(String tokenId) {
        String token = stringRedisTemplate.opsForValue().get(tokenId);

        if (StringUtils.isBlank(token)) {
            return refreshToken(tokenId);
        }

        if (INVALID.equals(token)) {
            // 无效的tokenId已经使用过两次以上，则抛出异常，不再查询mysql
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "无效的tokenId:" + token);
        }

        Payload<UserInfo> payload = null;
        try {
            payload = JwtUtils.getInfoFromToken(token, iSecretKeyService.getPublicKey(), UserInfo.class);
        } catch (ExpiredJwtException e) {
            // token过期，则refreshToken
            e.printStackTrace();
            return refreshToken(tokenId);
        }
        assert payload != null;
        return payload.getUserInfo();
    }

    @Override
    public Boolean logout(String tokenId) {
        // 设置mysql中的token过期时间
        LambdaQueryWrapper<UsersToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsersToken::getTokenId, tokenId);
        iUsersTokenService.update(new UsersToken(tokenId, new Date()), wrapper);
        // 删除redis缓存中的token
        return stringRedisTemplate.delete(tokenId);
    }

    @Override
    @Transactional
    public Boolean modify(Users user, String tokenId) {
        // 登录判断
        UserInfo userInfo = getLoginUser(tokenId);
        // 先确定用户名有没有重复
        LambdaQueryWrapper<Users> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(Users::getUsername, user.getUsername())
                .ne(Users::getId, user.getId());
        List<Users> list = list(usernameWrapper);
        if (list.size() > 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户名重复！请更换用户名。");
        }
        if (!user.getUsername().equals(userInfo.getUsername())) {
            // 如果修改了用户名，则要重新生成token
            generatorDoubleToken(modelMapper.map(user, UserInfo.class));
        }
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(encodePassword(user.getPassword()));
        }
        return updateById(user);
    }

    @Override
    public Boolean delete(String userName, String password, String tokenId) {
        // 判断用户是否登录
        UserInfo userInfo = getLoginUser(tokenId);
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, userName)
                .eq(Users::getPassword, encodePassword(password));
        Users one = getOne(wrapper);
        Assert.state(one != null, "用户名或密码不正确！");
        remove(wrapper);
        return logout(tokenId);
    }

    private String encodePassword(String password) {
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
    /**
     * 刷新token
     */
    private UserInfo refreshToken(String tokenId) {
        LambdaQueryWrapper<UsersToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsersToken::getTokenId, tokenId);
        // wrapper.gt(UsersToken::getExpireTime, new Date());
        UsersToken usersToken = iUsersTokenService.getOne(wrapper);

        // 长期token有效
        if (usersToken != null && StringUtils.isNotBlank(usersToken.getToken()) && usersToken.getExpireTime().after(new Date())) {
            // 双token中的长期token
            String newToken = usersToken.getToken();
            UserInfo userInfo = Objects.requireNonNull(JwtUtils.getInfoFromToken(newToken, iSecretKeyService.getPublicKey(), UserInfo.class)).getUserInfo();
            // 重新生成双token
            generatorDoubleToken(userInfo);
            // 将原来的长期token标记为过期
            LambdaUpdateWrapper<UsersToken> expireWrapper = new LambdaUpdateWrapper<>();
            expireWrapper.eq(UsersToken::getTokenId, tokenId);
            iUsersTokenService.update(new UsersToken(tokenId, new Date()), expireWrapper);
            return userInfo;
        }

        usersToken = usersToken == null ? new UsersToken(tokenId, 0) : usersToken;
        if (usersToken.getUsed() >= this.ALLOW_INVALID_TOKEN_SEARCH_NUM) {
            // 已经使用过 ${ALLOW_INVALID_TOKEN_SEARCH_NUM} 以上的无效tokenId，不排除遭到恶意攻击，缓存常量invalid ${INVALID_EXPIRE}分钟，防止缓存穿透
            stringRedisTemplate.opsForValue().set(tokenId, INVALID, this.INVALID_EXPIRE, TimeUnit.MINUTES);
        } else {
            // 使用次数小于2次，每次使用更新mysql user_token表的used字段
            usersToken.setUsed(usersToken.getUsed() + 1);
            // 数据库不存在这条记录则插入，存在则更新
            iUsersTokenService.saveOrUpdate(usersToken);
        }
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录！");
    }

    /**
     * 生成双token
     * @param userInfo
     */
    private void generatorDoubleToken(UserInfo userInfo) {
        // 根据用户信息和私钥生成双token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, iSecretKeyService.getPrivateKey(), TOKEN_EXPIRE);
        String tokenLongExpire = JwtUtils.generateTokenExpireInMinutes(userInfo, iSecretKeyService.getPrivateKey(), TOKEN_LONG_EXPIRE);
        // 将短期tokenMD5加密之后返回给前端，不直接返回token，提高安全性
        String tokenId = DigestUtils.md5DigestAsHex(token.getBytes());
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletResponse response = ((ServletRequestAttributes) requestAttributes).getResponse();
        // tokenId放入response.headers
        assert response != null;
        response.addHeader("tokenId", tokenId);

        // 同步代码块，防止缓存击穿
        synchronized (tokenId.intern()) {
            // 短期token存入redis，设置过期时间TOKEN_EXPIRE
            stringRedisTemplate.opsForValue().set(tokenId, token, TOKEN_EXPIRE, TimeUnit.MINUTES);
            // 长期token存入mysql，短期token过期后启用
            Payload<Object> infoFromToken = JwtUtils.getInfoFromToken(tokenLongExpire, iSecretKeyService.getPublicKey());
            iUsersTokenService.save(new UsersToken(null, tokenId, tokenLongExpire, (long) (TOKEN_LONG_EXPIRE / 60 / 24),
                    TimeUnit.DAYS.name(), infoFromToken.getCreateTime(), infoFromToken.getExpiration(), 0));
        }

    }

}
