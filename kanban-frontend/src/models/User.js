/**
 * A person. The same fields as UserDto on the backend.
 * The search in the Share dialog returns only id and username, so email may be missing there.
 */
export class User {
    /** @param {{ id: string, username: string, email?: string }} fields */
    constructor({ id, username, email }) {
        this.id = id;             // UUID as text
        this.username = username;
        this.email = email;
    }

    /** @param {{ id: string, username: string, email?: string }} dto the JSON of the backend */
    static fromApi(dto) {
        return new User(dto);
    }
}
