package com.loafer.core.controller;

import com.loafer.core.dto.UserLoginDto;
import com.loafer.core.dto.UserModifyDto;
import com.loafer.core.dto.UserRegisterDto;
import com.loafer.core.model.UserInfo;
import com.loafer.core.model.Users;
import com.loafer.core.service.IUsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = "用户")
@RestController
@RequestMapping("user")
public class UsersController {

    @Resource(name = "usersServiceImpl")
    private IUsersService service;

    @Resource
    private ModelMapper modelMapper;

    @ApiOperation(value = "用户注册")
    @PostMapping("register")
    public ResponseEntity<Long> register(@Valid @RequestBody @ApiParam UserRegisterDto dto) {
        return ResponseEntity.ok(service.register(modelMapper.map(dto, Users.class)));
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("login")
    public ResponseEntity<Boolean> login(@Valid @RequestBody @ApiParam UserLoginDto dto) {
        service.login(dto.getUsername(), dto.getPassword());
        return ResponseEntity.ok(true);
    }

    @ApiOperation(value = "获取登录用户")
    @GetMapping("getLoginUser")
    public ResponseEntity<UserInfo> getLoginUser(HttpServletRequest request) {
        String tokenId = request.getHeader("tokenId");
        Assert.state(StringUtils.isNotBlank(tokenId), "请求头缺少tokenId！");
        return ResponseEntity.ok(service.getLoginUser(tokenId));
    }

    @ApiOperation(value = "注销")
    @GetMapping("logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        String tokenId = request.getHeader("tokenId");
        Assert.state(StringUtils.isNotBlank(tokenId), "请求头缺少tokenId！");
        return ResponseEntity.ok(service.logout(tokenId));
    }

    @ApiOperation(value = "修改个人信息")
    @PostMapping("modify")
    public ResponseEntity<Boolean> modify(@Valid @RequestBody @ApiParam UserModifyDto dto, HttpServletRequest request) {
        String tokenId = request.getHeader("tokenId");
        Assert.state(StringUtils.isNotBlank(tokenId), "请求头缺少tokenId！");
        return ResponseEntity.ok(service.modify(modelMapper.map(dto, Users.class), tokenId));
    }

    @ApiOperation(value = "账号删除")
    @DeleteMapping("delete")
    public ResponseEntity<Boolean> delete(@Valid @RequestBody @ApiParam UserLoginDto dto, HttpServletRequest request) {
        String tokenId = request.getHeader("tokenId");
        Assert.state(StringUtils.isNotBlank(tokenId), "请求头缺少tokenId！");
        service.delete(dto.getUsername(), dto.getPassword(), tokenId);
        return ResponseEntity.ok(true);
    }

}
