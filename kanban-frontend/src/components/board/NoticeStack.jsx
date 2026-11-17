/** Short messages in the corner about what other users just did ("Anna changed “Task”"). */
export default function NoticeStack({ notices }) {
    return (
        <div className="notice-stack" aria-live="polite">
            {notices.map((n) => <div key={n.id} className="notice">{n.text}</div>)}
        </div>
    );
}
