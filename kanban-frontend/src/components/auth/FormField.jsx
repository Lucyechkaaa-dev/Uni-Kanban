/** A label with an input (and an optional hint under it). All other props go to the <input>. */
export default function FormField({ id, label, hint, ...inputProps }) {
    return (
        <div className="form-group">
            <label htmlFor={id}>{label}</label>
            <input id={id} {...inputProps} />
            {hint && <div className="form-hint">{hint}</div>}
        </div>
    );
}
