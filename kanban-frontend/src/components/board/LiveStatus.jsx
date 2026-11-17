import { LINK_LABEL } from '../../constants';

/** The small dot "Live / Connecting… / Reconnecting…" in the top bar. */
export default function LiveStatus({ link }) {
    return (
        <span className={`live-status live-${link}`} title="Changes from other users appear here live">
            <span className="live-dot" aria-hidden="true"></span>
            {LINK_LABEL[link]}
        </span>
    );
}
