// The only file that talks to the backend. Components call api.something() and never see fetch or URLs.
// The browser asks its own address (/api/...); Vite forwards that to the backend (see vite.config.js).
//
// The login is a cookie (KANBAN_SESSION): the backend sets it after login, the browser sends it back by itself
// with every request. The cookie is HttpOnly, so this code cannot even read it. That is why there is
// no token to keep or to attach here (and nothing in localStorage that a stolen script could copy).

export class ApiError extends Error {
    constructor(status, message) {
        super(message);
        this.status = status;
    }
}

//401
let onUnauthorized = () => {};
export const setUnauthorizedHandler = (handler) => {
    onUnauthorized = handler;
};

export const initials = (name) => (name ? name.trim().slice(0, 2).toUpperCase() : '?');

async function request(method, path, body) {
    const headers = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';

    let response;
    try {
        response = await fetch(`/api${path}`, {
            method,
            headers,
            credentials: 'same-origin', // send the session cookie (the default, written out on purpose)
            body: body !== undefined ? JSON.stringify(body) : undefined,
        });
    } catch {
        throw new ApiError(0, 'The server is not reachable. Is the backend running?');
    }

    if (response.status === 204) return null;

    const data = await response.json().catch(() => null);
    if (!response.ok) {
        // 401 outside /auth/
        // /auth/* -> a wrong password is also 401
        if (response.status === 401 && !path.startsWith('/auth/')) {
            onUnauthorized();
        }
        throw new ApiError(response.status, (data && data.error) || `Request failed (${response.status})`);
    }
    return data;
}

// backend names <-> frontend names

const PRIORITY_LABEL = { LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High' };

function toUiTask(t) {
    return {
        id: t.id,
        code: `TASK-${t.number}`,
        priority: PRIORITY_LABEL[t.priority] || 'Medium',
        name: t.title,
        comment: t.description || '',
        tag: t.tag || '#Custom',
        user: initials(t.assigneeName),
        date: t.dueDate || '',
        commentsCount: 0,
    };
}

function toUiBoard(b) {
    return {
        id: b.id,
        name: b.name,
        description: b.description || '',
        access: b.access,
        columns: b.columns.map((c) => ({
            id: c.id,
            title: c.name,
            color: c.color,
            tasks: c.tasks.map(toUiTask),
        })),
    };
}

const toApiTask = (form) => ({
    title: form.name,
    description: form.comment || null,
    priority: (form.priority || 'Medium').toUpperCase(),
    tag: form.tag || null,
    dueDate: form.date || null,
});

export const api = {
    //cookie
    register: (username, email, password) => request('POST', '/auth/register', { username, email, password }),

    login: (email, password) => request('POST', '/auth/login', { email, password }),

    logout: () => request('POST', '/auth/logout'),

    //who am I: resolves with the user if the browser has a valid session, rejects with 401 if not
    me: () => request('GET', '/auth/me'),

    //the first board of the user made at registration
    async firstBoard() {
        const boards = await request('GET', '/boards');
        if (boards.length === 0) throw new ApiError(404, 'You have no boards yet');
        return toUiBoard(await request('GET', `/boards/${boards[0].id}`));
    },

    async board(id) {
        return toUiBoard(await request('GET', `/boards/${id}`));
    },

    async createTask(columnId, form, assigneeId) {
        return toUiTask(await request('POST', `/columns/${columnId}/tasks`, { ...toApiTask(form), assigneeId }));
    },

    updateTask: (id, form) => request('PUT', `/tasks/${id}`, toApiTask(form)),

    moveTask: (id, columnId) => request('PUT', `/tasks/${id}/move`, { columnId }),

    deleteTask: (id) => request('DELETE', `/tasks/${id}`),
};
