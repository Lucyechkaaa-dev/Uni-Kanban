package backend.models.task;

import backend.models.team.Team;
import backend.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "task_tables")
public class Board{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Team team;

    @OneToMany(mappedBy = "taskTable", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TaskList> taskLists = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_table_member_roles", joinColumns = @JoinColumn(name = "task_table_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "role")
    private java.util.Map<UUID, String> memberRoles = new java.util.HashMap<>();

    public Board(String name) {
        this(name, (User) null);
    }

    public Board(String name, Team team) {
        this.name = name;
        this.team = team;
        this.taskLists = new ArrayList<>();
        this.memberRoles = new java.util.HashMap<>();
    }

    public Board(String name, User user) {
        this.name = name;
        this.user = user;
        this.taskLists = new ArrayList<>();
        this.memberRoles = new java.util.HashMap<>();
    }

    public boolean isOwner(UUID userId) {
        return this.user != null && this.user.getId() != null && this.user.getId().equals(userId);
    }

    public boolean isOwner(User user) {
        return user != null && isOwner(user.getId());
    }

    public void assignMemberRole(UUID userId, String role) {
        if (userId != null && role != null && !role.isBlank()) {
            memberRoles.put(userId, role.trim().toUpperCase());
        }
    }

    public void removeMemberRole(UUID userId) {
        if (userId != null) {
            memberRoles.remove(userId);
        }
    }

    public String getMemberRole(UUID userId) {
        return userId != null ? memberRoles.get(userId) : null;
    }

    public Board(String name, User user, Team team) {
        this.name = name;
        this.user = user;
        this.team = team;
        this.taskLists = new ArrayList<>();
        this.memberRoles = new java.util.HashMap<>();
    }

    public void addTaskList(TaskList taskList) {
        taskLists.add(taskList);
        taskList.setBoard(this);
    }

    public void removeTaskList(TaskList taskList) {
        taskLists.remove(taskList);
        taskList.setBoard(null);
    }
}
