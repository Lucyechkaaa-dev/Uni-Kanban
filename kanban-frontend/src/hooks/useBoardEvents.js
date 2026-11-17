import { useEffect, useRef, useState } from 'react';
import { api } from '../api';
import { CHANGE_KIND_TASK, EVENT_CHANGE, HTTP, LINK, MESSAGES, NOTICE_LIFETIME_MS, NOTICES_KEPT } from '../constants';
import { makeId } from '../utils';

/**
 * Live updates, like a shared Trello board (the backend sends them as Server-Sent Events):
 *  - a card change updates only that card (no redraw, the card editor and scroll stay as they are);
 *  - a structural change (columns, name, members) reloads the board;
 *  - after a dropout, the board is reloaded, since events sent while disconnected are not replayed;
 *  - every server read waits for our own pending saves (enqueue), so it never shows an older state than we sent.
 *
 * Returns the state of the connection (link) and the short messages about what other users did (notices).
 */
export function useBoardEvents({ boardId, board, username, enqueue, setBoard, setBoards, setBoardId, setError }) {
    const [link, setLink] = useState(LINK.CONNECTING);
    const [notices, setNotices] = useState([]); // { id, text }

    // Keep the latest board for the live handlers (they run outside render).
    const boardRef = useRef(null);
    useEffect(() => {
        boardRef.current = board;
    }, [board]);

    useEffect(() => {
        if (!boardId) return undefined;
        let active = true;
        let wasDown = false;
        const source = new EventSource(api.eventsUrl(boardId));

        const refreshList = () => api.boardList().then((list) => { if (active) setBoards(list); }).catch(() => {});
        const notify = (text) => {
            const id = makeId();
            setNotices((list) => [...list.slice(-NOTICES_KEPT), { id, text }]);
            setTimeout(() => setNotices((list) => list.filter((n) => n.id !== id)), NOTICE_LIFETIME_MS);
        };

        const reloadBoard = () => enqueue(async () => {
            try {
                const fresh = await api.board(boardId);
                if (active) setBoard(fresh);
            } catch (err) {
                if (!active) return;
                if (err.status === HTTP.NOT_FOUND) {
                    // unshared or deleted: move to another board
                    setError(MESSAGES.BOARD_UNAVAILABLE);
                    const list = await api.boardList();
                    if (active) {
                        setBoards(list);
                        setBoardId(list.length > 0 ? list[0].id : null);
                    }
                } else {
                    setError(err.message);
                }
            }
        });

        const refreshCard = (taskId, actor) => enqueue(async () => {
            let card = null;
            try {
                card = await api.card(taskId);
            } catch (err) {
                if (err.status !== HTTP.NOT_FOUND) {
                    if (active) setError(err.message);
                    return;
                }
            }
            if (!active) return;

            const known = boardRef.current;
            if (card && known && !known.hasColumn(card.columnId)) {
                // a column we do not know yet (added by someone else): take the whole board
                reloadBoard();
                return;
            }
            setBoard((current) => (current ? current.withCardPlaced(taskId, card) : current));
            if (actor && actor !== username) {
                notify(card ? `${actor} changed “${card.name}”` : `${actor} deleted a card`);
            }
        });

        source.onopen = () => {
            setLink(LINK.LIVE);
            if (wasDown) {
                wasDown = false;
                reloadBoard();   // we may have missed changes while disconnected
                refreshList();
            }
        };
        source.onerror = () => {
            wasDown = true;
            setLink(LINK.RECONNECTING); // EventSource retries by itself
        };
        source.addEventListener(EVENT_CHANGE, (event) => {
            const change = JSON.parse(event.data);
            if (change.kind === CHANGE_KIND_TASK) {
                refreshCard(change.taskId, change.actor);
            } else {
                reloadBoard();
                refreshList();
            }
        });

        return () => {
            active = false;
            source.close();
        };
    }, [boardId, username, enqueue, setBoard, setBoards, setBoardId, setError]);

    return { link, notices };
}
