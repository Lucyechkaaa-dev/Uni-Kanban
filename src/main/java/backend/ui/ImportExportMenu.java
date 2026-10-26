package backend.ui;

import backend.dao.TeamDao;
import backend.models.task.Board;
import backend.models.team.Team;
import backend.export.impl.KanbanImportExportService;
import backend.export.KanbanImportExportServiceImpl;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.Scanner;
import java.util.UUID;

@Log4j2
@NoArgsConstructor
public class ImportExportMenu {

    private final KanbanImportExportService importExportService = new KanbanImportExportServiceImpl();
    private final TeamDao teamDao = new TeamDao();
    private final Scanner sc = new Scanner(System.in);

    public void initMenu() {
        while (true) {
            System.out.println("""
                    
                    --- IMPORT / EXPORT MENU ---
                    1. Export Board to JSON
                    2. Import Board from JSON
                    3. Export Board to CSV (for Excel)
                    4. Import Board from CSV (for Excel)
                    5. Back
                    """);
            System.out.print("Select option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> exportBoardToJson();
                case "2" -> importBoardFromJson();
                case "3" -> exportBoardToCsv();
                case "4" -> importBoardFromCsv();
                case "5" -> { return; }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    private void exportBoardToJson() {
        Board table = promptForBoard();
        if (table == null) return;

        System.out.print("Enter output JSON file path (e.g. board.json): ");
        String path = sc.nextLine().trim();
        if (path.isEmpty()) path = "board.json";

        try {
            File file = new File(path);
            importExportService.exportToJson(table, file);
            System.out.println("Board exported successfully to: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error exporting to JSON: " + e.getMessage());
        }
    }

    private void importBoardFromJson() {
        System.out.print("Enter input JSON file path: ");
        String path = sc.nextLine().trim();
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File does not exist: " + path);
            return;
        }

        Team targetTeam = promptForOptionalTeam();

        try {
            Board table = importExportService.importFromJson(file, targetTeam);
            System.out.println("Board '" + table.getName() + "' successfully imported from JSON!");
            System.out.println("Columns count: " + table.getTaskLists().size());
            table.getTaskLists().forEach(col ->
                    System.out.println(" - [" + col.getName() + "] " + col.getTasks().size() + " tasks")
            );
        } catch (Exception e) {
            System.out.println("Error importing from JSON: " + e.getMessage());
        }
    }

    private void exportBoardToCsv() {
        Board table = promptForBoard();
        if (table == null) return;

        System.out.print("Enter output CSV file path (e.g. board.csv): ");
        String path = sc.nextLine().trim();
        if (path.isEmpty()) path = "board.csv";

        try {
            File file = new File(path);
            importExportService.exportToCsv(table, file);
            System.out.println("Board exported successfully to CSV (Excel compatible) at: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error exporting to CSV: " + e.getMessage());
        }
    }

    private void importBoardFromCsv() {
        System.out.print("Enter input CSV file path: ");
        String path = sc.nextLine().trim();
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File does not exist: " + path);
            return;
        }

        Team targetTeam = promptForOptionalTeam();

        try {
            Board table = importExportService.importFromCsv(file, targetTeam);
            System.out.println("Board '" + table.getName() + "' successfully imported from CSV!");
            System.out.println("Columns count: " + table.getTaskLists().size());
            table.getTaskLists().forEach(col ->
                    System.out.println(" - [" + col.getName() + "] " + col.getTasks().size() + " tasks")
            );
        } catch (Exception e) {
            System.out.println("Error importing from CSV: " + e.getMessage());
        }
    }

    private Board promptForBoard() {
        System.out.print("Enter board name to export (or leave blank to create sample board): ");
        String boardName = sc.nextLine().trim();
        if (boardName.isEmpty()) {
            boardName = "Sample Kanban Board";
        }

        Board table = new Board(boardName);
        backend.models.task.TaskList todo = new backend.models.task.TaskList("To Do");
        todo.addTask(new backend.models.task.Task("Setup Project", "Configure build and models"));
        todo.addTask(new backend.models.task.Task("Write Tests", "Comprehensive test coverage"));

        backend.models.task.TaskList inProgress = new backend.models.task.TaskList("In Progress");
        inProgress.addTask(new backend.models.task.Task("Implement Features", "Business logic"));

        backend.models.task.TaskList done = new backend.models.task.TaskList("Done");

        table.addTaskList(todo);
        table.addTaskList(inProgress);
        table.addTaskList(done);
        return table;
    }

    private Team promptForOptionalTeam() {
        System.out.print("Attach to team by ID? (enter UUID or leave empty for standalone): ");
        String teamIdStr = sc.nextLine().trim();
        if (teamIdStr.isEmpty()) return null;

        try {
            UUID teamId = UUID.fromString(teamIdStr);
            Team team = teamDao.findById(teamId);
            if (team == null) {
                System.out.println("Team not found with ID: " + teamId + ", proceeding as standalone board.");
            }
            return team;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format, proceeding as standalone board.");
            return null;
        }
    }
}
