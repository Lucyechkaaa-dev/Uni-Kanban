import { useState } from 'react';
import { api } from '../api';
import { EMPTY_TASK_FORM } from '../constants';
import { getTodayLocal, isBlank } from '../utils';

/**
 * Everything the user does with cards: drag to another column, edit, delete, create.
 * Each action changes the screen at once (setColumns) and passes the request to send (see useSaveQueue).
 */
export function useTaskActions({ columns, setColumns, send, user }) {
    const [draggedTaskId, setDraggedTaskId] = useState(null);
    const [activeMenuTaskId, setActiveMenuTaskId] = useState(null);
    const [editingTaskId, setEditingTaskId] = useState(null);
    const [addingToColumnId, setAddingToColumnId] = useState(null);

    const [editFormData, setEditFormData] = useState({});
    const [newTaskData, setNewTaskData] = useState(EMPTY_TASK_FORM);

    // ---- drag and drop ----
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
        const sourceColumn = columns.find((col) => col.tasks.some((t) => t.id === movedTaskId));
        setDraggedTaskId(null);
        if (!sourceColumn || sourceColumn.id === targetColumnId) return;

        const movedTask = sourceColumn.tasks.find((t) => t.id === movedTaskId);
        setColumns((cols) => cols.map((col) => {
            if (col.id === sourceColumn.id) return col.withTasks(col.tasks.filter((t) => t.id !== movedTaskId));
            if (col.id === targetColumnId) return col.withTasks([...col.tasks, movedTask]);
            return col;
        }));
        send(() => api.moveTask(movedTaskId, targetColumnId));
    };

    // ---- edit ----
    const startEditing = (task) => {
        setEditingTaskId(task.id);
        setEditFormData({ ...task });
        setActiveMenuTaskId(null); // close the menu with three dots
    };
    const cancelEditing = () => setEditingTaskId(null);
    const saveTaskEdit = () => {
        if (isBlank(editFormData.name)) return;

        const taskId = editingTaskId;
        const form = { ...editFormData, name: editFormData.name.trim() };
        setEditingTaskId(null);
        setColumns((cols) => cols.map((col) => col.withTasks(col.tasks.map((t) => (t.id === taskId
            ? t.with({ name: form.name, comment: form.comment, tag: form.tag, priority: form.priority, date: form.date })
            : t)))));
        send(() => api.updateTask(taskId, form));
    };

    // ---- delete ----
    const deleteTask = (taskId) => {
        setActiveMenuTaskId(null);
        setColumns((cols) => cols.map((col) => col.withTasks(col.tasks.filter((t) => t.id !== taskId))));
        send(() => api.deleteTask(taskId));
    };

    // ---- create (inline form in a column) ----
    const handleSaveNewTask = (columnId) => {
        if (isBlank(newTaskData.name)) return;

        const form = {
            ...newTaskData,
            name: newTaskData.name.trim(),
            comment: newTaskData.comment.trim(),
            date: newTaskData.date || getTodayLocal(),
        };
        setAddingToColumnId(null);
        setNewTaskData(EMPTY_TASK_FORM);
        // the id and the number of a new card are given by the server, so the card is added from its answer
        send(() => api.createTask(columnId, form, user.id), (created) =>
            setColumns((cols) => cols.map((col) => (col.id === columnId ? col.withTasks([...col.tasks, created]) : col))));
    };
    const cancelAdding = () => {
        setAddingToColumnId(null);
        setNewTaskData(EMPTY_TASK_FORM);
    };

    return {
        // state of the forms and menus
        activeMenuTaskId, setActiveMenuTaskId,
        editingTaskId, editFormData, setEditFormData,
        addingToColumnId, setAddingToColumnId,
        newTaskData, setNewTaskData,
        // actions
        handleDragStart, handleDragEnd, handleDragOver, handleDrop,
        startEditing, cancelEditing, saveTaskEdit,
        deleteTask,
        handleSaveNewTask, cancelAdding,
    };
}
