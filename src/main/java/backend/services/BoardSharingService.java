package backend.services;

import backend.models.task.Board;
import backend.models.team.TeamRole;

import java.util.List;
import java.util.UUID;

/**
 * Sharing a board. Under the hood a shared board gets its own team: the owner is the team's OWNER,
 * and everyone added to the board is a member of that team with a team role.
 * <p>
 * Team role -> board access: VIEWER = read, MEMBER = work on cards, ADMIN = also manage members and columns.
 * <p>
 * Rules (enforced here, not in the UI):
 *  - a user with no access to a board gets "not found" (NoSuchElementException), never "forbidden"
 *  - the owner and ADMINs manage members; only the owner grants or changes ADMIN
 *  - the owner is not a member and cannot be added, changed or removed
 *  - any member may leave the board
 *  - a board that already belongs to a team other than its own sharing team is not changed here
 */
public interface BoardSharingService {

    /** A person with access to a board. The owner appears with role OWNER. */
    record Member(UUID userId, String username, TeamRole role) {
    }

    /** A board another user owns, shared with the caller, with the caller's role. */
    record SharedBoard(Board board, TeamRole role) {
    }

    List<Member> members(UUID boardId, UUID operatorId);

    Member addMember(UUID boardId, UUID userId, TeamRole role, UUID operatorId);

    Member changeRole(UUID boardId, UUID userId, TeamRole role, UUID operatorId);

    void removeMember(UUID boardId, UUID userId, UUID operatorId);

    List<SharedBoard> sharedWith(UUID userId);
}
