package services;

import backend.dao.TaskDao;
import backend.dao.TaskListDao;
import backend.dao.TaskTableDao;
import backend.dao.UserDao;
import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.TaskTable;
import backend.models.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TaskService {

	private final TaskDao taskDao;
	private final TaskListDao taskListDao;
	private final TaskTableDao taskTableDao;
	private final UserDao userDao;

	public TaskService() {
		this(new TaskDao(), new TaskListDao(), new TaskTableDao(), new UserDao());
	}

	public static TaskService create() {
		return new TaskService();
	}

	// ==================== Task Table Operations ====================

	public TaskTable createTable(String name, UUID userId) {
		validateNotBlank(name, "Table name cannot be empty");
		User user = userId != null ? getUserOrThrow(userId) : null;
		TaskTable table = new TaskTable(name.trim(), user);
		taskTableDao.save(table);
		log.info("Created TaskTable '{}' (id={}) for user {}", table.getName(), table.getId(), userId);
		return table;
	}

	public TaskTable createTable(String name) {
		return createTable(name, null);
	}

	public Optional<TaskTable> findTable(UUID id) {
		return id != null ? Optional.ofNullable(taskTableDao.findById(id)) : Optional.empty();
	}

	public TaskTable getTable(UUID id) {
		return findTable(id).orElseThrow(() -> new NoSuchElementException("TaskTable not found with id: " + id));
	}

	public List<TaskTable> findTablesByUser(UUID userId) {
		return userId != null ? taskTableDao.findByUserId(userId) : Collections.emptyList();
	}

	public List<TaskTable> findAllTables() {
		return taskTableDao.findAll();
	}

	public TaskTable renameTable(UUID tableId, String newName) {
		return renameTable(tableId, newName, null);
	}

	public TaskTable renameTable(UUID tableId, String newName, UUID operatorId) {
		validateNotBlank(newName, "New table name cannot be empty");
		TaskTable table = getTable(tableId);
		assertTableOwner(table, operatorId);
		table.setName(newName.trim());
		taskTableDao.update(table);
		log.info("Renamed TaskTable id={} to '{}' by operator {}", tableId, table.getName(), operatorId);
		return table;
	}

	public boolean deleteTable(UUID tableId) {
		return deleteTable(tableId, null);
	}

	public boolean deleteTable(UUID tableId, UUID operatorId) {
		assertTableOwner(tableId, operatorId);
		log.info("Deleting TaskTable id={} by operator {}", tableId, operatorId);
		return taskTableDao.deleteById(tableId);
	}

	// ==================== Role Management Operations ====================

	public void assignMemberRole(UUID tableId, UUID targetUserId, String role, UUID operatorId) {
		assertTableOwner(tableId, operatorId);
		validateNotBlank(role, "Role name cannot be empty");
		getUserOrThrow(targetUserId);

		TaskTable table = getTable(tableId);
		table.assignMemberRole(targetUserId, role);
		taskTableDao.update(table);
		log.info("Table owner {} assigned role '{}' to user {} on table {}", operatorId, role, targetUserId, tableId);
	}

	public void removeMemberRole(UUID tableId, UUID targetUserId, UUID operatorId) {
		assertTableOwner(tableId, operatorId);
		TaskTable table = getTable(tableId);
		table.removeMemberRole(targetUserId);
		taskTableDao.update(table);
		log.info("Table owner {} removed role from user {} on table {}", operatorId, targetUserId, tableId);
	}

	public String getMemberRole(UUID tableId, UUID userId) {
		return getTable(tableId).getMemberRole(userId);
	}

	public java.util.Map<UUID, String> getMemberRoles(UUID tableId) {
		return getTable(tableId).getMemberRoles();
	}

	public void addUserRole(UUID tableId, UUID targetUserId, String role, UUID operatorId) {
		assertTableOwner(tableId, operatorId);
		validateNotBlank(role, "Role name cannot be empty");
		User target = getUserOrThrow(targetUserId);
		target.addRole(role);
		userDao.update(target);
		log.info("Table owner {} added role '{}' to user {}", operatorId, role, targetUserId);
	}

	public void removeUserRole(UUID tableId, UUID targetUserId, String role, UUID operatorId) {
		assertTableOwner(tableId, operatorId);
		validateNotBlank(role, "Role name cannot be empty");
		User target = getUserOrThrow(targetUserId);
		target.removeRole(role);
		userDao.update(target);
		log.info("Table owner {} removed role '{}' from user {}", operatorId, role, targetUserId);
	}

	// ==================== Task List Operations ====================

	public TaskList createList(String name, UUID tableId) {
		return createList(name, tableId, null);
	}

	public TaskList createList(String name, UUID tableId, UUID operatorId) {
		validateNotBlank(name, "List name cannot be empty");
		TaskTable table = tableId != null ? getTable(tableId) : null;
		if (table != null) {
			assertTableOwner(table, operatorId);
		}
		TaskList list = new TaskList(name.trim(), table);
		taskListDao.save(list);
		log.info("Created TaskList '{}' (id={}) in table {}", list.getName(), list.getId(), tableId);
		return list;
	}

	public TaskList createList(String name) {
		return createList(name, null, null);
	}

	public Optional<TaskList> findList(UUID id) {
		return id != null ? Optional.ofNullable(taskListDao.findById(id)) : Optional.empty();
	}

	public TaskList getList(UUID id) {
		return findList(id).orElseThrow(() -> new NoSuchElementException("TaskList not found with id: " + id));
	}

	public List<TaskList> findListsByTable(UUID tableId) {
		return tableId != null ? taskListDao.findByTableId(tableId) : Collections.emptyList();
	}

	public TaskList renameList(UUID listId, String newName) {
		return renameList(listId, newName, null);
	}

	public TaskList renameList(UUID listId, String newName, UUID operatorId) {
		validateNotBlank(newName, "New list name cannot be empty");
		TaskList list = getList(listId);
		if (list.getTaskTable() != null) {
			assertTableOwner(list.getTaskTable(), operatorId);
		}
		list.setName(newName.trim());
		taskListDao.update(list);
		log.info("Renamed TaskList id={} to '{}' by operator {}", listId, list.getName(), operatorId);
		return list;
	}

	public boolean deleteList(UUID listId) {
		return deleteList(listId, null);
	}

	public boolean deleteList(UUID listId, UUID operatorId) {
		TaskList list = getList(listId);
		if (list.getTaskTable() != null) {
			assertTableOwner(list.getTaskTable(), operatorId);
		}
		log.info("Deleting TaskList id={} by operator {}", listId, operatorId);
		return taskListDao.deleteById(listId);
	}

	// ==================== Task Operations ====================

	public Task createTask(String title) {
		return createTask(title, null, null, null);
	}

	public Task createTask(String title, String description) {
		return createTask(title, description, null, null);
	}

	public Task createTask(String title, String description, UUID listId) {
		return createTask(title, description, null, listId);
	}

	public Task createTask(String title, String description, UUID userId, UUID listId) {
		validateNotBlank(title, "Task title cannot be empty");
		User user = userId != null ? getUserOrThrow(userId) : null;
		TaskList list = listId != null ? getList(listId) : null;

		Task task = new Task(title.trim(), description != null ? description.trim() : null, user, list);
		if (list != null) {
			task.setPosition(list.getTasks().size());
		}
		taskDao.save(task);
		log.info("Created Task '{}' (id={})", task.getTitle(), task.getId());
		return task;
	}

	public Task createTask(Task task) {
		Objects.requireNonNull(task, "Task cannot be null");
		validateNotBlank(task.getTitle(), "Task title cannot be empty");
		task.setTitle(task.getTitle().trim());
		taskDao.save(task);
		log.info("Created Task entity '{}' (id={})", task.getTitle(), task.getId());
		return task;
	}

	public Optional<Task> findTask(UUID id) {
		return id != null ? Optional.ofNullable(taskDao.findById(id)) : Optional.empty();
	}

	public Task getTask(UUID id) {
		return findTask(id).orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
	}

	public List<Task> findTasksByList(UUID listId) {
		return listId != null ? taskDao.findByTaskListId(listId) : Collections.emptyList();
	}

	public List<Task> findTasksByUser(UUID userId) {
		return userId != null ? taskDao.findByUserId(userId) : Collections.emptyList();
	}

	public List<Task> searchTasks(String query) {
		return taskDao.searchByTitle(query);
	}

	public List<Task> findAllTasks() {
		return taskDao.findAll();
	}

	public Task updateTask(UUID id, String newTitle, String newDescription) {
		return updateTask(id, newTitle, newDescription, null);
	}

	public Task updateTask(UUID id, String newTitle, String newDescription, UUID operatorId) {
		Task task = getTask(id);
		assertTaskAccess(task, operatorId);

		if (newTitle != null) {
			validateNotBlank(newTitle, "Task title cannot be empty");
			task.setTitle(newTitle.trim());
		}
		if (newDescription != null) {
			task.setDescription(newDescription.trim());
		}
		taskDao.update(task);
		log.info("Updated Task id={}: title='{}' by operator {}", id, task.getTitle(), operatorId);
		return task;
	}

	public Task updateTask(Task task) {
		Objects.requireNonNull(task, "Task cannot be null");
		if (task.getId() == null) {
			throw new IllegalArgumentException("Task ID cannot be null when updating");
		}
		validateNotBlank(task.getTitle(), "Task title cannot be empty");
		taskDao.update(task);
		log.info("Updated Task entity id={}", task.getId());
		return task;
	}

	public Task assignTask(UUID taskId, UUID userId) {
		return assignTask(taskId, userId, null);
	}

	public Task assignTask(UUID taskId, UUID userId, UUID operatorId) {
		Task task = getTask(taskId);
		assertTaskAccess(task, operatorId);
		User user = getUserOrThrow(userId);
		task.setUser(user);
		taskDao.update(task);
		log.info("Assigned Task id={} to User id={} by operator {}", taskId, userId, operatorId);
		return task;
	}

	public Task unassignTask(UUID taskId) {
		return unassignTask(taskId, null);
	}

	public Task unassignTask(UUID taskId, UUID operatorId) {
		Task task = getTask(taskId);
		assertTaskAccess(task, operatorId);
		task.setUser(null);
		taskDao.update(task);
		log.info("Unassigned Task id={} by operator {}", taskId, operatorId);
		return task;
	}

	public Task moveTask(UUID taskId, UUID targetListId) {
		return moveTask(taskId, targetListId, -1, null);
	}

	public Task moveTask(UUID taskId, UUID targetListId, int position) {
		return moveTask(taskId, targetListId, position, null);
	}

	public Task moveTask(UUID taskId, UUID targetListId, int position, UUID operatorId) {
		Task task = getTask(taskId);
		assertTaskAccess(task, operatorId);
		TaskList targetList = targetListId != null ? getList(targetListId) : null;
		if (targetList != null && targetList.getTaskTable() != null) {
			assertTableOwner(targetList.getTaskTable(), operatorId);
		}

		task.setTaskList(targetList);
		if (position >= 0) {
			task.setPosition(position);
		} else if (targetList != null) {
			task.setPosition(targetList.getTasks().size());
		}
		taskDao.update(task);
		log.info("Moved Task id={} to List id={} at position {} by operator {}", taskId, targetListId, task.getPosition(), operatorId);
		return task;
	}

	public Task reorderTask(UUID taskId, int newPosition) {
		return reorderTask(taskId, newPosition, null);
	}

	public Task reorderTask(UUID taskId, int newPosition, UUID operatorId) {
		if (newPosition < 0) {
			throw new IllegalArgumentException("Position must be non-negative: " + newPosition);
		}
		Task task = getTask(taskId);
		assertTaskAccess(task, operatorId);
		task.setPosition(newPosition);
		taskDao.update(task);
		log.info("Reordered Task id={} to position {} by operator {}", taskId, newPosition, operatorId);
		return task;
	}

	public boolean deleteTask(UUID taskId) {
		return deleteTask(taskId, null);
	}

	public boolean deleteTask(UUID taskId, UUID operatorId) {
		Task task = getTask(taskId);
		assertTaskAccess(task, operatorId);
		log.info("Deleting Task id={} by operator {}", taskId, operatorId);
		return taskDao.deleteById(taskId);
	}

	// ==================== Internal Helpers ====================

	public boolean isTableOwner(UUID tableId, UUID userId) {
		if (tableId == null || userId == null) return false;
		return getTable(tableId).isOwner(userId);
	}

	private void assertTableOwner(TaskTable table, UUID operatorId) {
		if (operatorId != null) {
			if (table == null || !table.isOwner(operatorId)) {
				throw new SecurityException("User " + operatorId + " is not the owner of table " + (table != null ? table.getId() : "null"));
			}
		}
	}

	private void assertTableOwner(UUID tableId, UUID operatorId) {
		if (operatorId != null) {
			assertTableOwner(getTable(tableId), operatorId);
		}
	}

	private void assertTaskAccess(Task task, UUID operatorId) {
		if (operatorId != null) {
			boolean isOwner = task.getTaskList() != null && task.getTaskList().getTaskTable() != null && task.getTaskList().getTaskTable().isOwner(operatorId);
			boolean isAssignee = task.getUser() != null && operatorId.equals(task.getUser().getId());
			if (!isOwner && !isAssignee) {
				throw new SecurityException("User " + operatorId + " is not authorized to modify task " + task.getId());
			}
		}
	}

	private User getUserOrThrow(UUID userId) {
		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null");
		}
		User user = userDao.findById(userId);
		if (user == null) {
			throw new NoSuchElementException("User not found with id: " + userId);
		}
		return user;
	}

	private void validateNotBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
	}
}
