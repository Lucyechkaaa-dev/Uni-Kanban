package backend.services;

import backend.models.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User register(String username, String email, String password);
    User authenticate(String email, String password);
    void changePassword(UUID userId, String oldPassword, String newPassword);
    User updateProfile(UUID userId, String username, String email);

    Optional<User> findById(UUID id);
    User getById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> searchByUsername(String query);
    List<User> findAll();
    boolean deleteById(UUID id);
    void delete(User user);
    boolean existsById(UUID id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
