package com.loafer.core.interceptor;

import com.loafer.core.common.ErrorCode;
import com.loafer.core.common.exception.BusinessException;
import com.loafer.core.model.UserInfo;
import com.loafer.core.service.IUsersService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource(name = "usersServiceImpl")
    private IUsersService iUsersService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod method = (HandlerMethod) handler;
        String[] allowableControllers = {
          "springfox.documentation.swagger.web.ApiResourceController"
        };
        if (Arrays.asList(allowableControllers).contains(method.getBean().getClass().getName())) {
            return true;
        }
        String tokenId = request.getHeader("tokenId");
        iUsersService.getLoginUser(tokenId);
        return true;
    }
}
