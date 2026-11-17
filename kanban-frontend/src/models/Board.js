import { ACCESS } from '../constants';
import { Column } from './Column';

/** A board with its columns and cards. Backend: BoardDto. */
export class Board {
    /**
     * @param {{
     *   id: string,
     *   name: string,
     *   description: string,
     *   access: 'NONE' | 'VIEW' | 'EDIT' | 'MANAGE',
     *   owner: boolean,
     *   columns: Column[]
     * }} fields
     */
    constructor({ id, name, description, access, owner, columns }) {
        this.id = id;                   // UUID as text
        this.name = name;
        this.description = description; // "" if none
        this.access = access;           // what this user may do here
        this.owner = owner;             // true if this user created the board
        this.columns = columns;         // Column[]
    }

    static fromApi(dto) {
        return new Board({
            id: dto.id,
            name: dto.name,
            description: dto.description || '',
            access: dto.access,
            owner: dto.owner,
            columns: dto.columns.map(Column.fromApi),
        });
    }

    /** A copy with some fields changed. */
    with(changes) {
        return new Board({ ...this, ...changes });
    }

    withColumns(columns) {
        return this.with({ columns });
    }

    /** Rename, share and manage members are for those with the MANAGE access. */
    get canManage() {
        return this.access === ACCESS.MANAGE;
    }

    /** Everybody except the read-only viewers can add cards. */
    get canAddTasks() {
        return this.access !== ACCESS.VIEW;
    }

    hasColumn(columnId) {
        return this.columns.some((col) => col.id === columnId);
    }

    /**
     * Puts one card where the server says it is (its column, sorted by the position of the server).
     * card === null removes the card. Returns a new board.
     */
    withCardPlaced(taskId, card) {
        const stripped = this.columns.map((col) => col.withTasks(col.tasks.filter((t) => t.id !== taskId)));
        if (!card) return this.withColumns(stripped);
        return this.withColumns(stripped.map((col) => (col.id !== card.columnId ? col : col.withTasks(
            [...col.tasks, card].sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
        ))));
    }
}
