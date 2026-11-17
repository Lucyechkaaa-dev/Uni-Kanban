// Every constant of the frontend lives here. Only values, no logic:
// components, hooks, models and api.js import what they need from this file.

// ---- backend and browser ----
export const API_BASE = '/api';
export const THEME_STORAGE_KEY = 'kanban-theme';

export const HTTP = Object.freeze({
    NO_CONTENT: 204,
    UNAUTHORIZED: 401,
    NOT_FOUND: 404,
});

// ---- screens of the application ----
export const VIEW = Object.freeze({
    LOADING: 'loading',
    LOGIN: 'login',
    REGISTER: 'register',
    BOARD: 'board',
});

export const THEME = Object.freeze({
    LIGHT: 'light',
    DARK: 'dark',
});

// ---- cards ----
// the backend sends LOW / MEDIUM / HIGH, the screen shows Low / Medium / High
export const PRIORITIES = Object.freeze(['Low', 'Medium', 'High']);
export const PRIORITY_LABEL = Object.freeze({ LOW: 'Low', MEDIUM: 'Medium', HIGH: 'High' });
export const DEFAULT_PRIORITY = 'Medium';

// tags, just as an example
export const AVAILABLE_TAGS = Object.freeze(['#Frontend', '#Backend', '#API', '#DevOps', '#Design', '#Custom']);
export const DEFAULT_TAG = '#Custom';

// an empty card form: the "new card" form starts from it
export const EMPTY_TASK_FORM = Object.freeze({
    name: '',
    comment: '',
    priority: DEFAULT_PRIORITY,
    tag: DEFAULT_TAG,
    date: '',
});

// ---- access to a board ----
// what the backend says about the current user on the open board (BoardDto.access)
export const ACCESS = Object.freeze({
    NONE: 'NONE',
    VIEW: 'VIEW',
    EDIT: 'EDIT',
    MANAGE: 'MANAGE',
});

// the role of a person on a board; OWNER is the creator, the rest are given in the Share dialog
export const ROLE = Object.freeze({
    OWNER: 'OWNER',
    ADMIN: 'ADMIN',
    MEMBER: 'MEMBER',
    VIEWER: 'VIEWER',
});

// roles that can be given to somebody, with what they allow (the Share dialog shows these texts)
export const ROLE_LABELS = Object.freeze({
    [ROLE.VIEWER]: 'Viewer (read only)',
    [ROLE.MEMBER]: 'Editor (works on cards)',
    [ROLE.ADMIN]: 'Admin (manages members)',
});
export const DEFAULT_GRANT_ROLE = ROLE.MEMBER;

// ---- Share dialog ----
export const SEARCH_DELAY_MS = 300;
export const MIN_SEARCH_LENGTH = 2;

// ---- live updates (Server-Sent Events) ----
export const LINK = Object.freeze({
    CONNECTING: 'connecting',
    LIVE: 'live',
    RECONNECTING: 'reconnecting',
});
export const LINK_LABEL = Object.freeze({
    [LINK.CONNECTING]: 'Connecting…',
    [LINK.LIVE]: 'Live',
    [LINK.RECONNECTING]: 'Reconnecting…',
});
export const EVENT_CHANGE = 'change';    // the name of the event the backend sends
export const CHANGE_KIND_TASK = 'task';  // "one card changed"; any other kind means "reload the board"

export const NOTICE_LIFETIME_MS = 4000;  // how long a message about another user stays on the screen
export const NOTICES_KEPT = 3;           // older messages are dropped when a new one comes

// ---- texts the logic shows (not the labels of the markup) ----
export const MESSAGES = Object.freeze({
    SERVER_UNREACHABLE: 'The server is not reachable. Is the backend running?',
    NO_BOARDS: 'You have no boards yet',
    BOARD_UNAVAILABLE: 'This board is no longer available to you',
    LOADING_BOARD: 'Loading the board…',
});
