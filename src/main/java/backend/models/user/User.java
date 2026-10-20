package backend.models.user;

import backend.models.task.Task;
import backend.models.team.Team;
import backend.models.team.TeamRole;
import backend.models.team.UserTeam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Log4j2
@Entity
@Table(name = "users")
public class User {

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
	private List<Task> tasks;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<UserTeam> userTeams = new ArrayList<>();

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.tasks = new ArrayList<>();
		this.userTeams = new ArrayList<>();
	}

	public void addTask(Task task) {
		log.info("{} Task set for userID {}", task.getId(), this.id);
		tasks.add(task);
		task.setUser(this);
	}

	public void removeTask(Task task) {
		log.info("{} Task removed for userID {}", task.getId(), this.id);
		tasks.remove(task);
		task.setUser(null);
	}

	public void joinTeam(Team team, TeamRole role) {
		UserTeam userTeam = new UserTeam(this, team, role);
		userTeams.add(userTeam);
		if (team.getMembers() != null && !team.getMembers().contains(userTeam)) {
			team.getMembers().add(userTeam);
		}
	}

	public void leaveTeam(Team team) {
		userTeams.removeIf(ut -> ut.getTeam() != null && ut.getTeam().equals(team));
		if (team != null && team.getMembers() != null) {
			team.getMembers().removeIf(ut -> ut.getUser() != null && ut.getUser().equals(this));
		}
	}
}
