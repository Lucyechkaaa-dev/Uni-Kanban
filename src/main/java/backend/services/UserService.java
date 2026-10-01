package backend.services;

import backend.models.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User register(String username, String password);
    User create(User user);
    Optional<User> findById(UUID id);
    void getById(UUID id);
    Optional<User> findByUsername(String username);
    List<User> searchByUsername(String query);
    List<User> findAll();
    void updateUser(User user);
    boolean deleteById(UUID id);
    void delete(User user);
    boolean existsById(UUID id);
    boolean existsByUsername(String username);
}
