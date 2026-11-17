import { ROLE } from '../constants';

/** One line of the list of boards in the left panel: my own boards and the boards shared with me. */
export class BoardSummary {
    /**
     * @param {{ id: string, name: string, owned: boolean, role: string, ownerName: string | null }} fields
     */
    constructor({ id, name, owned, role, ownerName }) {
        this.id = id;
        this.name = name;
        this.owned = owned;         // true: I created it
        this.role = role;           // OWNER, or VIEWER / MEMBER / ADMIN for a shared board
        this.ownerName = ownerName; // the name of the owner (only for shared boards)
    }

    /** An item of GET /boards (the boards I own). */
    static fromOwned(dto) {
        return new BoardSummary({ id: dto.id, name: dto.name, owned: true, role: ROLE.OWNER, ownerName: null });
    }

    /** An item of GET /boards/shared. */
    static fromShared(dto) {
        return new BoardSummary({ id: dto.id, name: dto.name, owned: false, role: dto.role, ownerName: dto.ownerUsername });
    }
}
