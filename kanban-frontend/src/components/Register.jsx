export default function Register({ onSwitchToLogin, onRegisterSuccess }) {
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
                <form onSubmit={(e) => { e.preventDefault(); onRegisterSuccess(); }}>
                    <div className="form-group">
                        <label htmlFor="register-email">Email</label>
                        <input id="register-email" name="email" type="email" autoComplete="email" placeholder="example@mail.com" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="register-password">Password</label>
                        <input id="register-password" name="password" type="password" autoComplete="new-password" placeholder="Create a password" required />
                        <div className="form-hint">Use at least 8 characters.</div>
                    </div>
                    <button type="submit" className="btn-primary">Create account</button>
                </form>
            </div>
            <div className="auth-footer">
                Already have an account?
                <button type="button" onClick={onSwitchToLogin}>Sign in</button>
            </div>
        </div>
    );
}