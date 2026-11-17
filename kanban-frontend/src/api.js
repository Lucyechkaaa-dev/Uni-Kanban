// The only file that talks to the backend. Components and hooks call api.something() and never see fetch or URLs.
// The browser asks its own address (/api/...); Vite forwards that to the backend (see vite.config.js).
//
// The login is a cookie (KANBAN_SESSION): the backend sets it after login, the browser sends it back by itself
// with every request. The cookie is HttpOnly, so this code cannot even read it. That is why there is
// no token to keep or to attach here (and nothing in localStorage that a stolen script could copy).
//
// The structures of the data (User, Task, Column, Board ...) are classes in ./models; they also translate
// the names of the backend (title, number ...) into the names the screen uses (name, code ...).

import { API_BASE, HTTP, MESSAGES } from './constants';
import { Board, BoardSummary, Member, Task, User } from './models';

export class ApiError extends Error {
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}

// App.jsx sets this: called when the backend says "401" in the middle of the work
let onUnauthorized = () => {};
export const setUnauthorizedHandler = (handler) => {
    onUnauthorized = handler;
};

async function request(method, path, body) {
    const headers = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';

    let response;
    try {
        response = await fetch(`${API_BASE}${path}`, {
            method,
            headers,
            credentials: 'same-origin', // send the session cookie (the default, written out on purpose)
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch {
        throw new ApiError(0, MESSAGES.SERVER_UNREACHABLE);
    }

    if (response.status === HTTP.NO_CONTENT) return null;

    const data = await response.json().catch(() => null);
    if (!response.ok) {
        // 401 outside /auth/ means "the login is gone": back to the login page.
        // /auth/* is excluded: a wrong password is also 401
        if (response.status === HTTP.UNAUTHORIZED && !path.startsWith('/auth/')) {
            onUnauthorized();
        }
        throw new ApiError(response.status, (data && data.error) || `Request failed (${response.status})`);
    }
    return data;
}

export const api = {
    // register and login answer with the user and set the login cookie
    register: async (username, email, password) =>
        User.fromApi(await request('POST', '/auth/register', { username, email, password })),

    login: async (email, password) => User.fromApi(await request('POST', '/auth/login', { email, password })),

    logout: () => request('POST', '/auth/logout'),

    // who am I: resolves with the user if the browser has a valid session, rejects with 401 if not
    me: async () => User.fromApi(await request('GET', '/auth/me')),

    // the boards the user owns, then the boards shared with the user (role = VIEWER, MEMBER or ADMIN)
    async boardList() {
        const [mine, shared] = await Promise.all([request('GET', '/boards'), request('GET', '/boards/shared')]);
        return [...mine.map(BoardSummary.fromOwned), ...shared.map(BoardSummary.fromShared)];
    },

    async board(id) {
        return Board.fromApi(await request('GET', `/boards/${id}`));
    },

    renameBoard: (id, name) => request('PUT', `/boards/${id}`, { name }),

    // one card as the server stores it now (for live updates)
    card: async (id) => Task.fromApi(await request('GET', `/tasks/${id}`)),

    // live updates: a stream that says "change" whenever the board changed (see BoardEventsController)
    eventsUrl: (id) => `${API_BASE}/boards/${id}/events`,

    // sharing: who has access to a board, and who can be added
    async members(boardId) {
        return (await request('GET', `/boards/${boardId}/members`)).map(Member.fromApi);
    },

    addMember: (boardId, userId, role) => request('POST', `/boards/${boardId}/members`, { userId, role }),

    changeMemberRole: (boardId, userId, role) => request('PUT', `/boards/${boardId}/members/${userId}`, { role }),

    removeMember: (boardId, userId) => request('DELETE', `/boards/${boardId}/members/${userId}`),

    async searchUsers(query) {
        return (await request('GET', `/users/search?q=${encodeURIComponent(query)}`)).map(User.fromApi);
    },

    // the number (TASK-5) and the id are given by the server, so a new card is shown from the answer
    async createTask(columnId, form, assigneeId) {
        return Task.fromApi(await request('POST', `/columns/${columnId}/tasks`, { ...Task.toRequest(form), assigneeId }));
    },

    updateTask: (id, form) => request('PUT', `/tasks/${id}`, Task.toRequest(form)),

    moveTask: (id, columnId) => request('PUT', `/tasks/${id}/move`, { columnId }),

    deleteTask: (id) => request('DELETE', `/tasks/${id}`),
};
