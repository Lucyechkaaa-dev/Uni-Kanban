package controllers;

import backend.models.user.User;
import backend.security.AuthInterceptor;
import backend.services.SessionService;
import backend.services.UserService;
import controllers.ApiDtos.LoginRequest;
import controllers.ApiDtos.RegisterRequest;
import controllers.ApiDtos.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

/**
 * Register, login, logout. A successful login adds a row to the auth_sessions table and the answer carries
 * "Set-Cookie: KANBAN_SESSION=<token>" (HttpOnly, SameSite=Lax). The rows live in PostgreSQL,
 * so a restart of the backend does not log anybody out.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final SessionService sessions;
    private final boolean secureCookie;

    public AuthController(UserService users, SessionService sessions,
                          @Value("${app.cookie.secure:false}") boolean secureCookie) {
        this.users = users;
        this.sessions = sessions;
        this.secureCookie = secureCookie;
    }

    /** Creates the account (and its "My board") and logs the new user in at once. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestBody RegisterRequest body,
                            HttpServletRequest request, HttpServletResponse response) {
        User user = users.register(body.username(), body.email(), body.password());
        startSession(request, response, user.getId());
        return UserDto.of(user);
    }

    @PostMapping("/login")
    public UserDto login(@RequestBody LoginRequest body,
                         HttpServletRequest request, HttpServletResponse response) {
        User user = users.authenticate(body.email(), body.password());
        startSession(request, response, user.getId());
        return UserDto.of(user);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.close(AuthInterceptor.tokenOf(request));
        setCookie(response, "", Duration.ZERO); // the browser deletes the cookie
    }

    /** Who am I: the page calls it after a reload to learn whether the browser still has a valid login. */
    @GetMapping("/me")
    public UserDto me(@RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        return UserDto.of(users.getById(userId));
    }

    /** A new token at every login; the token the browser had before (if any) stops working. */
    private void startSession(HttpServletRequest request, HttpServletResponse response, UUID userId) {
        sessions.close(AuthInterceptor.tokenOf(request));
        setCookie(response, sessions.open(userId), SessionService.LIFETIME);
    }

    private void setCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(SessionService.COOKIE_NAME, token)
                .httpOnly(true)          // scripts on the page cannot read it
                .sameSite("Lax")         // another site cannot make the browser send it with its POST
                .secure(secureCookie)    // true when the site runs on https
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
