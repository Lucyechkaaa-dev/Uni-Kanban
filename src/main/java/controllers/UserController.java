package controllers;

import backend.security.AuthInterceptor;
import backend.services.UserService;
import controllers.ApiDtos.UserSearchDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** The user directory used by the sharing dialog. Returns only id and username. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping("/search")
    public List<UserSearchDto> search(@RequestParam(name = "q", required = false, defaultValue = "") String q,
                                      @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        return users.searchUsers(q, userId).stream()
                .map(u -> new UserSearchDto(u.getId(), u.getUsername()))
                .toList();
    }
}
