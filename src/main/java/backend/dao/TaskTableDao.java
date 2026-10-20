package backend.dao;

import backend.models.task.TaskTable;
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
public class TaskTableDao {

	public TaskTable findById(UUID id) {
		if (id == null) return null;
		log.info("Search task table for id {}", id);
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			TaskTable table = session.find(TaskTable.class, id);
			if (table != null) {
				log.info("TaskTable found: id={}, name={}", table.getId(), table.getName());
				return table;
			}
			log.info("TaskTable with id {} not found", id);
			return null;
		}
	}

	public void save(TaskTable table) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.persist(table);
			tx.commit();
			log.info("TaskTable saved with id: {}", table.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskTable save failed: {}", table, e);
			throw e;
		}
	}

	public void update(TaskTable table) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.merge(table);
			tx.commit();
			log.info("TaskTable updated with id: {}", table.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskTable update failed: {}", table, e);
			throw e;
		}
	}

	public void delete(TaskTable table) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.remove(session.contains(table) ? table : session.merge(table));
			tx.commit();
			log.info("TaskTable deleted with id: {}", table.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskTable deletion failed: {}", table, e);
			throw e;
		}
	}

	public boolean deleteById(UUID id) {
		if (id == null) return false;
		log.info("Delete task table for id {}", id);
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			TaskTable table = session.find(TaskTable.class, id);
			if (table != null) {
				session.remove(table);
				tx.commit();
				log.info("TaskTable deleted with id: {}", id);
				return true;
			}
			tx.commit();
			log.info("TaskTable with id {} not found for deletion", id);
			return false;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskTable deletion failed for id: {}", id, e);
			throw e;
		}
	}

	public List<TaskTable> findAll() {
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from TaskTable", TaskTable.class).list();
		}
	}

	public List<TaskTable> findByUserId(UUID userId) {
		if (userId == null) return Collections.emptyList();
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from TaskTable t where t.user.id = :userId", TaskTable.class)
					.setParameter("userId", userId)
					.list();
		}
	}
}
