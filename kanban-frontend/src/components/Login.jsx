export default function Login({ onSwitchToRegister, onLoginSuccess }) {
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
                <h2>Login to your account</h2>
                <form onSubmit={(e) => { e.preventDefault(); onLoginSuccess(); }}>
                    <div className="form-group">
                        <label htmlFor="login-email">Email</label>
                        <input id="login-email" name="email" type="email" autoComplete="email" placeholder="example@mail.com" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="login-password">Password</label>
                        <input id="login-password" name="password" type="password" autoComplete="current-password" placeholder="password" required />
                    </div>
                    <button type="submit" className="btn-primary">Login</button>
                </form>
            </div>
            <div className="auth-footer">
                Doesn't have account yet?
                <button type="button" onClick={onSwitchToRegister}>Register</button>
            </div>
        </div>
    );
}