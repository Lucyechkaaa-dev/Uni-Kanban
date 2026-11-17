import { useState } from 'react';

/**
 * The same behaviour for the login and the register form: read the fields, call the backend,
 * show its error under the form, keep the button disabled while waiting.
 *
 * submit(formData) does the request and returns the user; onSuccess(user) goes to the board.
 */
export function useAuthForm(submit, onSuccess) {
    const [error, setError] = useState('');
    const [busy, setBusy] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        // read the fields BEFORE the first await (after it e.currentTarget is already null)
        const form = new FormData(e.currentTarget);
        setBusy(true);
        setError('');
        try {
            onSuccess(await submit(form));
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    return { error, busy, handleSubmit };
}
