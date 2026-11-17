import { isBlank } from '../../utils';
import { PrioritySelect, TagSelect } from './TaskForm';

/** The inline form "create a card". data is the form, onChange gets the whole new form. */
export default function NewTaskForm({ data, onChange, onSave, onCancel }) {
    const set = (field) => (e) => onChange({ ...data, [field]: e.target.value });

    return (
        <div
            className="add-task-inline"
            onKeyDown={(e) => { if (e.key === 'Escape') onCancel(); }}
        >
            <input
                type="text"
                placeholder="Title"
                className="edit-input"
                value={data.name}
                onChange={set('name')}
                onKeyDown={(e) => { if (e.key === 'Enter') onSave(); }}
                autoFocus
            />
            <textarea
                placeholder="Comment"
                className="edit-input"
                style={{ marginTop: '6px', resize: 'vertical' }}
                value={data.comment}
                onChange={set('comment')}
            />
            <input
                type="date"
                className="edit-input"
                style={{ marginTop: '6px' }}
                value={data.date}
                onChange={set('date')}
            />
            <div className="form-row">
                <TagSelect value={data.tag} onChange={set('tag')} />
                <PrioritySelect value={data.priority} onChange={set('priority')} />
            </div>
            <div className="edit-actions">
                <button className="btn-sm-cancel" onClick={onCancel}>Cancel</button>
                <button className="btn-sm-save" onClick={onSave} disabled={isBlank(data.name)}>
                    Create
                </button>
            </div>
        </div>
    );
}
