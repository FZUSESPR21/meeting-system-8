package com.example.scoring_system.filter;

import com.example.scoring_system.bean.JWTToken;
import com.example.scoring_system.bean.User;
import com.example.scoring_system.service.UserService;
import com.example.scoring_system.utils.JwtUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

@Data
@Slf4j
public class JwtAuthFilter extends AuthenticatingFilter {

    private static final int tokenRefreshInterval = 300;
    @Autowired
    private UserService userService;

    /**
     * 父类会在请求进入拦截器后调用该方法，返回true则继续，返回false则会调用onAccessDenied()。这里在不通过时，还调用了isPermissive()方法，我们后面解释。
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if(this.isLoginRequest(request, response))
            return true;
        boolean allowed = false;
        try {
            allowed = executeLogin(request, response);
        } catch(IllegalStateException e){ //not found any token
            log.error("没有发现token");
        }catch (Exception e) {
            log.error("登录错误", e);
        }
        return allowed || super.isPermissive(mappedValue);
    }



    /**
    * @Description:  使用自定义的token类，提交给shiro.
    * @Param: [servletRequest, servletResponse]
    * @return: org.apache.shiro.authc.AuthenticationToken
    * @Date: 2021/4/30
    */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        String jwtToken= getAuthzHeader(servletRequest);
        log.info("客户端上传的token:"+jwtToken);
        if (StringUtils.isNotBlank(jwtToken)&&!JwtUtils.isTokenExpired(jwtToken))
        {
            log.info("token通过验证");
            return new JWTToken(jwtToken);
        }
        log.info("token没有通过验证");
        return null;
    }

    /**
    * @Description:isAccessAllowed返回false则进入该方法，此处返回错误的响应头。
    * @Param: [servletRequest, servletResponse]
    * @return: boolean
    * @Date: 2021/4/30
    */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletResponse httpServletResponse=WebUtils.toHttp(servletResponse);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setStatus(HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION);
        fillCorsHeader(WebUtils.toHttp(servletRequest),httpServletResponse);
        return false;
    }

    /**
    * @Description: 如果Shiro Login认证成功进入该方法，即登录成功，同时进行了token刷新判断。
    * @Param: [token, subject, request, response]
    * @return: boolean
    * @Date: 2021/4/30
    */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        String newToken = null;
        if(token instanceof JWTToken){
            JWTToken jwtToken = (JWTToken)token;
            User user = (User) subject.getPrincipal();
            boolean shouldRefresh = shouldTokenRefresh(JwtUtils.getIssuedAt(jwtToken.getToken()));
            if(shouldRefresh) {
                newToken = userService.generateJwtToken(user);
            }
        }
        if(StringUtils.isNotBlank(newToken))
            httpResponse.setHeader("x-auth-token", newToken);

        return true;
    }

    /**
    * @Description:  shiro的login认证失败，会调用这个方法，
    * @Param: [token, e, request, response]
    * @return: boolean
    * @Date: 2021/4/30
    */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        log.error("token失效:"+token,e.getMessage());
        return super.onLoginFailure(token, e, request, response);
    }

    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String header = httpRequest.getHeader("x-auth-token");
        return StringUtils.removeStart(header, "Bearer ");
    }

    protected boolean shouldTokenRefresh(Date issueAt){
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }

    /**
    * @Description: 跨域支持
    * @Param: [httpServletRequest, httpServletResponse]
    * @return: void
    * @Date: 2021/4/30
    */
    protected void fillCorsHeader(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,HEAD");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
    }
}
