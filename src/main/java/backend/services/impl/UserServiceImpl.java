package backend.services.impl;

import backend.dao.UserDao;
import backend.models.task.Board;
import backend.models.user.User;
import backend.services.InvalidCredentialsException;
import backend.services.UserService;
import backend.utils.Passwords;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Log4j2
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService{

    static final String DEFAULT_BOARD_NAME = "My board";
    static final String INVALID_LOGIN = "Invalid email or password";
    private static final int MIN_PASSWORD_LENGTH = 8;
    static final int MIN_SEARCH_LENGTH = 2;
    static final int MAX_SEARCH_RESULTS = 10;
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserDao userDao;

    @Override
    public User register(String username, String email, String password) {
        log.info("Attempting to register user with username: {}", username);
        validateUsername(username);
        String normalizedEmail = normalizeEmail(email);
        validatePassword(password);

        String trimmedUsername = username.trim();
        if (existsByUsername(trimmedUsername)) {
            log.warn("Registration rejected: username '{}' is already taken", trimmedUsername);
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
        }
        if (existsByEmail(normalizedEmail)) {
            log.warn("Registration rejected: e-mail is already registered");
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(trimmedUsername, Passwords.hash(password));
        user.setEmail(normalizedEmail);

        // the first board comes with the account; User -> Board -> TaskList are cascaded, so one save writes all of it
        Board board = new Board(DEFAULT_BOARD_NAME, user);
        board.addDefaultColumns();
        user.addTaskTable(board);

        userDao.save(user);
        log.info("User registered successfully with id: {}", user.getId());
        return user;
    }

    @Override
    public User authenticate(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        User user = normalizedEmail.isEmpty() ? null : userDao.findByEmail(normalizedEmail);

        // the hash is always checked (against a dummy one if there is no such user): same answer and same time either way
        boolean passwordOk = Passwords.matches(password, user != null ? user.getPassword() : null);
        if (user == null || !passwordOk) {
            log.warn("Login failed");
            throw new InvalidCredentialsException(INVALID_LOGIN);
        }
        log.info("User logged in: id={}", user.getId());
        return user;
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (!Passwords.matches(oldPassword, user.getPassword())) {
            log.warn("Password change rejected for user id={}: wrong current password", userId);
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        validatePassword(newPassword);

        user.setPassword(Passwords.hash(newPassword));
        userDao.update(user);
        log.info("Password changed for user id={}", userId);
    }

    @Override
    public User updateProfile(UUID userId, String username, String email) {
        User user = getById(userId);

        if (username != null && !username.isBlank()) {
            String trimmedUsername = username.trim();
            User sameName = userDao.findByUsername(trimmedUsername);
            if (sameName != null && !sameName.getId().equals(userId)) {
                log.warn("Update rejected: username '{}' is already taken by another user", trimmedUsername);
                throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already taken");
            }
            user.setUsername(trimmedUsername);
        }

        if (email != null && !email.isBlank()) {
            String normalizedEmail = normalizeEmail(email);
            User sameEmail = userDao.findByEmail(normalizedEmail);
            if (sameEmail != null && !sameEmail.getId().equals(userId)) {
                log.warn("Update rejected: e-mail is already registered by another user");
                throw new IllegalArgumentException("Email is already registered");
            }
            user.setEmail(normalizedEmail);
        }

        userDao.update(user);
        log.info("Profile updated: id={}", userId);
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
    public User getById(UUID id) {
        return findById(id).orElseThrow(() -> {
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
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userDao.findByEmail(email.trim().toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<User> searchByUsername(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return userDao.searchByUsername(query.trim());
    }

    @Override
    public List<User> searchUsers(String query, UUID excludeId) {
        String text = query != null ? query.trim() : "";
        if (text.length() < MIN_SEARCH_LENGTH) {
            throw new IllegalArgumentException("Type at least " + MIN_SEARCH_LENGTH + " characters to search");
        }
        return userDao.searchUsers(text, excludeId, MAX_SEARCH_RESULTS);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
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

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userDao.findByEmail(email.trim().toLowerCase(Locale.ROOT)) != null;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Email is not valid");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        if (Passwords.tooLong(password)) {
            throw new IllegalArgumentException("Password must be at most " + Passwords.MAX_BYTES + " bytes long");
        }
    }

}