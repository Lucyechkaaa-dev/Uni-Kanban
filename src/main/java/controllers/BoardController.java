package controllers;

import backend.models.task.Board;
import backend.models.task.Task;
import backend.security.AuthInterceptor;
import backend.services.Access;
import backend.services.TaskService;
import controllers.ApiDtos.BoardDto;
import controllers.ApiDtos.BoardSummary;
import controllers.ApiDtos.ColumnDto;
import controllers.ApiDtos.TaskDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final TaskService tasks;

    public BoardController(TaskService tasks) {
        this.tasks = tasks;
    }

    /** The boards the user owns (for now the one made at registration, "My board"). */
    @GetMapping
    public List<BoardSummary> myBoards(@RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        return tasks.findTablesByUser(userId).stream().map(BoardSummary::of).toList();
    }

    /** The whole board for the screen: columns in order, cards inside them. */
    @GetMapping("/{id}")
    public BoardDto board(@PathVariable("id") UUID id,
                          @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Access access = tasks.accessLevel(id, userId); // 404 if there is no such board
        if (!access.atLeast(Access.VIEW)) {
            throw new SecurityException("User " + userId + " cannot view board " + id);
        }
        Board board = tasks.getTable(id);

        Map<UUID, List<TaskDto>> cardsByColumn = new HashMap<>();
        for (Task task : tasks.findTasksByTable(id)) {
            cardsByColumn.computeIfAbsent(task.getTaskListParent().getId(), key -> new ArrayList<>())
                    .add(TaskDto.of(task));
        }
        List<ColumnDto> columns = tasks.findListsByTable(id).stream()
                .map(list -> new ColumnDto(list.getId(), list.getName(), list.getColor(), list.getPosition(),
                        cardsByColumn.getOrDefault(list.getId(), List.of())))
                .toList();

        return new BoardDto(board.getId(), board.getName(), board.getDescription(), access.name(), columns);
    }
}
