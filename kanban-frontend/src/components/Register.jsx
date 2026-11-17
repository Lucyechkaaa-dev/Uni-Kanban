import { api } from '../api';
import { useAuthForm } from '../hooks/useAuthForm';
import AuthFrame from './auth/AuthFrame';
import FormField from './auth/FormField';

export default function Register({ onSwitchToLogin, onRegisterSuccess }) {
    const { error, busy, handleSubmit } = useAuthForm(
        (form) => api.register(form.get('username'), form.get('email'), form.get('password')),
        onRegisterSuccess
    );

    return (
        <AuthFrame
            title="Create your account"
            footerText="Already have an account?"
            footerAction="Sign in"
            onFooterAction={onSwitchToLogin}
        >
            <form onSubmit={handleSubmit}>
                <FormField id="register-username" label="Username" name="username" type="text" autoComplete="username" placeholder="Nick Name" required />
                <FormField id="register-email" label="Email" name="email" type="email" autoComplete="email" placeholder="example@mail.com" required />
                <FormField
                    id="register-password"
                    label="Password"
                    name="password"
                    type="password"
                    autoComplete="new-password"
                    placeholder="Create a password"
                    minLength={8}
                    hint="Use at least 8 characters."
                    required
                />
                {error && <div className="form-error" role="alert">{error}</div>}
                <button type="submit" className="btn-primary" disabled={busy}>Create account</button>
            </form>
        </AuthFrame>
    );
}
