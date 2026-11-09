package backend.dao;

import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
public class TaskDao {

	public Task findById(UUID id) {
		if (id == null) return null;
		log.info("Search task for id {}", id);
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			Task task = session.createQuery(
							"select t from Task t left join fetch t.userOwner left join fetch t.taskListParent l "
									+ "left join fetch l.board b left join fetch b.user left join fetch b.team "
									+ "left join fetch b.memberRoles where t.id = :id",
							Task.class)
					.setParameter("id", id)
					.uniqueResult();
			if (task != null) {
				log.info("Task found: id={}, title={}", task.getId(), task.getTitle());
				return task;
			}
			log.info("Task with id {} not found", id);
			return null;
		}
	}

	public void save(Task task) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.persist(task);
			tx.commit();
			log.info("Task saved with id: {}", task.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("Task save failed: {}", task, e);
			throw e;
		}
	}

	public void update(Task task) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.merge(task);
			tx.commit();
			log.info("Task updated with id: {}", task.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("Task update failed: {}", task, e);
			throw e;
		}
	}

	public void delete(Task task) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.remove(session.contains(task) ? task : session.merge(task));
			tx.commit();
			log.info("Task deleted with id: {}", task.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("Task deletion failed: {}", task, e);
			throw e;
		}
	}

	public boolean deleteById(UUID id) {
		if (id == null) return false;
		log.info("Delete task for id {}", id);
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			Task task = session.find(Task.class, id);
			if (task != null) {
				session.remove(task);
				tx.commit();
				log.info("Task deleted with id: {}", id);
				return true;
			}
			tx.commit();
			log.info("Task with id {} not found for deletion", id);
			return false;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("Task deletion failed for id: {}", id, e);
			throw e;
		}
	}

	public List<Task> findAll() {
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from Task order by position asc", Task.class).list();
		}
	}

	public List<Task> findByUserId(UUID userId) {
		if (userId == null) return Collections.emptyList();
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from Task t where t.userOwner.id = :userId order by t.position asc", Task.class)
					.setParameter("userId", userId)
					.list();
		}
	}

	public List<Task> findByTaskListId(UUID taskListId) {
		if (taskListId == null) return Collections.emptyList();
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from Task t where t.taskListParent.id = :taskListId order by t.position asc", Task.class)
					.setParameter("taskListId", taskListId)
					.list();
		}
	}
	public int nextPositionInList(UUID taskListId) {
		if (taskListId == null) return 0;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			Integer max = session.createQuery("select max(t.position) from Task t where t.taskListParent.id = :id", Integer.class)
					.setParameter("id", taskListId)
					.uniqueResult();
			return max == null ? 0 : max + 1;
		}
	}

	public List<Task> searchByTitle(String query) {
		if (query == null || query.isBlank()) return Collections.emptyList();
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from Task t where lower(t.title) like lower(:query) order by t.position asc", Task.class)
					.setParameter("query", "%" + query.trim() + "%")
					.list();
		}
	}
	public void saveInList(Task task) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			TaskList list = task.getTaskListParent();
			if (list != null && list.getId() != null) {
				UUID boardId = session.createQuery("select l.board.id from TaskList l where l.id = :id", UUID.class)
						.setParameter("id", list.getId())
						.uniqueResult();
				if (boardId != null) {
					session.createMutationQuery("update Board b set b.lastTaskNumber = b.lastTaskNumber + 1 where b.id = :id")
							.setParameter("id", boardId)
							.executeUpdate();
					Integer number = session.createQuery("select b.lastTaskNumber from Board b where b.id = :id", Integer.class)
							.setParameter("id", boardId)
							.uniqueResult();
					task.setNumber(number == null ? 0 : number);
				}
				Integer max = session.createQuery("select max(t.position) from Task t where t.taskListParent.id = :id", Integer.class)
						.setParameter("id", list.getId())
						.uniqueResult();
				task.setPosition(max == null ? 0 : max + 1);
			}
			session.persist(task);
			tx.commit();
			log.info("Task saved with id: {}, number: {}, position: {}", task.getId(), task.getNumber(), task.getPosition());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("Task save failed: {}", task, e);
			throw e;
		}
	}
}
