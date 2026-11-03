package backend.models.task;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "task_lists")
public class TaskList{

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	@ColumnDefault("0")
	private int position = 0;

	@Column(nullable = false, length = 7)
	@ColumnDefault("'#94a3b8'")
	private String color = "#94a3b8";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_table_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Board board;

	@OneToMany(mappedBy = "taskListParent", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("position ASC, id ASC")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Task> tasks = new ArrayList<>();

	public TaskList(String name){
		this(name, null);
	}

	public TaskList(String name, Board board){
		this.name = name;
		this.board = board;
		this.tasks = new ArrayList<>();
	}
	public TaskList(String name, Board board, int position, String color){
		this(name, board);
		this.position = position;
		if(color != null) this.color = color;
	}

	public void addTask(Task task){
		tasks.add(task);
		task.setTaskListParent(this);
	}

	public void removeTask(Task task){
		tasks.remove(task);
		task.setTaskListParent(null);
	}
}
