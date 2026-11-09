package backend.ui;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class ConsoleMenu {
    private final Scanner sc = new Scanner(System.in);
    private final UserMenu userMenu;
    private final ImportExportMenu importExportMenu;

    public ConsoleMenu(UserMenu userMenu, ImportExportMenu importExportMenu) {
        this.userMenu = userMenu;
        this.importExportMenu = importExportMenu;
    }

    public void init() {
        while (true) {
            System.out.println("""
                    
                    --- CONSOLE MENU ---
                    1. User Menu
                    2. Import / Export (JSON & Excel CSV)
                    3. Exit
                    """);
            System.out.print("Select option: ");
            String input = sc.nextLine().trim();

            switch (input) {
                case "1" -> userMenu.initUserMenu();
                case "2" -> importExportMenu.initMenu();
                case "3" -> {
                    System.out.println("Exiting application...");
                    return;
                }
                default -> System.out.println("Invalid input, please try again.");
            }
        }
    }
}
