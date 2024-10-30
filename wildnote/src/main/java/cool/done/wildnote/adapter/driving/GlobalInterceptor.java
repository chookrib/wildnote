package cool.done.wildnote.adapter.driving;

import cool.done.wildnote.domain.AuthService;
import cool.done.wildnote.domain.NotLoginException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 */
public class GlobalInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws NotLoginException {

        String accessToken = request.getHeader("Access-Token");

        if(!authService.verifyAccessToken(accessToken))
            throw new NotLoginException("未登录");

        return true;
    }
}
