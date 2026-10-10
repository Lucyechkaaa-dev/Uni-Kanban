package backend.models.user;

import backend.models.task.Task;
import backend.models.task.TaskTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Log4j2

@Entity
@Table(name = "users")
public class User{

	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.UUID)
	@Id
	private UUID id;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	@ToString.Exclude
	private String password;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Task> tasks = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<TaskTable> taskTables = new ArrayList<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role", nullable = false)
	private Set<String> roles = new HashSet<>(Set.of(Role.USER.name()));

	public User(String username, String password){
		this.username = username;
		this.password = password;
		this.tasks = new ArrayList<>();
		this.taskTables = new ArrayList<>();
		this.roles = new HashSet<>(Set.of(Role.USER.name()));
	}

	public User(String username, String password, Role role){
		this(username, password);
		if(role != null) addRole(role);
	}

	public User(String username, String password, Set<String> roles){
		this(username, password);
		if(roles != null) roles.forEach(this::addRole);
	}

	public void addRole(String role){
		if(role != null && !role.isBlank()){
			roles.add(role.trim().toUpperCase());
		}
	}

	public void addRole(Role role){
		if(role != null){
			roles.add(role.name());
		}
	}

	public void removeRole(String role){
		if(role != null){
			roles.remove(role.trim().toUpperCase());
		}
	}

	public void removeRole(Role role){
		if(role != null){
			roles.remove(role.name());
		}
	}

	public boolean hasRole(String role){
		return role != null && roles.contains(role.trim().toUpperCase());
	}

	public boolean hasRole(Role role){
		return role != null && roles.contains(role.name());
	}

	public void addTask(Task task){
		log.info("{} Task set for userID {}", task.getId(), this.id);
		tasks.add(task);
		task.setUser(this);
	}

	public void removeTask(Task task){
		log.info("{} Task removed for userID {}", task.getId(), this.id);
		tasks.remove(task);
		task.setUser(null);
	}

	public void addTaskTable(TaskTable taskTable){
		log.info("{} TaskTable added for userID {}", taskTable.getId(), this.id);
		taskTables.add(taskTable);
		taskTable.setUser(this);
	}

	public void removeTaskTable(TaskTable taskTable){
		log.info("{} TaskTable removed for userID {}", taskTable.getId(), this.id);
		taskTables.remove(taskTable);
		taskTable.setUser(null);
	}
}
