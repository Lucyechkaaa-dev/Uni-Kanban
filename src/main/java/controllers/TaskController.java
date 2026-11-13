package controllers;

import backend.models.task.Priority;
import backend.models.task.Task;
import backend.security.AuthInterceptor;
import backend.services.Access;
import backend.services.TaskService;
import controllers.ApiDtos.MoveRequest;
import controllers.ApiDtos.TaskDto;
import controllers.ApiDtos.TaskRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

/** Cards. The access checks (EDIT on the board) are done by TaskService; a refusal becomes 403. */
@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService tasks;

    public TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    /** One card as it is stored now, for a live update. A card the user cannot see, or that is gone, is 404. */
    @GetMapping("/tasks/{id}")
    public TaskDto get(@PathVariable("id") UUID id,
                       @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Task task = tasks.findTask(id).orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
        if (task.getTaskListParent() == null || task.getTaskListParent().getBoard() == null) {
            throw new NoSuchElementException("Task not found with id: " + id);
        }
        UUID boardId = task.getTaskListParent().getBoard().getId();
        if (tasks.accessLevel(boardId, userId) == Access.NONE) {
            throw new NoSuchElementException("Task not found with id: " + id);
        }
        return TaskDto.of(task);
    }

    @PostMapping("/columns/{columnId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@PathVariable("columnId") UUID columnId,
                          @RequestBody TaskRequest body,
                          @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Task created = tasks.createTask(body.title(), body.description(), parsePriority(body.priority()),
                body.tag(), body.dueDate(), body.assigneeId(), columnId, userId);
        return TaskDto.of(created);
    }

    /** Replaces title, description, priority, tag and due date. The column is changed by /move, not here. */
    @PutMapping("/tasks/{id}")
    public TaskDto update(@PathVariable("id") UUID id,
                          @RequestBody TaskRequest body,
                          @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Task changes = new Task(body.title(), body.description());
        changes.setId(id);
        changes.setPriority(parsePriority(body.priority())); // null = keep the old one
        changes.setTag(body.tag());
        changes.setDueDate(body.dueDate());
        return TaskDto.of(tasks.updateTask(changes, userId));
    }

    @PutMapping("/tasks/{id}/move")
    public TaskDto move(@PathVariable("id") UUID id,
                        @RequestBody MoveRequest body,
                        @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        int position = body.position() != null ? body.position() : -1; // -1 = to the end of the column
        return TaskDto.of(tasks.moveTask(id, body.columnId(), position, userId));
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id,
                       @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        if (!tasks.deleteTask(id, userId)) {
            throw new NoSuchElementException("Task not found with id: " + id);
        }
    }

    private static Priority parsePriority(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Priority.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown priority '" + value + "', use LOW, MEDIUM or HIGH");
        }
    }
}
