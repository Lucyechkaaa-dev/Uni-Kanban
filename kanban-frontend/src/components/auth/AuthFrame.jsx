import BrandIcon from '../common/BrandIcon';

/**
 * The template of the login and register pages: the brand on top, a card with a title and the form
 * (children), and a line with a switch to the other page at the bottom.
 */
export default function AuthFrame({ title, footerText, footerAction, onFooterAction, children }) {
    return (
        <div className="auth-container">
            <div className="brand-header">
                <div className="brand-logo-icon">
                    <BrandIcon />
                </div>
                Uni-Kanban
            </div>
            <div className="auth-card">
                <h2>{title}</h2>
                {children}
            </div>
            <div className="auth-footer">
                {footerText}
                <button type="button" onClick={onFooterAction}>{footerAction}</button>
            </div>
        </div>
    );
}
