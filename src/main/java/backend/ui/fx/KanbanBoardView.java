package backend.ui.fx;

import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.TaskTable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class KanbanBoardView extends BorderPane {

    private final TaskTable currentTable;
    private final HBox columnsContainer;
    private final Label titleLabel;

    public KanbanBoardView() {
        this(createDefaultTable());
    }

    public KanbanBoardView(TaskTable table) {
        this.currentTable = table;

        getStylesheets().add(getClass().getResource("/styles/kanban.css").toExternalForm());

        // Top bar
        HBox topBar = new HBox();
        topBar.getStyleClass().add("kanban-topbar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox();
        titleLabel = new Label(currentTable.getName() != null ? currentTable.getName() : "Uni-Kanban Board");
        titleLabel.getStyleClass().add("board-title");

        Label subtitleLabel = new Label("Visual Workflow & Task Management");
        subtitleLabel.getStyleClass().add("board-subtitle");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addColumnBtn = new Button("+ Add Column");
        addColumnBtn.getStyleClass().add("secondary-button");
        addColumnBtn.setOnAction(e -> handleAddColumn());

        Button addTaskBtn = new Button("+ Add Task");
        addTaskBtn.getStyleClass().add("primary-button");
        addTaskBtn.setOnAction(e -> handleQuickAddTask());

        topBar.getChildren().addAll(titleBox, spacer, addColumnBtn, addTaskBtn);
        setTop(topBar);

        // Columns container
        columnsContainer = new HBox();
        columnsContainer.getStyleClass().add("columns-container");

        ScrollPane scrollPane = new ScrollPane(columnsContainer);
        scrollPane.getStyleClass().add("board-scroll-pane");
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setCenter(scrollPane);

        renderBoard();
    }

    public void renderBoard() {
        columnsContainer.getChildren().clear();
        List<TaskList> lists = currentTable.getTaskLists();

        for (int i = 0; i < lists.size(); i++) {
            TaskList list = lists.get(i);
            boolean hasLeft = (i > 0);
            boolean hasRight = (i < lists.size() - 1);

            int colIndex = i;
            ColumnView colView = new ColumnView(
                    list,
                    hasLeft,
                    hasRight,
                    () -> handleAddTaskToColumn(list),
                    (col, task) -> moveTask(colIndex, colIndex - 1, task),
                    (col, task) -> moveTask(colIndex, colIndex + 1, task),
                    (col, task) -> deleteTask(col, task),
                    col -> deleteColumn(col)
            );
            columnsContainer.getChildren().add(colView);
        }
    }

    private void handleAddColumn() {
        NewColumnDialog.openDialog().ifPresent(name -> {
            TaskList newList = new TaskList(name);
            currentTable.addTaskList(newList);
            renderBoard();
        });
    }

    private void handleQuickAddTask() {
        if (currentTable.getTaskLists().isEmpty()) {
            handleAddColumn();
            return;
        }
        // Add to first column by default
        TaskList targetList = currentTable.getTaskLists().get(0);
        handleAddTaskToColumn(targetList);
    }

    private void handleAddTaskToColumn(TaskList list) {
        NewTaskDialog.openDialog(list.getName()).ifPresent(task -> {
            list.addTask(task);
            renderBoard();
        });
    }

    private void moveTask(int fromIndex, int toIndex, Task task) {
        List<TaskList> lists = currentTable.getTaskLists();
        if (fromIndex >= 0 && fromIndex < lists.size() && toIndex >= 0 && toIndex < lists.size()) {
            TaskList fromList = lists.get(fromIndex);
            TaskList toList = lists.get(toIndex);

            fromList.removeTask(task);
            toList.addTask(task);
            renderBoard();
        }
    }

    private void deleteTask(TaskList list, Task task) {
        list.removeTask(task);
        renderBoard();
    }

    private void deleteColumn(TaskList list) {
        currentTable.removeTaskList(list);
        renderBoard();
    }

    private static TaskTable createDefaultTable() {
        TaskTable table = new TaskTable("Uni-Kanban Sprint Board");

        TaskList todo = new TaskList("To Do");
        todo.addTask(new Task("Set up CI/CD pipeline", "Configure GitHub Actions workflow"));
        todo.addTask(new Task("Implement JavaFX UI", "Build modern Kanban board interface"));

        TaskList inProgress = new TaskList("In Progress");
        inProgress.addTask(new Task("Refactor Task models", "Add TaskTable and TaskList entities"));

        TaskList done = new TaskList("Done");
        done.addTask(new Task("Configure Hibernate", "Set up PostgreSQL and SessionFactory"));
        done.addTask(new Task("Spring Boot Integration", "Added web controller and endpoints"));

        table.addTaskList(todo);
        table.addTaskList(inProgress);
        table.addTaskList(done);

        return table;
    }
}
