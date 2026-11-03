package backend.utils;

import backend.dao.TaskDao;
import backend.dao.TaskListDao;
import backend.dao.TaskTableDao;
import backend.dao.TeamDao;
import backend.dao.UserDao;
import backend.dao.UserTeamDao;
import backend.models.task.Board;
import backend.models.task.Priority;
import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.team.TeamRole;
import backend.models.user.User;
import backend.services.TaskService;
import backend.services.impl.TaskServiceImpl;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MappingSmokeTest {

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void startDatabase() {
        Configuration cfg = new Configuration().configure("hibernate.cfg.xml");
        cfg.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:h2:mem:kanban_test;DB_CLOSE_DELAY=-1");
        cfg.setProperty("hibernate.connection.username", "sa");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.getProperties().remove("hibernate.default_schema");
        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        cfg.setProperty("hibernate.show_sql", "false");
        cfg.setProperty("hibernate.format_sql", "false");
        cfg.setProperty("hibernate.highlight_sql", "false");
        sessionFactory = cfg.buildSessionFactory();
        HibernateFactory.setSessionFactoryForTest(sessionFactory);
    }

    @AfterAll
    static void stopDatabase() {
        HibernateFactory.setSessionFactoryForTest(null);
        if (sessionFactory != null) sessionFactory.close();
    }

    private static User newUser(String prefix) {
        User user = new User(prefix + "-" + UUID.randomUUID(), "secret123");
        new UserDao().save(user);
        return user;
    }

    /** A board with the four default columns, saved. */
    private static Board newBoard(User owner) {
        Board board = new Board("Main", owner);
        board.addDefaultColumns();
        new TaskTableDao().save(board);
        return board;
    }

    @Test
    @DisplayName("All six entities are registered in hibernate.cfg.xml")
    void allEntitiesAreMapped() {
        Set<String> names = sessionFactory.getMetamodel().getEntities().stream()
                .map(EntityType::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("User", "Task", "Board", "TaskList", "Team", "UserTeam"), names);
    }

    @Test
    @DisplayName("Every DAO query is valid HQL for the current mapping")
    void everyDaoQueryParses() {
        UUID id = UUID.randomUUID();
        UserDao users = new UserDao();
        TaskTableDao boards = new TaskTableDao();
        TaskListDao lists = new TaskListDao();
        TaskDao tasks = new TaskDao();

        assertDoesNotThrow(() -> {
            users.findByUsername("nobody");
            users.findById(id);
            users.searchByUsername("no");
            boards.findById(id);
            boards.findAll();
            boards.findByUserId(id);
            lists.findById(id);
            lists.findAll();
            lists.findByTableId(id);
            lists.nextPositionInBoard(id);
            tasks.findById(id);
            tasks.findAll();
            tasks.findByUserId(id);
            tasks.findByTaskListId(id);
            tasks.searchByTitle("x");
            tasks.nextPositionInList(id);
            new TeamDao().findById(id);
            new TeamDao().findByName("x");
            new TeamDao().searchByName("x");
            new TeamDao().findAll();
            UserTeamDao userTeams = new UserTeamDao();
            userTeams.findById(id);
            userTeams.findByUserAndTeam(id, id);
            userTeams.findByUserId(id);
            userTeams.findByTeamId(id);
            userTeams.findByTeamIdAndRole(id, TeamRole.MEMBER);
            userTeams.findAll();
        });
    }

    @Test
    @DisplayName("A board is saved with four ordered columns, and the card gets number, position and priority")
    void boardColumnsAndCardRoundTrip() {
        User owner = newUser("owner");
        Board board = newBoard(owner);

        List<TaskList> lists = new TaskListDao().findByTableId(board.getId());
        assertEquals(List.of("To-do", "In progress", "In review", "Done"),
                lists.stream().map(TaskList::getName).toList());
        assertEquals(List.of(0, 1, 2, 3), lists.stream().map(TaskList::getPosition).toList());
        assertEquals("#94a3b8", lists.get(0).getColor());

        TaskService service = new TaskServiceImpl();
        Task first = service.createTask("first", "details", owner.getId(), lists.get(0).getId());
        Task second = service.createTask("second", null, owner.getId(), lists.get(0).getId());

        assertEquals(1, first.getNumber());
        assertEquals(2, second.getNumber());
        assertEquals(0, first.getPosition());
        assertEquals(1, second.getPosition());
        assertEquals(Priority.MEDIUM, first.getPriority());

        Task stored = new TaskDao().findById(first.getId());
        assertEquals(1, stored.getNumber());
        assertEquals(lists.get(0).getId(), stored.getTaskListParent().getId());
    }

    @Test
    @DisplayName("Permission checks work with a real operator id (no lazy loading errors)")
    void ownerOperationsDoNotFailOnLazyLoading() {
        User owner = newUser("owner");
        Board board = newBoard(owner);
        List<TaskList> lists = new TaskListDao().findByTableId(board.getId());
        TaskService service = new TaskServiceImpl();

        Task task = service.createTask("card", null, owner.getId(), lists.get(0).getId());

        Task moved = service.moveTask(task.getId(), lists.get(1).getId(), -1, owner.getId());
        assertEquals(0, moved.getPosition());
        assertEquals(lists.get(1).getId(), moved.getTaskListParent().getId());

        assertDoesNotThrow(() -> service.renameList(lists.get(0).getId(), "Backlog", owner.getId()));
        assertDoesNotThrow(() -> service.updateTask(task.getId(), "card!", "text", owner.getId()));
        assertThrows(SecurityException.class,
                () -> service.updateTask(task.getId(), "hack", null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("Priority, tag and due date are stored")
    void newCardFieldsArePersisted() {
        User owner = newUser("owner");
        Board board = newBoard(owner);
        List<TaskList> lists = new TaskListDao().findByTableId(board.getId());
        TaskService service = new TaskServiceImpl();

        Task task = service.createTask("card", null, owner.getId(), lists.get(0).getId());
        task.setPriority(Priority.HIGH);
        task.setTag("Frontend");
        task.setDueDate(LocalDate.of(2026, 11, 5));
        service.updateTask(task);

        Task stored = new TaskDao().findById(task.getId());
        assertEquals(Priority.HIGH, stored.getPriority());
        assertEquals("Frontend", stored.getTag());
        assertEquals(LocalDate.of(2026, 11, 5), stored.getDueDate());
    }

    @Test
    @DisplayName("Username and e-mail are unique in the database")
    void uniquenessIsEnforcedByTheDatabase() {
        String name = "dup-" + UUID.randomUUID();
        User first = new User(name, "secret123");
        first.setEmail("Dup-" + name + "@Mail.com");
        new UserDao().save(first);
        assertEquals(("dup-" + name + "@mail.com"), first.getEmail());

        assertThrows(RuntimeException.class, () -> new UserDao().save(new User(name, "secret123")));

        User sameEmail = new User("other-" + UUID.randomUUID(), "secret123");
        sameEmail.setEmail(first.getEmail().toUpperCase());
        assertThrows(RuntimeException.class, () -> new UserDao().save(sameEmail));
    }

    @Test
    @DisplayName("Deleting a user keeps the cards that were assigned to him")
    void deletingUserUnassignsCards() {
        User owner = newUser("owner");
        User assignee = newUser("assignee");
        Board board = newBoard(owner);
        List<TaskList> lists = new TaskListDao().findByTableId(board.getId());
        Task task = new TaskServiceImpl().createTask("card", null, assignee.getId(), lists.get(0).getId());

        assertTrue(new UserDao().deleteById(assignee.getId()));

        Task stored = new TaskDao().findById(task.getId());
        assertNotNull(stored);
        assertNull(stored.getUserOwner());
    }
}