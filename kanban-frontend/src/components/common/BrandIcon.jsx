/** The logo: three stacked layers. Used on the login pages and in the left panel. */
export default function BrandIcon({ size = 18, rounded = true }) {
    const roundProps = rounded ? { strokeLinecap: 'round', strokeLinejoin: 'round' } : {};
    return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" {...roundProps}>
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
        </svg>
    );
}
