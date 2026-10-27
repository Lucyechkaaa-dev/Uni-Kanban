package backend.models.team;

import backend.models.task.Board;
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
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UserTeam> members = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Board> tables = new ArrayList<>();

    public Team(String name) {
        this.name = name;
        this.members = new ArrayList<>();
        this.tables = new ArrayList<>();
    }

    public Team(String name, String description) {
        this(name, description, null);
    }

    public Team(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.tables = new ArrayList<>();
    }

    public Team(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.tables = new ArrayList<>();
    }

    public void addMember(User user, TeamRole role) {
        UserTeam userTeam = new UserTeam(user, this, role);
        members.add(userTeam);
    }

    public void removeMember(User user) {
        members.removeIf(m -> m.getUser() != null && m.getUser().equals(user));
    }

    public void addTable(Board table) {
        tables.add(table);
        table.setTeam(this);
    }

    public void removeTable(Board table) {
        tables.remove(table);
        table.setTeam(null);
    }
}
