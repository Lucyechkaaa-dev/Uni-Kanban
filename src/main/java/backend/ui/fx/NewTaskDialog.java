package backend.ui.fx;

import backend.models.task.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class NewTaskDialog extends Dialog<Task> {

    public NewTaskDialog(String columnTitle) {
        setTitle("New Task");
        setHeaderText("Add task to: " + columnTitle);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Task Title");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(3);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);

        getDialogPane().setContent(grid);

        // Validation: disable create button when title is empty
        Button createBtn = (Button) getDialogPane().lookupButton(createButtonType);
        createBtn.setDisable(true);
        titleField.textProperty().addListener((obs, oldVal, newVal) ->
                createBtn.setDisable(newVal == null || newVal.trim().isEmpty())
        );

        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String title = titleField.getText().trim();
                String desc = descArea.getText().trim();
                Task task = new Task(title);
                if (!desc.isEmpty()) {
                    task.setDescription(desc);
                }
                return task;
            }
            return null;
        });
    }

    public static Optional<Task> openDialog(String columnTitle) {
        return new NewTaskDialog(columnTitle).showAndWait();
    }
}
