package backend.models.task;

import backend.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int position = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_list_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TaskList taskList;

    public Task(String title) {
        this(title, null);
    }

    public Task(String title, TaskList taskList) {
        this(title, null, null, taskList);
    }

    public Task(String title, String description, User user, TaskList taskList) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.taskList = taskList;
    }
}
