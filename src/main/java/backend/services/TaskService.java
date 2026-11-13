package backend.services;

import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.Board;
import backend.models.task.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TaskService {

    // ==================== Task Table Operations ====================

    Board createTable(String name, UUID userId);

    Optional<Board> findTable(UUID id);

    Board getTable(UUID id);

    List<Board> findTablesByUser(UUID userId);

    List<Board> findAllTables();

    Board renameTable(UUID tableId, String newName, UUID operatorId);

    boolean deleteTable(UUID tableId, UUID operatorId);

    // ==================== Access / Role Management Operations ====================

    Access accessLevel(UUID tableId, UUID userId);

    void assignMemberRole(UUID tableId, UUID targetUserId, String role, UUID operatorId);

    void removeMemberRole(UUID tableId, UUID targetUserId, UUID operatorId);

    String getMemberRole(UUID tableId, UUID userId);

    Map<UUID, String> getMemberRoles(UUID tableId);

    // ==================== Task List Operations ====================

    TaskList createList(String name, UUID tableId, UUID operatorId);

    Optional<TaskList> findList(UUID id);

    TaskList getList(UUID id);

    List<TaskList> findListsByTable(UUID tableId);

    TaskList renameList(UUID listId, String newName, UUID operatorId);

    boolean deleteList(UUID listId, UUID operatorId);

    // ==================== Task Operations ====================

    Task createTask(String title, String description, UUID assigneeId, UUID listId, UUID operatorId);

    /** The same, with the fields the card form has. priority, tag and dueDate may be null (MEDIUM, no tag, no date). */
    Task createTask(String title, String description, Priority priority, String tag, LocalDate dueDate,
                    UUID assigneeId, UUID listId, UUID operatorId);

    Optional<Task> findTask(UUID id);

    Task getTask(UUID id);

    List<Task> findTasksByList(UUID listId);

    /** Every card of the board with its assignee loaded, ordered by position: for the board screen. */
    List<Task> findTasksByTable(UUID tableId);

    List<Task> findTasksByUser(UUID userId);

    List<Task> searchTasks(String query);

    List<Task> findAllTasks();

    Task updateTask(UUID id, String newTitle, String newDescription, UUID operatorId);

    /** Copies title, description, priority, tag and due date onto the stored card; column and board cannot be changed here. */
    Task updateTask(Task task, UUID operatorId);

    Task assignTask(UUID taskId, UUID userId, UUID operatorId);

    Task unassignTask(UUID taskId, UUID operatorId);

    /** position -1 = to the end of the column. A card stays on its own board. */
    Task moveTask(UUID taskId, UUID targetListId, int position, UUID operatorId);

    Task reorderTask(UUID taskId, int newPosition, UUID operatorId);

    boolean deleteTask(UUID taskId, UUID operatorId);

    // ==================== Internal Helpers / Utilities ====================

    boolean isTableOwner(UUID tableId, UUID userId);
}
