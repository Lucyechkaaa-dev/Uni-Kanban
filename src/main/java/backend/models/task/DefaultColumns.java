package backend.models.task;

import java.util.List;

/**
 * The four columns every new board starts with: same names and colors as the frontend prototype
 * (kanban-frontend/src/components/BoardView.jsx). The order in the list is the column position.
 */
public final class DefaultColumns{

    public record ColumnSpec(String name, String color){}

    public static final List<ColumnSpec> ALL = List.of(
            new ColumnSpec("To-do", "#94a3b8"),
            new ColumnSpec("In progress", "#3b82f6"),
            new ColumnSpec("In review", "#eab308"),
            new ColumnSpec("Done", "#22c55e")
    );

    private DefaultColumns(){}
}