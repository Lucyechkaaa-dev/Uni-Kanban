import { useEffect, useState } from 'react';
import { api } from '../api';
import './ShareDialog.css';

// Team role names as the backend knows them, with what they allow.
const ROLE_LABELS = {
    VIEWER: 'Viewer (read only)',
    MEMBER: 'Editor (works on cards)',
    ADMIN: 'Admin (manages members)',
};

const SEARCH_DELAY_MS = 300;
const MIN_SEARCH_LENGTH = 2;

// The server enforces these rules; the dialog only hides what would be refused.
// Only the owner grants or changes ADMIN.
export default function ShareDialog({ boardId, myRole, currentUserId, onClose, onChanged }) {
    const isOwner = myRole === 'OWNER';
    const canManage = isOwner || myRole === 'ADMIN';
    const [members, setMembers] = useState([]);
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [grantRole, setGrantRole] = useState('MEMBER');
    const [error, setError] = useState('');

    const roleOptions = (current) => Object.keys(ROLE_LABELS).filter((r) => isOwner || r !== 'ADMIN' || current === 'ADMIN');

    const reload = () => api.members(boardId).then(setMembers);

    useEffect(() => {
        let cancelled = false;
        api.members(boardId)
            .then((list) => { if (!cancelled) setMembers(list); })
            .catch((err) => { if (!cancelled) setError(err.message); });
        return () => { cancelled = true; };
    }, [boardId]);

    // search as the user types, but only after a short pause and from 2 characters on
    // (results are cleared in the change handler, not here)
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
    }, [query, members]);

    const run = async (action) => {
        setError('');
        try {
            await action();
            await reload();
            onChanged();
        } catch (err) {
            setError(err.message);
        }
    };

    const add = (user) => run(async () => {
        await api.addMember(boardId, user.id, grantRole);
        setQuery('');
        setResults([]);
    });

    const changeRole = (member, role) => run(() => api.changeMemberRole(boardId, member.userId, role));

    const remove = (member) => {
        const leaving = member.userId === currentUserId;
        if (!leaving && !window.confirm(`Remove ${member.username} from this board?`)) return;
        run(async () => {
            await api.removeMember(boardId, member.userId);
            if (leaving) onClose();
        });
    };

    return (
        <div className="share-overlay" role="dialog" aria-modal="true" aria-label="Share board" onClick={onClose}>
            <div className="share-dialog" onClick={(e) => e.stopPropagation()}>
                <div className="share-header">
                    <h2>Share board</h2>
                    <button className="icon-btn" aria-label="Close" onClick={onClose}>✕</button>
                </div>

                {error && <div className="board-error" role="alert" onClick={() => setError('')}>{error}</div>}

                {canManage && (
                    <div className="share-search">
                        <div className="form-row">
                            <input
                                type="text"
                                className="edit-input"
                                placeholder="Search users by name"
                                aria-label="Search users"
                                value={query}
                                onChange={(e) => {
                                    setQuery(e.target.value);
                                    if (e.target.value.trim().length < MIN_SEARCH_LENGTH) setResults([]);
                                }}
                            />
                            <select
                                className="edit-select"
                                aria-label="Role for new member"
                                value={grantRole}
                                onChange={(e) => setGrantRole(e.target.value)}
                            >
                                {roleOptions(null).map((r) => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
                            </select>
                        </div>
                        {results.map((u) => (
                            <div key={u.id} className="share-row">
                                <span className="share-name">{u.username}</span>
                                <button className="btn-sm-save" onClick={() => add(u)}>Add</button>
                            </div>
                        ))}
                        {query.trim().length >= MIN_SEARCH_LENGTH && results.length === 0 && (
                            <div className="share-empty">No matching users</div>
                        )}
                    </div>
                )}

                <h3>People with access</h3>
                {members.map((m) => {
                    const isOwnerRow = m.role === 'OWNER';
                    const editable = !isOwnerRow && canManage && (isOwner || m.role !== 'ADMIN');
                    const removable = !isOwnerRow && (m.userId === currentUserId || editable);
                    return (
                        <div key={m.userId} className="share-row">
                            <span className="share-name">
                                {m.username}
                                {m.userId === currentUserId && <span className="share-empty">(you)</span>}
                            </span>
                            {isOwnerRow ? (
                                <span className="share-badge">Owner</span>
                            ) : (
                                <span className="share-controls">
                                    <select
                                        className="edit-select"
                                        aria-label={`Role of ${m.username}`}
                                        value={m.role}
                                        disabled={!editable}
                                        onChange={(e) => changeRole(m, e.target.value)}
                                    >
                                        {roleOptions(m.role).map((r) => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
                                    </select>
                                    {removable && (
                                        <button className="btn-sm-cancel" onClick={() => remove(m)}>
                                            {m.userId === currentUserId ? 'Leave' : 'Remove'}
                                        </button>
                                    )}
                                </span>
                            )}
                        </div>
                    );
                })}
                {members.length <= 1 && <div className="share-empty">Nobody else has access yet.</div>}
            </div>
        </div>
    );
}
