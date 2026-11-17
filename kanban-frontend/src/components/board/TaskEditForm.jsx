import { isBlank } from '../../utils';
import { PrioritySelect, TagSelect } from './TaskForm';

/** The form that replaces a card while it is being edited. */
export default function TaskEditForm({ data, onChange, onSave, onCancel }) {
    const set = (field) => (e) => onChange({ ...data, [field]: e.target.value });

    return (
        <div
            className="edit-card-form"
            onKeyDown={(e) => { if (e.key === 'Escape') onCancel(); }}
        >
            <input
                type="text"
                className="edit-input"
                value={data.name}
                onChange={set('name')}
                onKeyDown={(e) => { if (e.key === 'Enter') onSave(); }}
                placeholder="Title"
            />
            <textarea
                className="edit-input"
                rows="2"
                value={data.comment}
                onChange={set('comment')}
                placeholder="Comment"
            />
            <input type="date" className="edit-input" value={data.date} onChange={set('date')} />
            <TagSelect value={data.tag} onChange={set('tag')} />
            <PrioritySelect value={data.priority} onChange={set('priority')} />
            <div className="edit-actions">
                <button className="btn-sm-cancel" onClick={onCancel}>Cancel</button>
                <button className="btn-sm-save" onClick={onSave} disabled={isBlank(data.name)}>
                    Save
                </button>
            </div>
        </div>
    );
}
