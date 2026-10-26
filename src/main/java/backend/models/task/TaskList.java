package backend.models.task;

import jakarta.persistence.*;
import lombok.*;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_table_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Board board;

	@OneToMany(mappedBy = "taskListParent", cascade = CascadeType.ALL, orphanRemoval = true)
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

	public void addTask(Task task){
		tasks.add(task);
		task.setTaskListParent(this);
	}

	public void removeTask(Task task){
		tasks.remove(task);
		task.setTaskListParent(null);
	}
}
