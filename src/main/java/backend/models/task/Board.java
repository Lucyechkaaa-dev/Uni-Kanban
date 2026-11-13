package backend.models.task;

import backend.models.team.Team;
import backend.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "boards")
public class Board{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "last_task_number", nullable = false)
    @ColumnDefault("0")
    @Setter(AccessLevel.NONE)
    private int lastTaskNumber = 0;

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

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TaskList> taskLists = new ArrayList<>();

    public Board(String name) {
        this(name, (User) null);
    }

    public Board(String name, Team team) {
        this.name = name;
        this.team = team;
        this.taskLists = new ArrayList<>();
    }

    public Board(String name, User user) {
        this.name = name;
        this.user = user;
        this.taskLists = new ArrayList<>();
    }

    public boolean isOwner(UUID userId) {
        return this.user != null && this.user.getId() != null && this.user.getId().equals(userId);
    }

    public boolean isOwner(User user) {
        return user != null && isOwner(user.getId());
    }

    public Board(String name, User user, Team team) {
        this.name = name;
        this.user = user;
        this.team = team;
        this.taskLists = new ArrayList<>();
    }

    public void addTaskList(TaskList taskList) {
        Objects.requireNonNull(taskList, "taskList must not be null");
        Board previous = taskList.getBoard();
        if (previous != null && previous != this) {
            previous.taskLists.removeIf(existing -> existing == taskList);
        }
        if (taskLists.stream().noneMatch(existing -> existing == taskList)) {
            taskLists.add(taskList);
        }
        taskList.setBoard(this);
    }

    public void removeTaskList(TaskList taskList) {
        Objects.requireNonNull(taskList, "taskList must not be null");
        if (taskLists.remove(taskList)) {
            taskList.setBoard(null);
        }
    }

    public void addDefaultColumns() {
        if (!taskLists.isEmpty()) {
            return;
        }
        int position = 0;
        for (DefaultColumns.ColumnSpec spec : DefaultColumns.ALL) {
            addTaskList(new TaskList(spec.name(), this, position++, spec.color()));
        }
    }
}
