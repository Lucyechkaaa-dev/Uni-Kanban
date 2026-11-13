import { useState } from 'react';
import { api } from '../api';

export default function Register({ onSwitchToLogin, onRegisterSuccess }) {
    const [error, setError] = useState('');
    const [busy, setBusy] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        // read the fields BEFORE the first await (after it e.currentTarget is already null
        const form = new FormData(e.currentTarget);
        setBusy(true);
        setError('');
        try {
            const user = await api.register(form.get('username'), form.get('email'), form.get('password'));
            onRegisterSuccess(user);
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <div className="auth-container">
            <div className="brand-header">
                <div className="brand-logo-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <polygon points="12 2 2 7 12 12 22 7 12 2" />
                        <polyline points="2 17 12 22 22 17" />
                        <polyline points="2 12 12 17 22 12" />
                    </svg>
                </div>
                Uni-Kanban
            </div>
            <div className="auth-card">
                <h2>Create your account</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="register-username">Username</label>
                        <input id="register-username" name="username" type="text" autoComplete="username" placeholder="Nick Name" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="register-email">Email</label>
                        <input id="register-email" name="email" type="email" autoComplete="email" placeholder="example@mail.com" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="register-password">Password</label>
                        <input id="register-password" name="password" type="password" autoComplete="new-password" placeholder="Create a password" minLength={8} required />
                        <div className="form-hint">Use at least 8 characters.</div>
                    </div>
                    {error && <div className="form-error" role="alert">{error}</div>}
                    <button type="submit" className="btn-primary" disabled={busy}>Create account</button>
                </form>
            </div>
            <div className="auth-footer">
                Already have an account?
                <button type="button" onClick={onSwitchToLogin}>Sign in</button>
            </div>
        </div>
    );
}
