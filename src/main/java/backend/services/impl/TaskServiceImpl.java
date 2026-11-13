package backend.services.impl;

import backend.dao.TaskDao;
import backend.dao.TaskListDao;
import backend.dao.TaskTableDao;
import backend.dao.UserDao;
import backend.dao.UserTeamDao;
import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.Board;
import backend.models.task.Priority;
import backend.models.team.UserTeam;
import backend.models.user.User;
import backend.services.Access;
import backend.services.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TaskServiceImpl implements TaskService{

	private final TaskDao taskDao;
	private final TaskListDao taskListDao;
	private final TaskTableDao taskTableDao;
	private final UserDao userDao;
	private final UserTeamDao userTeamDao;

	private static final int MAX_TAG_LENGTH = 32; // the tag column is varchar(32)

	// ==================== Task Table Operations ====================

	@Override
	public Board createTable(String name, UUID userId) {
		validateNotBlank(name, "Table name cannot be empty");
		User user = getUserOrThrow(userId);
		Board table = new Board(name.trim(), user);
		taskTableDao.save(table);
		log.info("Created TaskTable '{}' (id={}) for user {}", table.getName(), table.getId(), userId);
		return table;
	}

	@Override
	public Optional<Board> findTable(UUID id) {
		return id != null ? Optional.ofNullable(taskTableDao.findById(id)) : Optional.empty();
	}

	@Override
	public Board getTable(UUID id) {
		return findTable(id).orElseThrow(() -> new NoSuchElementException("TaskTable not found with id: " + id));
	}

	@Override
	public List<Board> findTablesByUser(UUID userId) {
		return userId != null ? taskTableDao.findByUserId(userId) : Collections.emptyList();
	}

	@Override
	public List<Board> findAllTables() {
		return taskTableDao.findAll();
	}

	@Override
	public Board renameTable(UUID tableId, String newName, UUID operatorId) {
		validateNotBlank(newName, "New table name cannot be empty");
		Board table = getTable(tableId);
		require(Access.MANAGE, table, operatorId);
		table.setName(newName.trim());
		taskTableDao.update(table);
		log.info("Renamed TaskTable id={} to '{}' by operator {}", tableId, table.getName(), operatorId);
		return table;
	}

	@Override
	public boolean deleteTable(UUID tableId, UUID operatorId) {
		if (tableId == null) return false;
		Optional<Board> tableOpt = findTable(tableId);
		if (tableOpt.isEmpty()) {
			return false;
		}
		require(Access.MANAGE, tableOpt.get(), operatorId);
		log.info("Deleting TaskTable id={} by operator {}", tableId, operatorId);
		return taskTableDao.deleteById(tableId);
	}

	// ==================== Access / Role Management Operations ====================

	@Override
	public Access accessLevel(UUID tableId, UUID userId) {
		return accessOf(getTable(tableId), userId);
	}

	@Override
	public void assignMemberRole(UUID tableId, UUID targetUserId, String role, UUID operatorId) {
		validateNotBlank(role, "Role name cannot be empty");
		Board table = getTable(tableId);
		require(Access.MANAGE, table, operatorId);
		getUserOrThrow(targetUserId);

		table.assignMemberRole(targetUserId, role);
		taskTableDao.update(table);
		log.info("User {} assigned role '{}' to user {} on table {}", operatorId, role, targetUserId, tableId);
	}

	@Override
	public void removeMemberRole(UUID tableId, UUID targetUserId, UUID operatorId) {
		Board table = getTable(tableId);
		require(Access.MANAGE, table, operatorId);
		table.removeMemberRole(targetUserId);
		taskTableDao.update(table);
		log.info("User {} removed role from user {} on table {}", operatorId, targetUserId, tableId);
	}

	@Override
	public String getMemberRole(UUID tableId, UUID userId) {
		return getTable(tableId).getMemberRole(userId);
	}

	@Override
	public Map<UUID, String> getMemberRoles(UUID tableId) {
		return getTable(tableId).getMemberRoles();
	}

	// ==================== Task List Operations ====================

	@Override
	public TaskList createList(String name, UUID tableId, UUID operatorId) {
		validateNotBlank(name, "List name cannot be empty");
		Board table = getTable(tableId);
		require(Access.MANAGE, table, operatorId);
		TaskList list = new TaskList(name.trim(), table);
		list.setPosition(taskListDao.nextPositionInBoard(table.getId()));
		taskListDao.save(list);
		log.info("Created TaskList '{}' (id={}) in table {}", list.getName(), list.getId(), tableId);
		return list;
	}

	@Override
	public Optional<TaskList> findList(UUID id) {
		return id != null ? Optional.ofNullable(taskListDao.findById(id)) : Optional.empty();
	}

	@Override
	public TaskList getList(UUID id) {
		return findList(id).orElseThrow(() -> new NoSuchElementException("TaskList not found with id: " + id));
	}

	@Override
	public List<TaskList> findListsByTable(UUID tableId) {
		return tableId != null ? taskListDao.findByTableId(tableId) : Collections.emptyList();
	}

	@Override
	public TaskList renameList(UUID listId, String newName, UUID operatorId) {
		validateNotBlank(newName, "New list name cannot be empty");
		TaskList list = getList(listId);
		require(Access.MANAGE, list.getBoard(), operatorId);
		list.setName(newName.trim());
		taskListDao.update(list);
		log.info("Renamed TaskList id={} to '{}' by operator {}", listId, list.getName(), operatorId);
		return list;
	}

	@Override
	public boolean deleteList(UUID listId, UUID operatorId) {
		if (listId == null) return false;
		Optional<TaskList> listOpt = findList(listId);
		if (listOpt.isEmpty()) {
			return false;
		}
		require(Access.MANAGE, listOpt.get().getBoard(), operatorId);
		log.info("Deleting TaskList id={} by operator {}", listId, operatorId);
		return taskListDao.deleteById(listId);
	}

	// ==================== Task Operations ====================

	@Override
	public Task createTask(String title, String description, UUID assigneeId, UUID listId, UUID operatorId) {
		return createTask(title, description, null, null, null, assigneeId, listId, operatorId);
	}

	@Override
	public Task createTask(String title, String description, Priority priority, String tag, LocalDate dueDate,
						   UUID assigneeId, UUID listId, UUID operatorId) {
		validateNotBlank(title, "Task title cannot be empty");
		String cleanTag = cleanTag(tag);
		TaskList list = getList(listId);
		require(Access.EDIT, list.getBoard(), operatorId);
		User assignee = assigneeId != null ? getUserOrThrow(assigneeId) : null;
		if (assignee != null) {
			requireCanBeAssigned(list.getBoard(), assignee.getId());
		}

		Task task = new Task(title.trim(), description != null ? description.trim() : null, assignee, list);
		if (priority != null) {
			task.setPriority(priority);
		}
		task.setTag(cleanTag);
		task.setDueDate(dueDate);
		// number and position are assigned inside the same transaction as the insert
		taskDao.saveInList(task);
		log.info("Created Task '{}' (id={}) by operator {}", task.getTitle(), task.getId(), operatorId);
		return task;
	}

	@Override
	public Optional<Task> findTask(UUID id) {
		return id != null ? Optional.ofNullable(taskDao.findById(id)) : Optional.empty();
	}

	@Override
	public Task getTask(UUID id) {
		return findTask(id).orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
	}

	@Override
	public List<Task> findTasksByList(UUID listId) {
		return listId != null ? taskDao.findByTaskListId(listId) : Collections.emptyList();
	}

	@Override
	public List<Task> findTasksByTable(UUID tableId) {
		return tableId != null ? taskDao.findByBoardId(tableId) : Collections.emptyList();
	}

	@Override
	public List<Task> findTasksByUser(UUID userId) {
		return userId != null ? taskDao.findByUserId(userId) : Collections.emptyList();
	}

	@Override
	public List<Task> searchTasks(String query) {
		return taskDao.searchByTitle(query);
	}

	@Override
	public List<Task> findAllTasks() {
		return taskDao.findAll();
	}

	@Override
	public Task updateTask(UUID id, String newTitle, String newDescription, UUID operatorId) {
		Task task = getTask(id);
		requireOnTask(Access.EDIT, task, operatorId);

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

	@Override
	public Task updateTask(Task task, UUID operatorId) {
		Objects.requireNonNull(task, "Task cannot be null");
		if (task.getId() == null) {
			throw new IllegalArgumentException("Task ID cannot be null when updating");
		}
		validateNotBlank(task.getTitle(), "Task title cannot be empty");

		// access is checked against the card as it is stored, and only the editable fields are copied:
		// the caller cannot move the card to another column or board through this method
		Task stored = getTask(task.getId());
		requireOnTask(Access.EDIT, stored, operatorId);

		stored.setTitle(task.getTitle().trim());
		stored.setDescription(task.getDescription() != null ? task.getDescription().trim() : null);
		if (task.getPriority() != null) {
			stored.setPriority(task.getPriority());
		}
		stored.setTag(cleanTag(task.getTag()));
		stored.setDueDate(task.getDueDate());
		taskDao.update(stored);
		log.info("Updated Task entity id={} by operator {}", stored.getId(), operatorId);
		return stored;
	}

	@Override
	public Task assignTask(UUID taskId, UUID userId, UUID operatorId) {
		Task task = getTask(taskId);
		requireOnTask(Access.EDIT, task, operatorId);
		User user = getUserOrThrow(userId);
		Board board = boardOf(task);
		if (board != null) {
			requireCanBeAssigned(board, user.getId());
		}
		task.setUserOwner(user);
		taskDao.update(task);
		log.info("Assigned Task id={} to User id={} by operator {}", taskId, userId, operatorId);
		return task;
	}

	@Override
	public Task unassignTask(UUID taskId, UUID operatorId) {
		Task task = getTask(taskId);
		requireOnTask(Access.EDIT, task, operatorId);
		task.setUserOwner(null);
		taskDao.update(task);
		log.info("Unassigned Task id={} by operator {}", taskId, operatorId);
		return task;
	}

	@Override
	public Task moveTask(UUID taskId, UUID targetListId, int position, UUID operatorId) {
		Task task = getTask(taskId);
		requireOnTask(Access.EDIT, task, operatorId);
		TaskList targetList = getList(targetListId);
		require(Access.EDIT, targetList.getBoard(), operatorId);

		// card numbers (TASK-1, TASK-2...) are counted per board, so a card stays on its board
		Board currentBoard = boardOf(task);
		if (currentBoard != null && !Objects.equals(currentBoard.getId(), targetList.getBoard().getId())) {
			throw new IllegalArgumentException("A card cannot be moved to a column of another board");
		}

		task.setTaskListParent(targetList);
		if (position >= 0) {
			task.setPosition(position);
		} else {
			task.setPosition(taskDao.nextPositionInList(targetList.getId()));
		}
		taskDao.update(task);
		log.info("Moved Task id={} to List id={} at position {} by operator {}", taskId, targetListId, task.getPosition(), operatorId);
		return task;
	}

	@Override
	public Task reorderTask(UUID taskId, int newPosition, UUID operatorId) {
		if (newPosition < 0) {
			throw new IllegalArgumentException("Position must be non-negative: " + newPosition);
		}
		Task task = getTask(taskId);
		requireOnTask(Access.EDIT, task, operatorId);
		task.setPosition(newPosition);
		taskDao.update(task);
		log.info("Reordered Task id={} to position {} by operator {}", taskId, newPosition, operatorId);
		return task;
	}

	@Override
	public boolean deleteTask(UUID taskId, UUID operatorId) {
		if (taskId == null) return false;
		Optional<Task> taskOpt = findTask(taskId);
		if (taskOpt.isEmpty()) {
			return false;
		}
		requireOnTask(Access.EDIT, taskOpt.get(), operatorId);
		log.info("Deleting Task id={} by operator {}", taskId, operatorId);
		return taskDao.deleteById(taskId);
	}

	// ==================== Internal Helpers / Utilities ====================

	@Override
	public boolean isTableOwner(UUID tableId, UUID userId) {
		if (tableId == null || userId == null) return false;
		return getTable(tableId).isOwner(userId);
	}

	private Access accessOf(Board board, UUID userId) {
		if (board == null || userId == null) {
			return Access.NONE;
		}
		if (board.isOwner(userId)) {
			return Access.MANAGE;
		}
		String boardRole = board.getMemberRole(userId);
		if (boardRole != null) {
			return Access.ofBoardRole(boardRole);
		}
		if (board.getTeam() != null) {
			UserTeam membership = userTeamDao.findByUserAndTeam(userId, board.getTeam().getId());
			return membership != null ? Access.of(membership.getRole()) : Access.NONE;
		}
		return Access.NONE;
	}

		private void require(Access needed, Board board, UUID operatorId) {
		if (operatorId == null) {
			throw new SecurityException("Authentication required");
		}
		if (!accessOf(board, operatorId).atLeast(needed)) {
			throw new SecurityException("User " + operatorId + " has no " + needed + " access to table "
					+ (board != null ? board.getId() : "null"));
		}
	}

	private void requireOnTask(Access needed, Task task, UUID operatorId) {
		Board board = boardOf(task);
		if (board != null) {
			require(needed, board, operatorId);
			return;
		}
		// Normally a card sits in a column and the column belongs to a board: the board decides who may touch it.
		// Old rows can hold a card that sits in no column at all (taskListParent == null), so there is no board to ask.
		// For such a card the only person allowed to touch it is the one it is assigned to.
		if (operatorId == null) {
			throw new SecurityException("Authentication required");
		}
		boolean isAssignee = task.getUserOwner() != null && operatorId.equals(task.getUserOwner().getId());
		if (!isAssignee) {
			throw new SecurityException("User " + operatorId + " is not authorized to modify task " + task.getId());
		}
	}

		private void requireCanBeAssigned(Board board, UUID assigneeId) {
		if (!accessOf(board, assigneeId).atLeast(Access.EDIT)) {
			throw new IllegalArgumentException("User " + assigneeId + " cannot edit this board, so cards cannot be assigned to him");
		}
	}

	private Board boardOf(Task task) {
		return task.getTaskListParent() != null ? task.getTaskListParent().getBoard() : null;
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

	/** Blank tag = no tag; a tag longer than the column is refused here, not by the database. */
	private String cleanTag(String tag) {
		if (tag == null || tag.isBlank()) {
			return null;
		}
		String trimmed = tag.trim();
		if (trimmed.length() > MAX_TAG_LENGTH) {
			throw new IllegalArgumentException("Tag must be at most " + MAX_TAG_LENGTH + " characters long");
		}
		return trimmed;
	}

	private void validateNotBlank(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
	}
}