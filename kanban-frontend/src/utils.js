// Small helper functions without state. Constants are in constants.js.

/** "Nick Name" -> "NI": the letters in an avatar. */
export const initials = (name) => (name ? name.trim().slice(0, 2).toUpperCase() : '?');

/** true for null, undefined, "" and "   ". */
export const isBlank = (value) => !value || !value.trim();

/** Today as "2026-11-16" in the local time zone (the format of <input type="date">). */
export const getTodayLocal = () => {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

/** A unique key for a short message on the screen. */
export const makeId = () => `${Date.now()}-${Math.random()}`;
