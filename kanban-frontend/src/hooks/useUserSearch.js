import { useEffect, useState } from 'react';
import { api } from '../api';
import { MIN_SEARCH_LENGTH, SEARCH_DELAY_MS } from '../constants';

/**
 * Search for people to add to a board: as the user types, but only after a short pause and
 * from 2 characters on. People who already have access are not offered.
 */
export function useUserSearch(members, setError) {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]); // User[]

    useEffect(() => {
        if (query.trim().length < MIN_SEARCH_LENGTH) return undefined;
        const timer = setTimeout(async () => {
            try {
                const found = await api.searchUsers(query.trim());
                const taken = new Set(members.map((m) => m.userId));
                setResults(found.filter((u) => !taken.has(u.id)));
            } catch (err) {
                setError(err.message);
            }
        }, SEARCH_DELAY_MS);
        return () => clearTimeout(timer);
    }, [query, members, setError]);

    // results are cleared here, in the change handler, not in the effect
    const changeQuery = (value) => {
        setQuery(value);
        if (value.trim().length < MIN_SEARCH_LENGTH) setResults([]);
    };

    const reset = () => {
        setQuery('');
        setResults([]);
    };

    return { query, results, changeQuery, reset };
}
