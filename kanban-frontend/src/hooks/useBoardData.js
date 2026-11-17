import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import { MESSAGES } from '../constants';

/**
 * What the board page shows: the list of boards (mine and shared), the open board and the last error.
 * Loads the list at the start, opens the first board, and loads the board again when another one is chosen.
 */
export function useBoardData() {
    const [boards, setBoards] = useState([]);     // BoardSummary[]
    const [boardId, setBoardId] = useState(null);
    const [board, setBoard] = useState(null);     // Board
    const [error, setError] = useState('');

    const loadBoardList = useCallback(
        () => api.boardList().then((list) => { setBoards(list); return list; }),
        []
    );

    // changes only the columns of the loaded board
    const setColumns = useCallback(
        (update) => setBoard((current) => (current ? current.withColumns(update(current.columns)) : current)),
        []
    );

    useEffect(() => {
        let cancelled = false;
        loadBoardList()
            .then((list) => {
                if (cancelled) return;
                if (list.length === 0) throw new Error(MESSAGES.NO_BOARDS);
                setBoardId(list[0].id);
            })
            .catch((err) => { if (!cancelled) setError(err.message); });
        return () => { cancelled = true; };
    }, [loadBoardList]);

    useEffect(() => {
        if (!boardId) return undefined;
        let cancelled = false;
        api.board(boardId)
            .then((loaded) => { if (!cancelled) setBoard(loaded); })
            .catch((err) => { if (!cancelled) setError(err.message); });
        return () => { cancelled = true; };
    }, [boardId]);

    // my role on the open board: OWNER, or VIEWER / MEMBER / ADMIN from the shared list
    const myRole = boards.find((b) => b.id === boardId)?.role ?? null;

    return { boards, setBoards, boardId, setBoardId, board, setBoard, setColumns, error, setError, loadBoardList, myRole };
}
