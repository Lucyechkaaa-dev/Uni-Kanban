package controllers;

import backend.models.task.Board;
import backend.models.task.Task;
import backend.models.team.TeamRole;
import backend.security.AuthInterceptor;
import backend.services.Access;
import backend.services.BoardSharingService;
import backend.services.TaskService;
import controllers.ApiDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final TaskService tasks;
    private final BoardSharingService sharing;

    public BoardController(TaskService tasks, BoardSharingService sharing) {
        this.tasks = tasks;
        this.sharing = sharing;
    }

    /** The boards the user owns. */
    @GetMapping
    public List<BoardSummary> myBoards(@RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        return tasks.findTablesByUser(userId).stream().map(BoardSummary::of).toList();
    }

    /** The boards other users shared with the user, with the user's role on each. */
    @GetMapping("/shared")
    public List<SharedBoardDto> sharedWithMe(@RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        return sharing.sharedWith(userId).stream()
                .map(s -> new SharedBoardDto(s.board().getId(), s.board().getName(), s.board().getDescription(),
                        s.board().getUser() != null ? s.board().getUser().getUsername() : null,
                        s.role().name()))
                .toList();
    }

    /** The whole board for the screen: columns in order, cards inside them. Boards the user cannot see are 404. */
    @GetMapping("/{id}")
    public BoardDto board(@PathVariable("id") UUID id,
                          @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        Access access = tasks.accessLevel(id, userId); // 404 if there is no such board
        if (access == Access.NONE) {
            throw new NoSuchElementException("Board not found with id: " + id);
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

        return new BoardDto(board.getId(), board.getName(), board.getDescription(), access.name(),
                board.isOwner(userId), columns);
    }

    /** Renames the board. Needs MANAGE (owner or admin); a board the user cannot see is 404. */
    @PutMapping("/{id}")
    public BoardSummary rename(@PathVariable("id") UUID id, @RequestBody ApiDtos.RenameBoardRequest body,
                               @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        if (tasks.accessLevel(id, userId) == Access.NONE) {
            throw new NoSuchElementException("Board not found with id: " + id);
        }
        return BoardSummary.of(tasks.renameTable(id, body.name(), userId));
    }

    // ==================== members ====================

    @GetMapping("/{id}/members")
    public List<MemberDto> members(@PathVariable("id") UUID id,
                                   @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        return sharing.members(id, userId).stream()
                .map(m -> new MemberDto(m.userId(), m.username(), m.role().name()))
                .toList();
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDto addMember(@PathVariable("id") UUID id, @RequestBody ShareRequest body,
                               @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        var member = sharing.addMember(id, body.userId(), parseRole(body.role()), userId);
        return new MemberDto(member.userId(), member.username(), member.role().name());
    }

    @PutMapping("/{id}/members/{memberId}")
    public MemberDto changeRole(@PathVariable("id") UUID id, @PathVariable("memberId") UUID memberId,
                                @RequestBody RoleRequest body,
                                @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        var member = sharing.changeRole(id, memberId, parseRole(body.role()), userId);
        return new MemberDto(member.userId(), member.username(), member.role().name());
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable("id") UUID id, @PathVariable("memberId") UUID memberId,
                             @RequestAttribute(AuthInterceptor.USER_ID) UUID userId) {
        sharing.removeMember(id, memberId, userId);
    }

    /** Role names as the UI sends them. Unknown names are a 400 (IllegalArgumentException). */
    private static TeamRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        return TeamRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
    }
}
