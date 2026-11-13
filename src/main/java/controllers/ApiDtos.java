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
                          String tag, LocalDate dueDate, UUID columnId, int position, UUID assigneeId, String assigneeName) {
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
                    task.getPosition(),
                    assignee != null ? assignee.getId() : null,
                    assignee != null ? assignee.getUsername() : null);
        }
    }

    public record ColumnDto(UUID id, String name, String color, int position, List<TaskDto> tasks) {
    }

    /** access = NONE, VIEW, EDIT or MANAGE: what the asking user may do on this board. owner = the asking user owns it. */
    public record BoardDto(UUID id, String name, String description, String access, boolean owner, List<ColumnDto> columns) {
    }

    /** A board another user shared with the asking user; role = VIEWER, MEMBER or ADMIN. */
    public record SharedBoardDto(UUID id, String name, String description, String ownerUsername, String role) {
    }

    /** Someone who can see the board. role = OWNER, VIEWER, MEMBER or ADMIN. */
    public record MemberDto(UUID userId, String username, String role) {
    }

    /** What the sharing dialog needs about a user: nothing private (no e-mail). */
    public record UserSearchDto(UUID id, String username) {
    }

    // ==================== sharing requests ====================

    /** role = VIEWER, MEMBER or ADMIN. */
    public record ShareRequest(UUID userId, String role) {
    }

    public record RoleRequest(String role) {
    }

    public record RenameBoardRequest(String name) {
    }

    public record ErrorResponse(String error) {
    }
}
