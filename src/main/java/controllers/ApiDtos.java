package controllers;

import backend.models.task.Board;
import backend.models.task.Task;
import backend.models.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * What travels over HTTP. Entities never leave the backend: User holds the password hash,
 * and Board / TaskList / Task point at each other through lazy relations that are closed by then.
 */
public final class ApiDtos {

    private ApiDtos() {
    }

    // ==================== requests ====================

    public record RegisterRequest(String username, String email, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    /** title is required, the rest may be null. priority is LOW, MEDIUM or HIGH. */
    public record TaskRequest(String title, String description, String priority, String tag,
                              LocalDate dueDate, UUID assigneeId) {
    }

    /** position = null means "to the end of the column". */
    public record MoveRequest(UUID columnId, Integer position) {
    }

    // ==================== responses ====================

    public record UserDto(UUID id, String username, String email) {
        public static UserDto of(User user) {
            return new UserDto(user.getId(), user.getUsername(), user.getEmail());
        }
    }

    public record BoardSummary(UUID id, String name, String description) {
        public static BoardSummary of(Board board) {
            return new BoardSummary(board.getId(), board.getName(), board.getDescription());
        }
    }

    public record TaskDto(UUID id, int number, String title, String description, String priority,
                          String tag, LocalDate dueDate, UUID columnId, UUID assigneeId, String assigneeName) {
        /** The assignee and the column must already be loaded (findById and findByBoardId fetch them). */
        public static TaskDto of(Task task) {
            User assignee = task.getUserOwner();
            return new TaskDto(
                    task.getId(),
                    task.getNumber(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority() != null ? task.getPriority().name() : null,
                    task.getTag(),
                    task.getDueDate(),
                    task.getTaskListParent() != null ? task.getTaskListParent().getId() : null,
                    assignee != null ? assignee.getId() : null,
                    assignee != null ? assignee.getUsername() : null);
        }
    }

    public record ColumnDto(UUID id, String name, String color, int position, List<TaskDto> tasks) {
    }

    /** access = NONE, VIEW, EDIT or MANAGE: what the asking user may do on this board. */
    public record BoardDto(UUID id, String name, String description, String access, List<ColumnDto> columns) {
    }

    public record ErrorResponse(String error) {
    }
}
