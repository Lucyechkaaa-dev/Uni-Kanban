package backend.models.task;

import backend.models.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskModelTest {

    @Test
    @DisplayName("Should create TaskTable with name and handle relations")
    void testTaskTableCreationAndRelations() {
        User user = new User("testuser", "password");
        TaskTable table = new TaskTable("Main Board", user);

        assertEquals("Main Board", table.getName());
        assertEquals(user, table.getUser());
        assertNotNull(table.getTaskLists());
        assertTrue(table.getTaskLists().isEmpty());

        TaskList list = new TaskList("To Do");
        table.addTaskList(list);

        assertEquals(1, table.getTaskLists().size());
        assertEquals(table, list.getTaskTable());

        table.removeTaskList(list);
        assertEquals(0, table.getTaskLists().size());
        assertNull(list.getTaskTable());
    }

    @Test
    @DisplayName("Should create TaskList with name and handle task relations")
    void testTaskListCreationAndRelations() {
        TaskTable table = new TaskTable("Project Board");
        TaskList list = new TaskList("In Progress", table);

        assertEquals("In Progress", list.getName());
        assertEquals(table, list.getTaskTable());
        assertNotNull(list.getTasks());
        assertTrue(list.getTasks().isEmpty());

        Task task = new Task();
        list.addTask(task);

        assertEquals(1, list.getTasks().size());
        assertEquals(list, task.getTaskList());

        list.removeTask(task);
        assertEquals(0, list.getTasks().size());
        assertNull(task.getTaskList());
    }

    @Test
    @DisplayName("Should support getters, setters, and ID assignment")
    void testGettersAndSetters() {
        UUID tableId = UUID.randomUUID();
        TaskTable table = new TaskTable();
        table.setId(tableId);
        table.setName("Sprint Backlog");

        assertEquals(tableId, table.getId());
        assertEquals("Sprint Backlog", table.getName());

        UUID listId = UUID.randomUUID();
        TaskList list = new TaskList();
        list.setId(listId);
        list.setName("Done");
        list.setTaskTable(table);

        assertEquals(listId, list.getId());
        assertEquals("Done", list.getName());
        assertEquals(table, list.getTaskTable());
    }

    @Test
    @DisplayName("Should create Task with title, description, and position without timestamps")
    void testTaskFieldsAndConstructors() {
        User user = new User("developer", "secret");
        TaskList list = new TaskList("Backlog");
        Task task = new Task("Implement Auth", "Add JWT auth", user, list);

        assertEquals("Implement Auth", task.getTitle());
        assertEquals("Add JWT auth", task.getDescription());
        assertEquals(0, task.getPosition());
        assertEquals(user, task.getUser());
        assertEquals(list, task.getTaskList());

        task.setPosition(5);
        assertEquals(5, task.getPosition());

        Task simpleTask = new Task("Simple Task");
        assertEquals("Simple Task", simpleTask.getTitle());
        assertNull(simpleTask.getDescription());
    }
}
