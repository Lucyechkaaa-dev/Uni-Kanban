package backend.dao;

import backend.models.team.Team;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
public class TeamDao {

    public Team findById(UUID id) {
        log.info("Search team for id {}", id);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            Team team = session.find(Team.class, id);
            if (team != null) {
                log.info("Team {} found", team.getName());
                return team;
            }
            log.info("Team with id {} not found", id);
            return null;
        }
    }

    public Team findByName(String name) {
        log.info("Search team for name {}", name);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            Team team = session.createQuery("from Team t where t.name = :name", Team.class)
                    .setParameter("name", name)
                    .uniqueResult();
            if (team != null) {
                log.info("Team with name {} found", name);
                return team;
            }
            log.info("Team with name {} not found", name);
            return null;
        }
    }

    public void save(Team team) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(team);
            tx.commit();
            log.info("Team saved with id: {}", team.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Team save failed for {}", team, e);
            throw e;
        }
    }

    public void update(Team team) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(team);
            tx.commit();
            log.info("Team updated with id: {}", team.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Team update failed for {}", team, e);
            throw e;
        }
    }

    public void delete(Team team) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.contains(team) ? team : session.merge(team));
            tx.commit();
            log.info("Team deleted with id: {}", team.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Team deletion failed for {}", team, e);
            throw e;
        }
    }

    public boolean deleteById(UUID id) {
        log.info("Delete team for id {}", id);
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Team team = session.find(Team.class, id);
            if (team != null) {
                session.remove(team);
                tx.commit();
                log.info("Team deleted with id: {}", id);
                return true;
            }
            tx.commit();
            log.info("Team with id {} not found for deletion", id);
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("Team deletion failed for id: {}", id, e);
            throw e;
        }
    }

    public List<Team> searchByName(String query) {
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from Team t where lower(t.name) like lower(:query)", Team.class)
                    .setParameter("query", "%" + query + "%")
                    .list();
        }
    }

    public List<Team> findAll() {
        log.info("findAll teams requested");
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            List<Team> teams = session.createQuery("from Team", Team.class).list();
            log.info("findAll teams found: {}", teams.size());
            return teams;
        }
    }
}
