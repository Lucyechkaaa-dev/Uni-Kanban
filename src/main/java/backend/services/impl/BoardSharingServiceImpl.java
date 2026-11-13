package backend.services.impl;

import backend.dao.BoardSharingDao;
import backend.dao.TaskTableDao;
import backend.models.task.Board;
import backend.models.team.Team;
import backend.models.team.TeamRole;
import backend.models.team.UserTeam;
import backend.models.user.User;
import backend.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BoardSharingServiceImpl implements BoardSharingService {

    private final TaskService taskService;
    private final TeamService teamService;
    private final UserTeamService userTeamService;
    private final UserService userService;
    private final TaskTableDao taskTableDao;
    private final BoardSharingDao sharingDao;
    private final BoardEvents events;

    @Override
    public List<Member> members(UUID boardId, UUID operatorId) {
        Board board = visibleBoard(boardId, operatorId);
        List<Member> result = new ArrayList<>();
        result.add(new Member(board.getUser().getId(), board.getUser().getUsername(), TeamRole.OWNER));
        Team team = sharingTeam(board);
        if (team != null) {
            for (UserTeam membership : sharingDao.findTeamMembers(team.getId())) {
                if (membership.getRole() != TeamRole.OWNER) {
                    result.add(new Member(membership.getUser().getId(), membership.getUser().getUsername(), membership.getRole()));
                }
            }
        }
        return result;
    }

    @Override
    public Member addMember(UUID boardId, UUID userId, TeamRole role, UUID operatorId) {
        Board board = manageableBoard(boardId, operatorId);
        requireGrantableRole(role);
        requireCanGrant(board, role, operatorId);

        User target = userService.findById(requireId(userId))
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
        if (board.isOwner(target.getId())) {
            throw new IllegalArgumentException("The owner already has full access to this board");
        }

        Team team = ensureSharingTeam(board);
        userTeamService.addMember(team.getId(), target.getId(), role);
        events.publish(boardId, BoardChange.board(null));
        log.info("User {} shared board {} with user {} as {}", operatorId, boardId, userId, role);
        return new Member(target.getId(), target.getUsername(), role);
    }

    @Override
    public Member changeRole(UUID boardId, UUID userId, TeamRole role, UUID operatorId) {
        Board board = manageableBoard(boardId, operatorId);
        requireGrantableRole(role);
        Team team = requireSharingTeam(board, userId);
        TeamRole current = currentRole(team, userId);
        requireCanGrant(board, role, operatorId);
        requireCanTouch(board, current, operatorId);

        userTeamService.updateRole(team.getId(), userId, role);
        events.publish(boardId, BoardChange.board(null));
        log.info("User {} changed user {} to {} on board {}", operatorId, userId, role, boardId);
        return new Member(userId, userService.getById(userId).getUsername(), role);
    }

    @Override
    public void removeMember(UUID boardId, UUID userId, UUID operatorId) {
        Board board = visibleBoard(boardId, operatorId);
        Team team = requireSharingTeam(board, userId);
        TeamRole current = currentRole(team, userId);
        if (current == TeamRole.OWNER) {
            throw new IllegalArgumentException("The owner cannot be removed from the board");
        }

        boolean leaving = userId.equals(operatorId);
        if (!leaving) {
            requireManage(boardId, operatorId);
            requireCanTouch(board, current, operatorId);
        }
        userTeamService.removeMember(team.getId(), userId);
        events.publish(boardId, BoardChange.board(null));
        log.info("User {} removed user {} from board {}", operatorId, userId, boardId);
    }

    @Override
    public List<SharedBoard> sharedWith(UUID userId) {
        List<SharedBoard> result = new ArrayList<>();
        for (Object[] row : sharingDao.findBoardsSharedWith(userId)) {
            result.add(new SharedBoard((Board) row[0], (TeamRole) row[1]));
        }
        return result;
    }

    // ==================== the sharing team ====================

    /** The team that exists only for this board, or null if the board has none. */
    private static Team sharingTeam(Board board) {
        Team team = board.getTeam();
        return team != null && team.getName().equals(sharingTeamName(board)) ? team : null;
    }

    /** Creates the board's sharing team on first use, with the owner as OWNER. */
    private Team ensureSharingTeam(Board board) {
        Team existing = sharingTeam(board);
        if (existing != null) {
            return existing;
        }
        if (board.getTeam() != null) {
            throw new IllegalArgumentException("This board belongs to the team '" + board.getTeam().getName()
                    + "'. Manage its access through that team.");
        }
        Team team = teamService.createTeam(sharingTeamName(board), "Shared board " + board.getId(), board.getUser().getId());
        board.setTeam(team);
        taskTableDao.update(board);
        log.info("Created sharing team {} for board {}", team.getId(), board.getId());
        return team;
    }

    private Team requireSharingTeam(Board board, UUID userId) {
        requireId(userId);
        Team team = sharingTeam(board);
        if (team == null) {
            throw new NoSuchElementException("This user is not a member of the board");
        }
        return team;
    }

    private TeamRole currentRole(Team team, UUID userId) {
        Optional<TeamRole> role = userTeamService.getUserRole(team.getId(), userId);
        return role.orElseThrow(() -> new NoSuchElementException("This user is not a member of the board"));
    }

    private static String sharingTeamName(Board board) {
        return "board:" + board.getId();
    }

    // ==================== rules ====================

    /** A board the user cannot see looks the same as a board that does not exist. */
    private Board visibleBoard(UUID boardId, UUID operatorId) {
        Board board = taskService.getTable(boardId);
        if (operatorId == null) {
            throw new SecurityException("Authentication required");
        }
        if (taskService.accessLevel(boardId, operatorId) == Access.NONE) {
            throw new NoSuchElementException("Board not found with id: " + boardId);
        }
        return board;
    }

    private Board manageableBoard(UUID boardId, UUID operatorId) {
        Board board = visibleBoard(boardId, operatorId);
        requireManage(boardId, operatorId);
        return board;
    }

    private void requireManage(UUID boardId, UUID operatorId) {
        if (!taskService.accessLevel(boardId, operatorId).atLeast(Access.MANAGE)) {
            throw new SecurityException("User " + operatorId + " cannot manage members of board " + boardId);
        }
    }

    /** Members are given VIEWER, MEMBER or ADMIN. OWNER is never granted; it is the board owner. */
    private static void requireGrantableRole(TeamRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (role == TeamRole.OWNER) {
            throw new IllegalArgumentException("Role must be VIEWER, MEMBER or ADMIN");
        }
    }

    /** Only the owner grants ADMIN. An ADMIN may grant VIEWER or MEMBER. */
    private static void requireCanGrant(Board board, TeamRole role, UUID operatorId) {
        if (role == TeamRole.ADMIN && !board.isOwner(operatorId)) {
            throw new SecurityException("Only the owner can grant the ADMIN role");
        }
    }

    /** An ADMIN cannot change or remove another ADMIN; only the owner can. */
    private static void requireCanTouch(Board board, TeamRole current, UUID operatorId) {
        if (current == TeamRole.ADMIN && !board.isOwner(operatorId)) {
            throw new SecurityException("Only the owner can change or remove an admin");
        }
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return id;
    }
}
