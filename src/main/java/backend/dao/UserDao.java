package backend.dao;

import backend.models.user.User;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
public class UserDao{
	public User findByUsername(String username){
		log.info("Search user for username {}", username);
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			User user = session.createQuery("from User u where u.username = :username", User.class)
					.setParameter("username", username)
					.uniqueResult();
			if (user != null) {
				log.info("User with username {} found", username);
				return user;
			}
			log.info("User with username {} not found", username);
			return null;
		}
	}

	public User findById(UUID id){
		log.info("Search user for id {}", id);
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			User user = session.find(User.class, id);
			if(user != null){
				log.info("User {} found", user.toString());
				return user;
			}
			log.info("User not found");
			return null;
		}
	}

	public void save(User user){
		Transaction tx = null;
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			tx = session.beginTransaction();
			session.persist(user);
			tx.commit();
			log.info("User saved with id: {}", user.getId());
		}
		catch(Exception e){
			if(tx != null) tx.rollback();
			log.error("User save failed {}", user.toString(), e);
			throw e;
		}
	}

	public void update(User user){
		Transaction tx = null;
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			tx = session.beginTransaction();
			session.merge(user);
			tx.commit();
			log.info("User updated with id: {}", user.getId());
		}
		catch(Exception e){
			if(tx != null) tx.rollback();
			log.error("User updated failed : {}", user.toString(), e);
			throw e;
		}
	}

	public void updateById(UUID id){
		Transaction tx = null;
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			tx = session.beginTransaction();
			User user = session.find(User.class, id);
			if(user != null){
				session.merge(user);
				tx.commit();
				log.info("User updated with id: {}", id);
			}
		}
		catch (Exception e){
			if(tx != null) tx.rollback();
			log.error("User updated failed with id: {}", id, e);
			throw e;
		}
	}

	public void delete(User user){
		Transaction tx = null;
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			tx = session.beginTransaction();
			session.remove(session.contains(user) ? user : session.merge(user));
			tx.commit();
			log.info("User deleted with id: {}", user.getId());
		}
		catch(Exception e){
			if(tx != null && tx.isActive()) tx.rollback();
			log.error("User deletion failed : {}", user.toString(), e);
			throw e;
		}
	}

	public boolean deleteById(UUID id){
		log.info("Delete user for id {}", id);
		Transaction tx = null;
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			tx = session.beginTransaction();
			User user = session.find(User.class, id);
			if(user != null){
				session.remove(user);
				tx.commit();
				log.info("User deleted with id: {}", id);
				return true;
			}
			tx.commit();
			log.info("User with id {} not found for deletion", id);
			return false;
		}
		catch(Exception e){
			if(tx != null && tx.isActive()) tx.rollback();
			log.error("User deletion failed for id: {}", id, e);
			throw e;
		}
	}

	public List<User> searchByUsername(String query) {
		try (Session session = HibernateFactory.getSessionFactory().openSession()) {
			return session.createQuery(
					"from User u where lower(u.username) like lower(:query)", User.class)
					.setParameter("query", "%" + query + "%")
					.list();
		}
	}

	public List<User> findAll(){
		log.info("findAll users requested");
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			List<User> fromUser = session.createQuery("from User", User.class).list();
			log.info("findAll users found : {}", fromUser.size());
			return fromUser;
		}
	}
}
