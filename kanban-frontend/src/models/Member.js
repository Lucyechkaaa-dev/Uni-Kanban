/** A person with access to a board, as the Share dialog lists them. */
export class Member {
    /** @param {{ userId: string, username: string, role: string }} fields */
    constructor({ userId, username, role }) {
        this.userId = userId;     // UUID as text
        this.username = username;
        this.role = role;         // OWNER, ADMIN, MEMBER or VIEWER
    }

    static fromApi(dto) {
        return new Member({ userId: dto.userId, username: dto.username, role: dto.role });
    }
}
