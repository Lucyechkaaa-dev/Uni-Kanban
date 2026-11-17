/** A red message; a click on it hides it. title (optional) is the hint on hover. */
export default function ErrorBanner({ message, onDismiss, title }) {
    if (!message) return null;
    return (
        <div className="board-error" role="alert" title={title} onClick={onDismiss}>
            {message}
        </div>
    );
}
