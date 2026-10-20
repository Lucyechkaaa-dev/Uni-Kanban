package backend.ui;

import lombok.NoArgsConstructor;

import java.util.Scanner;

@NoArgsConstructor
public class ConsoleMenu {
    private final static Scanner sc = new Scanner(System.in);
    private final static UserMenu userMenu = new UserMenu();
    private final static ImportExportMenu importExportMenu = new ImportExportMenu();

    public static void init() {
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
