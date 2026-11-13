package backend.services;

import java.util.UUID;

/**
 * What changed on a board, sent to everyone watching it.
 *  - kind "task": one card changed (created, edited, moved, reordered, assigned or deleted). Clients fetch that card.
 *  - kind "board": columns, the name or the members changed. Clients reload the board.
 * actor is the username of who made the change (null when unknown).
 */
public record BoardChange(String kind, UUID taskId, String actor) {

    public static BoardChange task(UUID taskId, String actor) {
        return new BoardChange("task", taskId, actor);
    }

    public static BoardChange board(String actor) {
        return new BoardChange("board", null, actor);
    }
}
