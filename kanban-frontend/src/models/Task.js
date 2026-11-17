import { DEFAULT_PRIORITY, DEFAULT_TAG, PRIORITY_LABEL } from '../constants';
import { initials } from '../utils';

/**
 * A card of the board, in the form the screen uses.
 * The backend calls the same thing TaskDto (title, number, assigneeName ...): see fromApi and toRequest.
 */
export class Task {
    /**
     * @param {{
     *   id: string,
     *   code: string,
     *   priority: 'Low' | 'Medium' | 'High',
     *   name: string,
     *   comment: string,
     *   tag: string,
     *   user: string,
     *   date: string,
     *   commentsCount: number,
     *   columnId?: string,
     *   position?: number
     * }} fields
     */
    constructor({ id, code, priority, name, comment, tag, user, date, commentsCount, columnId, position }) {
        this.id = id;                       // UUID as text
        this.code = code;                   // "TASK-5"
        this.priority = priority;           // Low | Medium | High
        this.name = name;                   // the title
        this.comment = comment;             // the description, "" if none
        this.tag = tag;                     // "#API"
        this.user = user;                   // initials of the assignee
        this.date = date;                   // "2026-11-20" or ""
        this.commentsCount = commentsCount; // the backend has no comments yet, so 0
        this.columnId = columnId;           // the column the server keeps the card in (live updates need it)
        this.position = position;           // the place in the column, the server sorts by it
    }

    /** TaskDto of the backend -> Task. */
    static fromApi(dto) {
        return new Task({
            id: dto.id,
            code: `TASK-${dto.number}`,
            priority: PRIORITY_LABEL[dto.priority] || DEFAULT_PRIORITY,
            name: dto.title,
            comment: dto.description || '',
            tag: dto.tag || DEFAULT_TAG,
            user: initials(dto.assigneeName),
            date: dto.dueDate || '',
            commentsCount: 0,
            columnId: dto.columnId,
            position: dto.position,
        });
    }

    /** The card form of the screen -> the body the backend expects (an empty date must go as null, not as ""). */
    static toRequest(form) {
        return {
            title: form.name,
            description: form.comment || null,
            priority: (form.priority || DEFAULT_PRIORITY).toUpperCase(),
            tag: form.tag || null,
            dueDate: form.date || null,
        };
    }

    /** A copy with some fields changed. The state of React is never changed in place. */
    with(changes) {
        return new Task({ ...this, ...changes });
    }
}
