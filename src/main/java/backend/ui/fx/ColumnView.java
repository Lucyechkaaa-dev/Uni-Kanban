package backend.ui.fx;

import backend.models.task.Task;
import backend.models.task.TaskList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ColumnView extends VBox {

    private final TaskList taskList;
    private final VBox cardsContainer;
    private final Label badgeLabel;
    private final Runnable onAddTaskRequested;
    private final BiConsumer<TaskList, Task> onMoveTaskLeft;
    private final BiConsumer<TaskList, Task> onMoveTaskRight;
    private final BiConsumer<TaskList, Task> onDeleteTask;
    private final Consumer<TaskList> onDeleteColumn;
    private final boolean hasLeftColumn;
    private final boolean hasRightColumn;

    public ColumnView(TaskList taskList,
                      boolean hasLeftColumn,
                      boolean hasRightColumn,
                      Runnable onAddTaskRequested,
                      BiConsumer<TaskList, Task> onMoveTaskLeft,
                      BiConsumer<TaskList, Task> onMoveTaskRight,
                      BiConsumer<TaskList, Task> onDeleteTask,
                      Consumer<TaskList> onDeleteColumn) {
        this.taskList = taskList;
        this.hasLeftColumn = hasLeftColumn;
        this.hasRightColumn = hasRightColumn;
        this.onAddTaskRequested = onAddTaskRequested;
        this.onMoveTaskLeft = onMoveTaskLeft;
        this.onMoveTaskRight = onMoveTaskRight;
        this.onDeleteTask = onDeleteTask;
        this.onDeleteColumn = onDeleteColumn;

        getStyleClass().add("kanban-column");

        // Header
        HBox header = new HBox();
        header.getStyleClass().add("column-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(taskList.getName());
        titleLabel.getStyleClass().add("column-title");

        badgeLabel = new Label(String.valueOf(taskList.getTasks().size()));
        badgeLabel.getStyleClass().add("column-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+");
        addBtn.getStyleClass().add("small-button");
        addBtn.setTooltip(new javafx.scene.control.Tooltip("Add task to column"));
        addBtn.setOnAction(e -> onAddTaskRequested.run());

        Button deleteColBtn = new Button("✕");
        deleteColBtn.getStyleClass().add("delete-button");
        deleteColBtn.setTooltip(new javafx.scene.control.Tooltip("Delete column"));
        deleteColBtn.setOnAction(e -> onDeleteColumn.accept(taskList));

        header.getChildren().addAll(titleLabel, badgeLabel, spacer, addBtn, deleteColBtn);

        // Cards container inside a ScrollPane
        cardsContainer = new VBox();
        cardsContainer.getStyleClass().add("cards-container");

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, scrollPane);

        renderTasks();
    }

    public void renderTasks() {
        cardsContainer.getChildren().clear();
        badgeLabel.setText(String.valueOf(taskList.getTasks().size()));

        for (Task task : taskList.getTasks()) {
            Runnable moveLeft = hasLeftColumn ? () -> onMoveTaskLeft.accept(taskList, task) : null;
            Runnable moveRight = hasRightColumn ? () -> onMoveTaskRight.accept(taskList, task) : null;

            TaskCardView card = new TaskCardView(
                    task,
                    moveLeft,
                    moveRight,
                    t -> onDeleteTask.accept(taskList, t)
            );
            cardsContainer.getChildren().add(card);
        }
    }

    public TaskList getTaskList() {
        return taskList;
    }
}
