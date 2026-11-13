package backend.security;

import backend.services.InvalidCredentialsException;
import backend.services.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Runs before every controller under /api (see WebConfig). Takes the login token from the cookie,
 * asks the sessions table whose it is and passes the user id on to the controller as a request attribute.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID = "userId";

    private final SessionService sessions;

    public AuthInterceptor(SessionService sessions) {
        this.sessions = sessions;
    }

    /** The token from the request cookies, or null. AuthController uses it too (logout). */
    public static String tokenOf(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (SessionService.COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // the browser's own pre-flight questions carry no cookie
        }
        UUID userId = sessions.userIdOf(tokenOf(request))
                .orElseThrow(() -> new InvalidCredentialsException("Authentication required"));
        request.setAttribute(USER_ID, userId);
        return true;
    }
}
