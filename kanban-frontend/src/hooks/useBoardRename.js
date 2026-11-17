import { useRef, useState } from 'react';
import { api } from '../api';

/** Renaming the board by clicking the pencil: the title turns into an input; Enter or leaving the input saves, Escape cancels. */
export function useBoardRename({ board, setBoard, reloadList, setError }) {
    const [renaming, setRenaming] = useState(false);
    const [nameDraft, setNameDraft] = useState('');
    const finished = useRef(false); // one save per rename session; Escape: the blur that follows must not save

    const startRename = () => {
        finished.current = false;
        setNameDraft(board.name);
        setRenaming(true);
    };

    const cancelRename = () => {
        finished.current = true;
        setRenaming(false);
    };

    const saveName = () => {
        // Enter, then the blur from removing the input, must not send twice
        if (finished.current) return;
        finished.current = true;
        const name = nameDraft.trim();
        setRenaming(false);
        if (!name || name === board.name) return;
        api.renameBoard(board.id, name)
            .then(() => {
                setBoard((current) => (current ? current.with({ name }) : current));
                return reloadList();
            })
            .catch((err) => setError(err.message));
    };

    return { renaming, nameDraft, setNameDraft, startRename, saveName, cancelRename };
}
