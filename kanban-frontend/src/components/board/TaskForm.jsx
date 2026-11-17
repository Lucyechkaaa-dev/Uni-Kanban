import { AVAILABLE_TAGS, PRIORITIES } from '../../constants';

/** The tag and the priority selects, shared by the "new card" and the "edit card" forms. */
export function TagSelect({ value, onChange }) {
    return (
        <select className="edit-select" title="Tag" aria-label="Tag" value={value} onChange={onChange}>
            {AVAILABLE_TAGS.map((tag) => <option key={tag} value={tag}>{tag}</option>)}
        </select>
    );
}

export function PrioritySelect({ value, onChange }) {
    return (
        <select className="edit-select" title="Priority" aria-label="Priority" value={value} onChange={onChange}>
            {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
        </select>
    );
}
