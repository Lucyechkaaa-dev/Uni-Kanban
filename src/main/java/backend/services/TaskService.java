package backend.services;

import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.Board;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TaskService {

    // ==================== Task Table Operations ====================

    Board createTable(String name, UUID userId);

    Board createTable(String name);

    Optional<Board> findTable(UUID id);

    Board getTable(UUID id);

    List<Board> findTablesByUser(UUID userId);

    List<Board> findAllTables();

    Board renameTable(UUID tableId, String newName);

    Board renameTable(UUID tableId, String newName, UUID operatorId);

    boolean deleteTable(UUID tableId);

    boolean deleteTable(UUID tableId, UUID operatorId);

    // ==================== Role Management Operations ====================

    void assignMemberRole(UUID tableId, UUID targetUserId, String role, UUID operatorId);

    void removeMemberRole(UUID tableId, UUID targetUserId, UUID operatorId);

    String getMemberRole(UUID tableId, UUID userId);

    Map<UUID, String> getMemberRoles(UUID tableId);

    void addUserRole(UUID tableId, UUID targetUserId, String role, UUID operatorId);

    void removeUserRole(UUID tableId, UUID targetUserId, String role, UUID operatorId);

    // ==================== Task List Operations ====================

    TaskList createList(String name, UUID tableId);

    TaskList createList(String name, UUID tableId, UUID operatorId);

    TaskList createList(String name);

    Optional<TaskList> findList(UUID id);

    TaskList getList(UUID id);

    List<TaskList> findListsByTable(UUID tableId);

    TaskList renameList(UUID listId, String newName);

    TaskList renameList(UUID listId, String newName, UUID operatorId);

    boolean deleteList(UUID listId);

    boolean deleteList(UUID listId, UUID operatorId);

    // ==================== Task Operations ====================

    Task createTask(String title);

    Task createTask(String title, String description);

    Task createTask(String title, String description, UUID listId);

    Task createTask(String title, String description, UUID userId, UUID listId);

    Task createTask(Task task);

    Optional<Task> findTask(UUID id);

    Task getTask(UUID id);

    List<Task> findTasksByList(UUID listId);

    List<Task> findTasksByUser(UUID userId);

    List<Task> searchTasks(String query);

    List<Task> findAllTasks();

    Task updateTask(UUID id, String newTitle, String newDescription);

    Task updateTask(UUID id, String newTitle, String newDescription, UUID operatorId);

    Task updateTask(Task task);

    Task assignTask(UUID taskId, UUID userId);

    Task assignTask(UUID taskId, UUID userId, UUID operatorId);

    Task unassignTask(UUID taskId);

    Task unassignTask(UUID taskId, UUID operatorId);

    Task moveTask(UUID taskId, UUID targetListId);

    Task moveTask(UUID taskId, UUID targetListId, int position);

    Task moveTask(UUID taskId, UUID targetListId, int position, UUID operatorId);

    Task reorderTask(UUID taskId, int newPosition);

    Task reorderTask(UUID taskId, int newPosition, UUID operatorId);

    boolean deleteTask(UUID taskId);

    boolean deleteTask(UUID taskId, UUID operatorId);

    // ==================== Internal Helpers / Utilities ====================

    boolean isTableOwner(UUID tableId, UUID userId);
}
