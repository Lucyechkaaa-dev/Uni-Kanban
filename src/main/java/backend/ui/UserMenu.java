package backend.ui;

import backend.models.user.User;
import backend.services.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

@Component
public class UserMenu {
    private final UserService userService;
    private final Scanner sc = new Scanner(System.in);

    public UserMenu(UserService userService) {
        this.userService = userService;
    }

    public void initUserMenu() {
        while (true) {
            System.out.println("""
                    
                    --- USER MENU ---
                    1. Search Methods
                    2. CRUD Methods
                    3. Back to Main Menu
                    """);
            System.out.print("Select option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine().trim();
                    System.out.print("Enter email: ");
                    String email = sc.nextLine().trim();
                    System.out.print("Enter password (at least 8 characters): ");
                    String password = sc.nextLine();
                    try {
                        User created = userService.register(username, email, password);
                        System.out.println("User created successfully! ID: " + created.getId());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "2" -> {
                    UUID id = readUuid("Enter user ID to update: ");
                    if (id == null) break;

                    Optional<User> optionalUser = userService.findById(id);
                    if (optionalUser.isEmpty()) {
                        System.out.println("User not found with id: " + id);
                        break;
                    }

                    User user = optionalUser.get();
                    System.out.print("Enter new username (leave blank to keep '" + user.getUsername() + "'): ");
                    String newUsername = sc.nextLine().trim();
                    System.out.print("Enter new email (leave blank to keep '" + (user.getEmail() == null ? "none" : user.getEmail()) + "'): ");
                    String newEmail = sc.nextLine().trim();
                    try {
                        userService.updateProfile(id, newUsername, newEmail);
                        System.out.println("Profile updated successfully!");
                    } catch (Exception e) {
                        System.out.println("Error updating user: " + e.getMessage());
                        break;
                    }

                    System.out.print("To change the password enter the current one (leave blank to skip): ");
                    String oldPassword = sc.nextLine();
                    if (!oldPassword.isEmpty()) {
                        System.out.print("Enter new password: ");
                        String newPassword = sc.nextLine();
                        try {
                            userService.changePassword(id, oldPassword, newPassword);
                            System.out.println("Password changed successfully!");
                        } catch (Exception e) {
                            System.out.println("Error changing password: " + e.getMessage());
                        }
                    }
                }
                case "3" -> { return; }
                default -> System.out.println("Invalid input");
            }
        }
    }

    private void searchUserMenu() {
        while (true) {
            System.out.println("""
                    
                    --- SEARCH USER MENU ---
                    1. Find all Users
                    2. Find user by ID
                    3. Find user by username
                    4. Search users by substring
                    5. Back
                    """);
            System.out.print("Select option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    List<User> users = userService.findAll();
                    if (users.isEmpty()) {
                        System.out.println("No users found.");
                    } else {
                        System.out.println("Users (" + users.size() + "):");
                        users.forEach(u -> System.out.println(" - " + u.getId() + " | " + u.getUsername()));
                    }
                }
                case "2" -> {
                    UUID id = readUuid("Enter user ID: ");
                    if (id == null) break;
                    Optional<User> user = userService.findById(id);
                    user.ifPresentOrElse(
                            u -> System.out.println("Found: " + u.getId() + " | " + u.getUsername()),
                            () -> System.out.println("User not found.")
                    );
                }
                case "3" -> {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine().trim();
                    Optional<User> user = userService.findByUsername(username);
                    user.ifPresentOrElse(
                            u -> System.out.println("Found: " + u.getId() + " | " + u.getUsername()),
                            () -> System.out.println("User not found.")
                    );
                }
                case "4" -> {
                    System.out.print("Enter query substring: ");
                    String query = sc.nextLine().trim();
                    List<User> users = userService.searchByUsername(query);
                    if (users.isEmpty()) {
                        System.out.println("No users matched query.");
                    } else {
                        users.forEach(u -> System.out.println(" - " + u.getId() + " | " + u.getUsername()));
                    }
                }
                case "5" -> { return; }
                default -> System.out.println("Invalid input");
            }
        }
    }

    private void crudUserMenu() {
        while (true) {
            System.out.println("""
                    
                    --- CRUD USER MENU ---
                    1. Create / Register User
                    2. Update User by ID
                    3. Delete User by ID
                    4. Back
                    """);
            System.out.print("Select option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Enter username: ");
                    String username = sc.nextLine().trim();
                    System.out.print("Enter email: ");
                    String email = sc.nextLine().trim();
                    System.out.print("Enter password (at least 8 characters): ");
                    String password = sc.nextLine();
                    try {
                        User created = userService.register(username, email, password);
                        System.out.println("User created successfully! ID: " + created.getId());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case "2" -> {
                    UUID id = readUuid("Enter user ID to update: ");
                    if (id == null) break;

                    Optional<User> optionalUser = userService.findById(id);
                    if (optionalUser.isEmpty()) {
                        System.out.println("User not found with id: " + id);
                        break;
                    }

                    User user = optionalUser.get();
                    System.out.print("Enter new username (leave blank to keep '" + user.getUsername() + "'): ");
                    String newUsername = sc.nextLine().trim();
                    System.out.print("Enter new email (leave blank to keep '" + (user.getEmail() == null ? "none" : user.getEmail()) + "'): ");
                    String newEmail = sc.nextLine().trim();
                    try {
                        userService.updateProfile(id, newUsername, newEmail);
                        System.out.println("Profile updated successfully!");
                    } catch (Exception e) {
                        System.out.println("Error updating user: " + e.getMessage());
                        break;
                    }

                    System.out.print("To change the password enter the current one (leave blank to skip): ");
                    String oldPassword = sc.nextLine();
                    if (!oldPassword.isEmpty()) {
                        System.out.print("Enter new password: ");
                        String newPassword = sc.nextLine();
                        try {
                            userService.changePassword(id, oldPassword, newPassword);
                            System.out.println("Password changed successfully!");
                        } catch (Exception e) {
                            System.out.println("Error changing password: " + e.getMessage());
                        }
                    }
                }
                case "3" -> {
                    UUID id = readUuid("Enter user ID to delete: ");
                    if (id == null) break;
                    try {
                        boolean deleted = userService.deleteById(id);
                        if (deleted) {
                            System.out.println("User deleted successfully.");
                        } else {
                            System.out.println("User not found with id: " + id);
                        }
                    } catch (Exception e) {
                        System.out.println("Error deleting user: " + e.getMessage());
                    }
                }
                case "4" -> { return; }
                default -> System.out.println("Invalid input");
            }
        }
    }

    private UUID readUuid(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format: " + input);
            return null;
        }
    }
}
