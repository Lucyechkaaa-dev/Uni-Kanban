package backend.ui;

import lombok.NoArgsConstructor;

import java.util.Scanner;

@NoArgsConstructor
public class ConsoleMenu {
    private final static Scanner sc = new Scanner(System.in);
    private final static UserMenu userMenu = new UserMenu();

    public static void init() {
        while (true) {
            System.out.println("""
                    
                    --- CONSOLE MENU ---
                    1. User Menu
                    2. Exit
                    """);
            System.out.print("Select option: ");
            String input = sc.nextLine().trim();

            switch (input) {
                case "1" -> userMenu.initUserMenu();
                case "2" -> {
                    System.out.println("Exiting application...");
                    return;
                }
                default -> System.out.println("Invalid input, please try again.");
            }
        }
    }
}
