package com.loafer.core.service;

import cn.hutool.core.exceptions.ValidateException;
import com.loafer.core.model.UserInfo;
import com.loafer.core.model.Users;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author loafer
 * @since 2023-07-16
 */
public interface IUsersService extends IService<Users> {

    /**
     * 用户注册
     * @param user
     * @return
     */
    long register(Users user) throws ValidateException;

    /**
     * 用户登录
     * @param username
     * @param password
     */
    void login(String username, String password);

    /**
     * 获取登录用户
     * @param tokenId
     * @return
     */
    UserInfo getLoginUser(String tokenId);

    /**
     * 注销登录
     * @param tokenId
     */
    Boolean logout(String tokenId);

    /**
     * 用户信息修改
     * @param user
     * @param tokenId
     * @return
     */
    Boolean modify(Users user, String tokenId);

    /**
     * 删除账号
     * @param username
     * @param password
     * @param tokenId
     * @return
     */
    Boolean delete(String username, String password, String tokenId);
}
