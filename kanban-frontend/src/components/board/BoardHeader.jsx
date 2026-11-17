import { initials } from '../../utils';
import LiveStatus from './LiveStatus';

/**
 * The top bar (board name, theme switch, connection state, avatar) and the title row:
 * the title with the rename pencil and the buttons Share and Add task.
 * rename is what useBoardRename returns.
 */
export default function BoardHeader({ board, user, theme, toggleTheme, link, rename, onShare, onAddTask }) {
    const { renaming, nameDraft, setNameDraft, startRename, saveName, cancelRename } = rename;

    return (
        <>
            <div className="board-header-top">
                <span>{board.name}</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <button className="theme-toggle-btn" onClick={toggleTheme}>
                        {theme === 'dark' ? '☀️' : '🌙'}
                    </button>
                    <LiveStatus link={link} />
                    <div className="avatar" style={{ width: '24px', height: '24px', fontSize: '0.65rem' }}>{initials(user.username)}</div>
                </div>
            </div>

            <div className="board-body-header">
                <div className="board-title-row">
                    {renaming ? (
                        <input
                            className="edit-input"
                            aria-label="Board name"
                            value={nameDraft}
                            autoFocus
                            style={{ fontSize: 'inherit', width: 'min(420px, 100%)' }}
                            onChange={(e) => setNameDraft(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') saveName();
                                if (e.key === 'Escape') cancelRename();
                            }}
                            onBlur={saveName}
                        />
                    ) : (
                        <h1>
                            {board.name} ⭐
                            {board.canManage && (
                                <button
                                    className="icon-btn"
                                    title="Rename board"
                                    aria-label="Rename board"
                                    style={{ marginLeft: '8px' }}
                                    onClick={startRename}
                                >
                                    ✏️
                                </button>
                            )}
                        </h1>
                    )}
                    <div className="board-actions">
                        {board.canManage && (
                            <button className="btn-secondary" onClick={onShare}>🔗 Share</button>
                        )}
                        {board.canAddTasks && (
                            <button className="btn-success" onClick={onAddTask}>+ Add task</button>
                        )}
                    </div>
                </div>
                <div className="board-comment-text">{board.description}</div>
            </div>
        </>
    );
}
