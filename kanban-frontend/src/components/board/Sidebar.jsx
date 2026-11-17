import { initials } from '../../utils';
import BrandIcon from '../common/BrandIcon';

/** Left panel: the logo, the list of boards (mine and shared with me) and the logged-in user with the logout button. */
export default function Sidebar({ user, boards, boardId, onSelectBoard, onLogout }) {
    const mine = boards.filter((b) => b.owned);
    const shared = boards.filter((b) => !b.owned);

    return (
        <aside className="sidebar">
            <div className="sidebar-top">
                <div className="logo-area">
                    <div className="brand-logo-icon">
                        <BrandIcon size={16} rounded={false} />
                    </div>
                    Uni-Kanban
                </div>
                <div className="nav-item active">
                    <span>🏠</span> Overview
                </div>
                <div className="board-switcher">
                    <div className="user-role">My boards</div>
                    {mine.map((b) => (
                        <div
                            key={b.id}
                            className={`nav-item ${b.id === boardId ? 'active' : ''}`}
                            onClick={() => onSelectBoard(b.id)}
                        >
                            <span>📋</span> {b.name}
                        </div>
                    ))}
                    {shared.length > 0 && <div className="user-role">Shared with me</div>}
                    {shared.map((b) => (
                        <div
                            key={b.id}
                            className={`nav-item ${b.id === boardId ? 'active' : ''}`}
                            onClick={() => onSelectBoard(b.id)}
                            title={`${b.ownerName ?? ''} · ${b.role}`}
                        >
                            <span>👥</span> {b.name}
                        </div>
                    ))}
                </div>
            </div>
            <div className="user-profile">
                <div className="user-info">
                    <div className="avatar">{initials(user.username)}</div>
                    <div>
                        <div className="user-name">{user.username}</div>
                        <div className="user-role">{user.email}</div>
                    </div>
                </div>
                <button className="icon-btn" title="Log out" aria-label="Log out" onClick={onLogout}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                        <polyline points="16 17 21 12 16 7" />
                        <line x1="21" y1="12" x2="9" y2="12" />
                    </svg>
                </button>
            </div>
        </aside>
    );
}
