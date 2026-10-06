package backend.models.task;

import backend.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
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
public class TaskTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "taskTable", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TaskList> taskLists = new ArrayList<>();

    public TaskTable(String name) {
        this.name = name;
        this.taskLists = new ArrayList<>();
    }

    public TaskTable(String name, User user) {
        this.name = name;
        this.user = user;
        this.taskLists = new ArrayList<>();
    }

    public void addTaskList(TaskList taskList) {
        taskLists.add(taskList);
        taskList.setTaskTable(this);
    }

    public void removeTaskList(TaskList taskList) {
        taskLists.remove(taskList);
        taskList.setTaskTable(null);
    }
}
