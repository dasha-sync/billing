package api.service;

import api.model.User;
import jakarta.servlet.http.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

// todo: изменить на сессии
@Service
@AllArgsConstructor
public class SessionService {
    private static final int COOKIE_MAX_AGE = 259200; // 3 days in seconds

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, "jwt", null, 0);
        addCookie(response, "username", null, 0);
        addCookie(response, "email", null, 0);
    }

    public void addCookie(HttpServletResponse response, String name, String value, int expiration) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Consider true for HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(expiration);
        response.addCookie(cookie);
    }

    public void setAuthCookies(HttpServletResponse response, String jwt, User user) {
        addCookie(response, "jwt", jwt, COOKIE_MAX_AGE);
        addCookie(response, "username", user.getUsername(), COOKIE_MAX_AGE);
        addCookie(response, "email", user.getEmail(), COOKIE_MAX_AGE);
    }

    public Map<String, String> extractCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Collections.emptyMap();
        }

        return Arrays.stream(cookies)
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (a, b) -> b));
    }
}
