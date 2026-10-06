package backend.ui.fx;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class NewColumnDialog extends Dialog<String> {

    public NewColumnDialog() {
        setTitle("New Column");
        setHeaderText("Create a new Kanban Column");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Column name (e.g. In Review)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        getDialogPane().setContent(grid);

        Button createBtn = (Button) getDialogPane().lookupButton(createButtonType);
        createBtn.setDisable(true);
        nameField.textProperty().addListener((obs, oldVal, newVal) ->
                createBtn.setDisable(newVal == null || newVal.trim().isEmpty())
        );

        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return nameField.getText().trim();
            }
            return null;
        });
    }

    public static Optional<String> openDialog() {
        return new NewColumnDialog().showAndWait();
    }
}
