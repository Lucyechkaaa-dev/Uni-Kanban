package backend.dao;

import backend.models.user.User;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.UUID;

@Log4j2
public class UserDao{

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

	public void delete(User user){
		Transaction tx = null;
		try(Session session = HibernateFactory.getSessionFactory().openSession()){
			tx = session.beginTransaction();
			session.remove(user);
			tx.commit();
			log.info("User deleted with id: {}", user.getId());
		}
		catch(Exception e){
			if(tx != null) tx.rollback();
			log.error("User deletion failed : {}", user.toString(), e);
			throw e;
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
