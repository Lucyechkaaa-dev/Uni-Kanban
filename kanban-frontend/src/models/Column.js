import { Task } from './Task';

/** A column of the board ("To do", "In progress" ...). Backend: ColumnDto. */
export class Column {
    /** @param {{ id: string, title: string, color: string, tasks: Task[] }} fields */
    constructor({ id, title, color, tasks }) {
        this.id = id;       // UUID as text
        this.title = title;
        this.color = color; // "#3b82f6"
        this.tasks = tasks; // Task[]
    }

    static fromApi(dto) {
        return new Column({
            id: dto.id,
            title: dto.name,
            color: dto.color,
            tasks: dto.tasks.map(Task.fromApi),
        });
    }

    /** A copy of the column with another list of cards. */
    withTasks(tasks) {
        return new Column({ ...this, tasks });
    }
}
