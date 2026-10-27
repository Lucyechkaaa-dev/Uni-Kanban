package backend.models.task;

import backend.models.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tasks")
public class Task{

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@EqualsAndHashCode.Include
	private UUID id;

	@Column(nullable = false, updatable = false, name = "created_date")
	private long createdDate = LocalDate.now().toEpochDay();

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private int position = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userOwner_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User userOwner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "task_list_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private TaskList taskListParent;

	public Task(String title){
		this(title, null, null, null);
	}

	public Task(String title, String description){
		this(title, description, null, null);
	}

	public Task(String title, TaskList taskListParent){
		this(title, null, null, taskListParent);
	}

	public Task(String title, String description, TaskList taskListParent){
		this(title, description, null, taskListParent);
	}

	public Task(String title, String description, User userOwner, TaskList taskListParent){
		this.title = title;
		this.description = description;
		this.userOwner = userOwner;
		this.taskListParent = taskListParent;
	}
}
