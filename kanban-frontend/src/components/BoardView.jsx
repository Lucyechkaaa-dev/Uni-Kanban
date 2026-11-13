import { useState, useEffect, useRef } from 'react';
import { api, initials } from '../api';

const EMPTY_NEW_TASK = { name: '', comment: '', priority: 'Medium', tag: '#Custom', date: '' };

const getTodayLocal = () => {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const isBlank = (value) => !value || !value.trim();

export default function BoardView({ user, theme, toggleTheme, onLogout }) {
    const [board, setBoard] = useState(null);
    const [error, setError] = useState('');
    const columns = board ? board.columns : [];

    const setColumns = (update) =>
        setBoard((current) => (current ? { ...current, columns: update(current.columns) } : current));

      useEffect(() => {
        let cancelled = false;
        api.firstBoard()
            .then((loaded) => { if (!cancelled) setBoard(loaded); })
            .catch((err) => { if (!cancelled) setError(err.message); });
        return () => { cancelled = true; };
    }, []);
      const queue = useRef(Promise.resolve());
    const send = (action, onSaved) => {
        queue.current = queue.current.then(async () => {
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

    const [draggedTaskId, setDraggedTaskId] = useState(null);
    const [activeMenuTaskId, setActiveMenuTaskId] = useState(null);
    const [editingTaskId, setEditingTaskId] = useState(null);
    const [addingToColumnId, setAddingToColumnId] = useState(null);

    const [editFormData, setEditFormData] = useState({});
    const [newTaskData, setNewTaskData] = useState(EMPTY_NEW_TASK);

    //теги, просто наприклад
    const availableTags = ['#Frontend', '#Backend', '#API', '#DevOps', '#Design', '#Custom'];
    const priorities = ['Low', 'Medium', 'High'];

    const handleDragStart = (e, taskId) => {
        e.dataTransfer.setData('application/x-kanban-task', taskId);
        e.dataTransfer.effectAllowed = 'move';
        setDraggedTaskId(taskId);
    };
    const handleDragEnd = () => setDraggedTaskId(null);
    const handleDragOver = (e) => e.preventDefault();
    const handleDrop = (targetColumnId) => {
        if (!draggedTaskId) return;

        const movedTaskId = draggedTaskId;
        const sourceColumn = columns.find(col => col.tasks.some(t => t.id === movedTaskId));
        setDraggedTaskId(null);
        if (!sourceColumn || sourceColumn.id === targetColumnId) return;

        const movedTask = sourceColumn.tasks.find(t => t.id === movedTaskId);
        setColumns(cols => cols.map(col => {
            if (col.id === sourceColumn.id) return { ...col, tasks: col.tasks.filter(t => t.id !== movedTaskId) };
            if (col.id === targetColumnId) return { ...col, tasks: [...col.tasks, movedTask] };
            return col;
        }));
        send(() => api.moveTask(movedTaskId, targetColumnId));
    };

    const startEditing = (task) => {
        setEditingTaskId(task.id);
        setEditFormData({ ...task });
        setActiveMenuTaskId(null); // Закрываем меню 3-х точек
    };
    const saveTaskEdit = () => {
        if (isBlank(editFormData.name)) return;

        const taskId = editingTaskId;
        const form = { ...editFormData, name: editFormData.name.trim() };
        setEditingTaskId(null);
        setColumns(cols => cols.map(col => ({
            ...col,
            tasks: col.tasks.map(t => t.id === taskId
                ? { ...t, name: form.name, comment: form.comment, tag: form.tag, priority: form.priority, date: form.date }
                : t)
        })));
        send(() => api.updateTask(taskId, form));
    };
    const deleteTask = (taskId) => {
        setActiveMenuTaskId(null);
        setColumns(cols => cols.map(col => ({
            ...col,
            tasks: col.tasks.filter(t => t.id !== taskId)
        })));
        send(() => api.deleteTask(taskId));
    };
    const handleSaveNewTask = (columnId) => {      //Inline
        if (isBlank(newTaskData.name)) return;

        const form = {
            ...newTaskData,
            name: newTaskData.name.trim(),
            comment: newTaskData.comment.trim(),
            date: newTaskData.date || getTodayLocal()
        };
        setAddingToColumnId(null);
        setNewTaskData(EMPTY_NEW_TASK);
        //id and number are given by the server, so the card is added from its answer
        send(() => api.createTask(columnId, form, user.id), (created) =>
            setColumns(cols => cols.map(col => col.id === columnId ? { ...col, tasks: [...col.tasks, created] } : col)));
    };
    const cancelAdding = () => {
        setAddingToColumnId(null);
        setNewTaskData(EMPTY_NEW_TASK);
    };

    if (!board) {
        return <div className="auth-container">{error || 'Loading the board…'}</div>;
    }

    return (
        <div className="app-layout" onClick={() => setActiveMenuTaskId(null)}>
            <aside className="sidebar">
                <div className="sidebar-top">
                    <div className="logo-area">
                        <div className="brand-logo-icon">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                <polygon points="12 2 2 7 12 12 22 7 12 2" />
                                <polyline points="2 17 12 22 22 17" />
                                <polyline points="2 12 12 17 22 12" />
                            </svg>
                        </div>
                        Uni-Kanban
                    </div>
                    <div className="nav-item active">
                        <span>🏠</span> Overview
                    </div>
                </div>
                <div className="user-profile">
                    <div className="user-info">
                        <div className="avatar">{initials(user.username)}</div>
                        <div>
                            <div className="user-name">{user.username}</div>
                            <div className="user-role">{user.email}</div>
                        </div>
                    </div>
                    <button className="icon-btn" title="Log out" aria-label="Log out" onClick={onLogout}>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                            <polyline points="16 17 21 12 16 7" />
                            <line x1="21" y1="12" x2="9" y2="12" />
                        </svg>
                    </button>
                </div>
            </aside>

            <main className="main-content">
                <div className="board-header-top">
                    <span>{board.name}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <button className="theme-toggle-btn" onClick={toggleTheme}>
                            {theme === 'dark' ? '☀️' : '🌙'}
                        </button>
                        <div className="avatar" style={{ width: '24px', height: '24px', fontSize: '0.65rem' }}>{initials(user.username)}</div>
                    </div>
                </div>

                <div className="board-body-header">
                    <div className="board-title-row">
                        <h1>{board.name} ⭐</h1>
                        <div className="board-actions">
                            <button className="btn-secondary">🔗 Share</button>
                            <button className="btn-success" onClick={() => columns[0] && setAddingToColumnId(columns[0].id)}>+ Add task</button>
                        </div>
                    </div>
                    <div className="board-comment-text">{board.description}</div>
                </div>

                {error && (
                    <div className="board-error" role="alert" title="Click to dismiss" onClick={() => setError('')}>
                        {error}
                    </div>
                )}

                <div className="kanban-board">
                    {columns.map(col => (
                        <div
                            key={col.id}
                            className="kanban-column"
                            onDragOver={handleDragOver}
                            onDrop={() => handleDrop(col.id)}
                        >
                            <div className="column-header">
                                <div className="column-title-wrap">
                                    <span className="status-dot" style={{ backgroundColor: col.color }}></span>
                                    {col.title} <span className="count-badge">{col.tasks.length}</span>
                                </div>
                                <div className="column-actions">
                                    <button className="icon-btn" title="Quick add" onClick={() => setAddingToColumnId(col.id)}>+</button>
                                </div>
                            </div>

                            <div className="column-cards">
                                 {addingToColumnId === col.id && (
                                    <div
                                        className="add-task-inline"
                                        onKeyDown={(e) => { if (e.key === 'Escape') cancelAdding(); }}
                                    >
                                        <input
                                            type="text"
                                            placeholder="Title"
                                            className="edit-input"
                                            value={newTaskData.name}
                                            onChange={(e) => setNewTaskData({ ...newTaskData, name: e.target.value })}
                                            onKeyDown={(e) => { if (e.key === 'Enter') handleSaveNewTask(col.id); }}
                                            autoFocus
                                        />
                                        <textarea
                                            placeholder="Comment"
                                            className="edit-input"
                                            style={{ marginTop: '6px', resize: 'vertical' }}
                                            value={newTaskData.comment}
                                            onChange={(e) => setNewTaskData({ ...newTaskData, comment: e.target.value })}
                                        />
                                        <input
                                            type="date"
                                            className="edit-input"
                                            style={{ marginTop: '6px' }}
                                            value={newTaskData.date}
                                            onChange={(e) => setNewTaskData({ ...newTaskData, date: e.target.value })}
                                        />
                                     <div className="form-row">
                                            <select
                                                className="edit-select"
                                                title="Tag"
                                                aria-label="Tag"
                                                value={newTaskData.tag}
                                                onChange={(e) => setNewTaskData({ ...newTaskData, tag: e.target.value })}
                                            >
                                                {availableTags.map(tag => <option key={tag} value={tag}>{tag}</option>)}
                                            </select>
                                            <select
                                                className="edit-select"
                                                title="Priority"
                                                aria-label="Priority"
                                                value={newTaskData.priority}
                                                onChange={(e) => setNewTaskData({ ...newTaskData, priority: e.target.value })}
                                            >
                                                {priorities.map(p => <option key={p} value={p}>{p}</option>)}
                                            </select>
                                        </div>
                                        <div className="edit-actions">
                                            <button className="btn-sm-cancel" onClick={cancelAdding}>Cancel</button>
                                            <button
                                                className="btn-sm-save"
                                                onClick={() => handleSaveNewTask(col.id)}
                                                disabled={isBlank(newTaskData.name)}
                                            >
                                                Create
                                            </button>
                                        </div>
                                    </div>
                                )}

                                {col.tasks.map(task => (
                                    <div
                                        key={task.id}
                                        className="kanban-card"
                                        draggable={editingTaskId !== task.id}
                                        onDragStart={(e) => handleDragStart(e, task.id)}
                                        onDragEnd={handleDragEnd}
                                    >
                                        {editingTaskId === task.id ? (
                                            <div
                                                className="edit-card-form"
                                                onKeyDown={(e) => { if (e.key === 'Escape') setEditingTaskId(null); }}
                                            >
                                                <input
                                                    type="text"
                                                    className="edit-input"
                                                    value={editFormData.name}
                                                    onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
                                                    onKeyDown={(e) => { if (e.key === 'Enter') saveTaskEdit(col.id); }}
                                                    placeholder="Title"
                                                />
                                                <textarea
                                                    className="edit-input"
                                                    rows="2"
                                                    value={editFormData.comment}
                                                    onChange={(e) => setEditFormData({ ...editFormData, comment: e.target.value })}
                                                    placeholder="Comment"
                                                />
                                                <input
                                                    type="date"
                                                    className="edit-input"
                                                    value={editFormData.date}
                                                    onChange={(e) => setEditFormData({ ...editFormData, date: e.target.value })}
                                                />
                                                <select
                                                    className="edit-select"
                                                    title="Tag"
                                                    aria-label="Tag"
                                                    value={editFormData.tag}
                                                    onChange={(e) => setEditFormData({ ...editFormData, tag: e.target.value })}
                                                >
                                                    {availableTags.map(tag => <option key={tag} value={tag}>{tag}</option>)}
                                                </select>
                                                <select
                                                    className="edit-select"
                                                    title="Priority"
                                                    aria-label="Priority"
                                                    value={editFormData.priority}
                                                    onChange={(e) => setEditFormData({ ...editFormData, priority: e.target.value })}
                                                >
                                                    {priorities.map(p => <option key={p} value={p}>{p}</option>)}
                                                </select>
                                                <div className="edit-actions">
                                                    <button className="btn-sm-cancel" onClick={() => setEditingTaskId(null)}>Cancel</button>
                                                    <button
                                                        className="btn-sm-save"
                                                        onClick={() => saveTaskEdit(col.id)}
                                                        disabled={isBlank(editFormData.name)}
                                                    >
                                                        Save
                                                    </button>
                                                </div>
                                            </div>
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
                                                        <button
                                                            className="icon-btn"
                                                            onClick={() => setActiveMenuTaskId(activeMenuTaskId === task.id ? null : task.id)}
                                                        >
                                                            ···
                                                        </button>
                                                        {activeMenuTaskId === task.id && (
                                                            <div className="dropdown-menu">
                                                                <button className="dropdown-item" onClick={() => startEditing(task)}>
                                                                    ✏️ Edit
                                                                </button>
                                                                <button className="dropdown-item delete" onClick={() => deleteTask(task.id)}>
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
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
}