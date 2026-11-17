import { useEffect, useState } from 'react';
import { api } from '../api';

/** The people with access to a board (for the Share dialog). reload() reads them again. */
export function useMembers(boardId, setError) {
    const [members, setMembers] = useState([]); // Member[]

    const reload = () => api.members(boardId).then(setMembers);

    useEffect(() => {
        let cancelled = false;
        api.members(boardId)
            .then((list) => { if (!cancelled) setMembers(list); })
            .catch((err) => { if (!cancelled) setError(err.message); });
        return () => { cancelled = true; };
    }, [boardId, setError]);

    return { members, reload };
}
