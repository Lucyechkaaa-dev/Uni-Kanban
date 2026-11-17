import { ROLE, ROLE_LABELS } from '../../constants';

/**
 * One line of "People with access". The server enforces the rules; the line only hides what would be refused:
 * only managers change roles, only the owner touches ADMINs, everybody may leave, the owner cannot be changed.
 */
export default function MemberRow({ member, currentUserId, canManage, isOwner, roleOptions, onChangeRole, onRemove }) {
    const isOwnerRow = member.role === ROLE.OWNER;
    const isMe = member.userId === currentUserId;
    const editable = !isOwnerRow && canManage && (isOwner || member.role !== ROLE.ADMIN);
    const removable = !isOwnerRow && (isMe || editable);

    return (
        <div className="share-row">
            <span className="share-name">
                {member.username}
                {isMe && <span className="share-empty">(you)</span>}
            </span>
            {isOwnerRow ? (
                <span className="share-badge">Owner</span>
            ) : (
                <span className="share-controls">
                    <select
                        className="edit-select"
                        aria-label={`Role of ${member.username}`}
                        value={member.role}
                        disabled={!editable}
                        onChange={(e) => onChangeRole(member, e.target.value)}
                    >
                        {roleOptions(member.role).map((r) => <option key={r} value={r}>{ROLE_LABELS[r]}</option>)}
                    </select>
                    {removable && (
                        <button className="btn-sm-cancel" onClick={() => onRemove(member)}>
                            {isMe ? 'Leave' : 'Remove'}
                        </button>
                    )}
                </span>
            )}
        </div>
    );
}
