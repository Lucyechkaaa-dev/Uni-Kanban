import TaskEditForm from './TaskEditForm';

/**
 * One card. While isEditing it shows the edit form instead of the text.
 * Everything it can do (drag, edit, delete) comes in as functions: the card itself keeps no state.
 */
export default function TaskCard({
    task,
    isEditing,
    editData,
    onEditChange,
    onSaveEdit,
    onCancelEdit,
    isMenuOpen,
    onToggleMenu,
    onStartEdit,
    onDelete,
    onDragStart,
    onDragEnd,
}) {
    return (
        <div
            className="kanban-card"
            draggable={!isEditing}
            onDragStart={onDragStart}
            onDragEnd={onDragEnd}
        >
            {isEditing ? (
                <TaskEditForm data={editData} onChange={onEditChange} onSave={onSaveEdit} onCancel={onCancelEdit} />
            ) : (
                <>
                    <div className="card-top">
                        <div className="card-top-left">
                            <span>{task.code}</span>
                            {task.priority && (
                                <span className="priority-badge">
                                    <span className={`priority-dot priority-dot-${task.priority.toLowerCase()}`}></span>
                                    {task.priority}
                                </span>
                            )}
                        </div>
                        <div className="card-menu-container" onClick={(e) => e.stopPropagation()}>
                            <button className="icon-btn" onClick={onToggleMenu}>
                                ···
                            </button>
                            {isMenuOpen && (
                                <div className="dropdown-menu">
                                    <button className="dropdown-item" onClick={onStartEdit}>
                                        ✏️ Edit
                                    </button>
                                    <button className="dropdown-item delete" onClick={onDelete}>
                                        🗑️ Delete
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                    <div className="card-title">{task.name}</div>
                    {task.comment && <div className="card-comment">{task.comment}</div>}
                    <div className="card-tags">
                        <span className="custom-tag">{task.tag}</span>
                    </div>
                    <div className="card-footer">
                        <div className="card-footer-left">
                            <div className="avatar" style={{ width: '18px', height: '18px', fontSize: '0.58rem' }}>{task.user}</div>
                            <span>📅 {task.date}</span>
                        </div>
                        <span>💬 {task.commentsCount}</span>
                    </div>
                </>
            )}
        </div>
    );
}
