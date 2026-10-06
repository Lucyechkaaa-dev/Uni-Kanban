package backend.ui.fx;

import backend.models.task.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class TaskCardView extends VBox {

    private final Task task;

    public TaskCardView(Task task,
                        Runnable onMoveLeft,
                        Runnable onMoveRight,
                        Consumer<Task> onDelete) {
        this.task = task;
        getStyleClass().add("task-card");

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("task-card-title");
        titleLabel.setWrapText(true);

        getChildren().add(titleLabel);

        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.getStyleClass().add("task-card-desc");
            descLabel.setWrapText(true);
            getChildren().add(descLabel);
        }

        HBox actionsBox = new HBox();
        actionsBox.getStyleClass().add("card-actions");
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        if (onMoveLeft != null) {
            Button leftBtn = new Button("◀");
            leftBtn.getStyleClass().add("small-button");
            leftBtn.setOnAction(e -> onMoveLeft.run());
            actionsBox.getChildren().add(leftBtn);
        }

        if (onMoveRight != null) {
            Button rightBtn = new Button("▶");
            rightBtn.getStyleClass().add("small-button");
            rightBtn.setOnAction(e -> onMoveRight.run());
            actionsBox.getChildren().add(rightBtn);
        }

        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> onDelete.accept(task));
        actionsBox.getChildren().add(deleteBtn);

        getChildren().add(actionsBox);
    }

    public Task getTask() {
        return task;
    }
}
