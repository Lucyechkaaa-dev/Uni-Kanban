/** One column: the header with the counter and the "+" button; the form and the cards come in as children. */
export default function Column({ column, onDragOver, onDrop, onQuickAdd, children }) {
    return (
        <div className="kanban-column" onDragOver={onDragOver} onDrop={onDrop}>
            <div className="column-header">
                <div className="column-title-wrap">
                    <span className="status-dot" style={{ backgroundColor: column.color }}></span>
                    {column.title} <span className="count-badge">{column.tasks.length}</span>
                </div>
                <div className="column-actions">
                    <button className="icon-btn" title="Quick add" onClick={onQuickAdd}>+</button>
                </div>
            </div>

            <div className="column-cards">{children}</div>
        </div>
    );
}
