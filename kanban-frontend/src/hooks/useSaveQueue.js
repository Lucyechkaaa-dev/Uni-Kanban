import { useCallback, useRef } from 'react';
import { api } from '../api';

/**
 * The queue of requests to the server.
 *
 * A change appears on the screen at once and goes to the server in the background (send).
 * The requests leave one after another, in the order of the clicks (a card that was moved and then deleted
 * must be moved first). If the server refuses one, its message is shown and the board is read again:
 * that undoes whatever the screen had assumed.
 *
 * enqueue(job) puts any other job into the same queue: live updates use it, so a read from the server
 * never shows an older state than the one we have just sent.
 */
export function useSaveQueue({ board, setBoard, setError }) {
    const queue = useRef(Promise.resolve());

    const enqueue = useCallback((job) => {
        queue.current = queue.current.then(job).catch(() => {});
    }, []);

    // onSaved gets the answer of the server (used for new cards)
    const send = (action, onSaved) => {
        enqueue(async () => {
            try {
                const result = await action();
                if (onSaved) onSaved(result);
            } catch (err) {
                setError(err.message);
                try {
                    setBoard(await api.board(board.id));
                } catch {
                    /* the screen stays as it is; the message says what went wrong */
                }
            }
        });
    };

    return { enqueue, send };
}
