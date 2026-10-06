package backend.services;

import backend.dao.UserDao;
import backend.models.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "UserDao must not be null");
    }

    public UserServiceImpl() {
        this(new UserDao());
    }

    @Override
    public User register(String username, String password) {
        log.info("Attempting to register user with username: {}", username);
        validateCredentials(username, password);

        String trimmedUsername = username.trim();
        if (existsByUsername(trimmedUsername)) {
            log.warn("Registration rejected: username '{}' is already taken", trimmedUsername);
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
        }

        User user = new User(trimmedUsername, password);
        userDao.save(user);
        log.info("User registered successfully with id: {}", user.getId());
        return user;
    }

    @Override
    public User create(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        log.info("Attempting to create user: {}", user.getUsername());
        validateCredentials(user.getUsername(), user.getPassword());

        String trimmedUsername = user.getUsername().trim();
        user.setUsername(trimmedUsername);

        if (existsByUsername(trimmedUsername)) {
            log.warn("Creation rejected: username '{}' is already taken", trimmedUsername);
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
        }

        userDao.save(user);
        log.info("User created successfully with id: {}", user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userDao.findById(id));
    }

    @Override
    public void getById(UUID id) {
        findById(id).orElseThrow(() -> {
            log.warn("User with id {} not found", id);
            return new NoSuchElementException("User not found with id: " + id);
        });
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userDao.findByUsername(username.trim()));
    }

    @Override
    public List<User> searchByUsername(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return userDao.searchByUsername(query.trim());
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public void updateUser(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null when updating");
        }
        validateCredentials(user.getUsername(), user.getPassword());

        String trimmedUsername = user.getUsername().trim();
        user.setUsername(trimmedUsername);

        getById(user.getId());

        Optional<User> existing = findByUsername(trimmedUsername);
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            log.warn("Update rejected: username '{}' is already taken by another user", trimmedUsername);
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
        }

        userDao.update(user);
        log.info("User updated successfully: id={}", user.getId());
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) {
            return false;
        }
        log.info("Attempting to delete user by id: {}", id);
        return userDao.deleteById(id);
    }

    @Override
    public void delete(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        log.info("Attempting to delete user entity: id={}", user.getId());
        userDao.delete(user);
    }

    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return userDao.findById(id) != null;
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userDao.findByUsername(username.trim()) != null;
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }
    }

}
