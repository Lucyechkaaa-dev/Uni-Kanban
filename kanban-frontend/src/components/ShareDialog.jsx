import { useState } from 'react';
import { DEFAULT_GRANT_ROLE, MIN_SEARCH_LENGTH, ROLE, ROLE_LABELS } from '../constants';
import { api } from '../api';
import { useMembers } from '../hooks/useMembers';
import { useUserSearch } from '../hooks/useUserSearch';
import ErrorBanner from './common/ErrorBanner';
import MemberRow from './share/MemberRow';
import './ShareDialog.css';

// The server enforces the rules; the dialog only hides what would be refused.
// Only the owner grants or changes ADMIN.
export default function ShareDialog({ boardId, myRole, currentUserId, onClose, onChanged }) {
    const isOwner = myRole === ROLE.OWNER;
    const canManage = isOwner || myRole === ROLE.ADMIN;
    const [grantRole, setGrantRole] = useState(DEFAULT_GRANT_ROLE);
    const [error, setError] = useState('');
    const { members, reload } = useMembers(boardId, setError);
    const { query, results, changeQuery, reset } = useUserSearch(members, setError);

    const roleOptions = (current) =>
        Object.keys(ROLE_LABELS).filter((r) => isOwner || r !== ROLE.ADMIN || current === ROLE.ADMIN);

    // every change: do it, read the people again, tell the page that the list of boards may have changed
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
        reset();
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

                <ErrorBanner message={error} onDismiss={() => setError('')} />

                {canManage && (
                    <div className="share-search">
                        <div className="form-row">
                            <input
                                type="text"
                                className="edit-input"
                                placeholder="Search users by name"
                                aria-label="Search users"
                                value={query}
                                onChange={(e) => changeQuery(e.target.value)}
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
                {members.map((m) => (
                    <MemberRow
                        key={m.userId}
                        member={m}
                        currentUserId={currentUserId}
                        canManage={canManage}
                        isOwner={isOwner}
                        roleOptions={roleOptions}
                        onChangeRole={changeRole}
                        onRemove={remove}
                    />
                ))}
                {members.length <= 1 && <div className="share-empty">Nobody else has access yet.</div>}
            </div>
        </div>
    );
}
