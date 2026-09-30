package launcher;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DebugLauncher {
    public static void main(String[] args) {
        CompileLauncher.launch(true);
        System.out.println("Debug mode enabled");

        User user = new User();
        user.setUsername("Test1Login");
        user.setPassword("Test1Password");
        Task task = new Task();
        user.addTask(task);
        UserDao userDao = new UserDao();
        userDao.save(user);
    }
}
