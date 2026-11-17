import { useState } from 'react';
import { MESSAGES, ROLE } from '../constants';
import { useBoardData } from '../hooks/useBoardData';
import { useSaveQueue } from '../hooks/useSaveQueue';
import { useBoardEvents } from '../hooks/useBoardEvents';
import { useBoardRename } from '../hooks/useBoardRename';
import { useTaskActions } from '../hooks/useTaskActions';
import ErrorBanner from './common/ErrorBanner';
import Sidebar from './board/Sidebar';
import BoardHeader from './board/BoardHeader';
import Column from './board/Column';
import TaskCard from './board/TaskCard';
import NewTaskForm from './board/NewTaskForm';
import NoticeStack from './board/NoticeStack';
import ShareDialog from './ShareDialog';
import './Live.css';

/**
 * The board page. It only puts the pieces together:
 *   data and server           hooks/ (useBoardData, useSaveQueue, useBoardEvents, useBoardRename, useTaskActions)
 *   constants                 constants.js
 *   data structures           models/
 *   markup of the parts       components/board/
 */
export default function BoardView({ user, theme, toggleTheme, onLogout }) {
    const {
        boards, setBoards, boardId, setBoardId, board, setBoard, setColumns, error, setError, loadBoardList, myRole,
    } = useBoardData();
    const { enqueue, send } = useSaveQueue({ board, setBoard, setError });
    const { link, notices } = useBoardEvents({
        boardId, board, username: user.username, enqueue, setBoard, setBoards, setBoardId, setError,
    });
    const rename = useBoardRename({ board, setBoard, reloadList: loadBoardList, setError });
    const tasks = useTaskActions({ columns: board ? board.columns : [], setColumns, send, user });
    const [sharing, setSharing] = useState(false);

    if (!board) {
        return <div className="auth-container">{error || MESSAGES.LOADING_BOARD}</div>;
    }

    const columns = board.columns;

    return (
        <div className="app-layout" onClick={() => tasks.setActiveMenuTaskId(null)}>
            <Sidebar
                user={user}
                boards={boards}
                boardId={boardId}
                onSelectBoard={setBoardId}
                onLogout={onLogout}
            />

            <main className="main-content">
                <BoardHeader
                    board={board}
                    user={user}
                    theme={theme}
                    toggleTheme={toggleTheme}
                    link={link}
                    rename={rename}
                    onShare={() => setSharing(true)}
                    onAddTask={() => columns[0] && tasks.setAddingToColumnId(columns[0].id)}
                />

                <ErrorBanner message={error} onDismiss={() => setError('')} title="Click to dismiss" />

                <div className="kanban-board">
                    {columns.map((col) => (
                        <Column
                            key={col.id}
                            column={col}
                            onDragOver={tasks.handleDragOver}
                            onDrop={() => tasks.handleDrop(col.id)}
                            onQuickAdd={() => tasks.setAddingToColumnId(col.id)}
                        >
                            {tasks.addingToColumnId === col.id && (
                                <NewTaskForm
                                    data={tasks.newTaskData}
                                    onChange={tasks.setNewTaskData}
                                    onSave={() => tasks.handleSaveNewTask(col.id)}
                                    onCancel={tasks.cancelAdding}
                                />
                            )}

                            {col.tasks.map((task) => (
                                <TaskCard
                                    key={task.id}
                                    task={task}
                                    isEditing={tasks.editingTaskId === task.id}
                                    editData={tasks.editFormData}
                                    onEditChange={tasks.setEditFormData}
                                    onSaveEdit={tasks.saveTaskEdit}
                                    onCancelEdit={tasks.cancelEditing}
                                    isMenuOpen={tasks.activeMenuTaskId === task.id}
                                    onToggleMenu={() => tasks.setActiveMenuTaskId(tasks.activeMenuTaskId === task.id ? null : task.id)}
                                    onStartEdit={() => tasks.startEditing(task)}
                                    onDelete={() => tasks.deleteTask(task.id)}
                                    onDragStart={(e) => tasks.handleDragStart(e, task.id)}
                                    onDragEnd={tasks.handleDragEnd}
                                />
                            ))}
                        </Column>
                    ))}
                </div>
            </main>

            <NoticeStack notices={notices} />

            {sharing && (
                <ShareDialog
                    boardId={board.id}
                    myRole={board.owner ? ROLE.OWNER : myRole}
                    currentUserId={user.id}
                    onClose={() => setSharing(false)}
                    onChanged={() => loadBoardList().catch(() => {})}
                />
            )}
        </div>
    );
}
