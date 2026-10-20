package backend.dao;

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
public class TaskListDao {

	public TaskList findById(UUID id) {
		if (id == null) return null;
		log.info("Search task list for id {}", id);
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			TaskList list = session.find(TaskList.class, id);
			if (list != null) {
				log.info("TaskList found: id={}, name={}", list.getId(), list.getName());
				return list;
			}
			log.info("TaskList with id {} not found", id);
			return null;
		}
	}

	public void save(TaskList taskList) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.persist(taskList);
			tx.commit();
			log.info("TaskList saved with id: {}", taskList.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskList save failed: {}", taskList, e);
			throw e;
		}
	}

	public void update(TaskList taskList) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.merge(taskList);
			tx.commit();
			log.info("TaskList updated with id: {}", taskList.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskList update failed: {}", taskList, e);
			throw e;
		}
	}

	public void delete(TaskList taskList) {
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.remove(session.contains(taskList) ? taskList : session.merge(taskList));
			tx.commit();
			log.info("TaskList deleted with id: {}", taskList.getId());
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskList deletion failed: {}", taskList, e);
			throw e;
		}
	}

	public boolean deleteById(UUID id) {
		if (id == null) return false;
		log.info("Delete task list for id {}", id);
		Transaction tx = null;
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			TaskList list = session.find(TaskList.class, id);
			if (list != null) {
				session.remove(list);
				tx.commit();
				log.info("TaskList deleted with id: {}", id);
				return true;
			}
			tx.commit();
			log.info("TaskList with id {} not found for deletion", id);
			return false;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			log.error("TaskList deletion failed for id: {}", id, e);
			throw e;
		}
	}

	public List<TaskList> findAll() {
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from TaskList", TaskList.class).list();
		}
	}

	public List<TaskList> findByTableId(UUID tableId) {
		if (tableId == null) return Collections.emptyList();
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery("from TaskList l where l.taskTable.id = :tableId", TaskList.class)
					.setParameter("tableId", tableId)
					.list();
		}
	}
}
