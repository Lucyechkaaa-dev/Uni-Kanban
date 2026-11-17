import { api } from '../api';
import { useAuthForm } from '../hooks/useAuthForm';
import AuthFrame from './auth/AuthFrame';
import FormField from './auth/FormField';

export default function Login({ onSwitchToRegister, onLoginSuccess }) {
    const { error, busy, handleSubmit } = useAuthForm(
        (form) => api.login(form.get('email'), form.get('password')),
        onLoginSuccess
    );

    return (
        <AuthFrame
            title="Login to your account"
            footerText="Doesn't have account yet?"
            footerAction="Register"
            onFooterAction={onSwitchToRegister}
        >
            <form onSubmit={handleSubmit}>
                <FormField id="login-email" label="Email" name="email" type="email" autoComplete="email" placeholder="example@mail.com" required />
                <FormField id="login-password" label="Password" name="password" type="password" autoComplete="current-password" placeholder="password" required />
                {error && <div className="form-error" role="alert">{error}</div>}
                <button type="submit" className="btn-primary" disabled={busy}>Login</button>
            </form>
        </AuthFrame>
    );
}
