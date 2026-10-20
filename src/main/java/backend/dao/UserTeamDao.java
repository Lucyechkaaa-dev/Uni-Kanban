package backend.dao;

import backend.models.team.TeamRole;
import backend.models.team.UserTeam;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
public class UserTeamDao {

    public UserTeam findById(UUID id) {
        log.info("Search UserTeam for id {}", id);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            UserTeam ut = session.find(UserTeam.class, id);
            if (ut != null) {
                log.info("UserTeam found with id {}", id);
                return ut;
            }
            log.info("UserTeam with id {} not found", id);
            return null;
        }
    }

    public UserTeam findByUserAndTeam(UUID userId, UUID teamId) {
        log.info("Search UserTeam for userId {} and teamId {}", userId, teamId);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserTeam ut where ut.user.id = :userId and ut.team.id = :teamId", UserTeam.class)
                    .setParameter("userId", userId)
                    .setParameter("teamId", teamId)
                    .uniqueResult();
        }
    }

    public List<UserTeam> findByUserId(UUID userId) {
        log.info("Search UserTeams for userId {}", userId);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserTeam ut where ut.user.id = :userId", UserTeam.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    public List<UserTeam> findByTeamId(UUID teamId) {
        log.info("Search UserTeams for teamId {}", teamId);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserTeam ut where ut.team.id = :teamId", UserTeam.class)
                    .setParameter("teamId", teamId)
                    .list();
        }
    }

    public List<UserTeam> findByTeamIdAndRole(UUID teamId, TeamRole role) {
        log.info("Search UserTeams for teamId {} and role {}", teamId, role);
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from UserTeam ut where ut.team.id = :teamId and ut.role = :role", UserTeam.class)
                    .setParameter("teamId", teamId)
                    .setParameter("role", role)
                    .list();
        }
    }

    public void save(UserTeam userTeam) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(userTeam);
            tx.commit();
            log.info("UserTeam saved with id: {}", userTeam.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("UserTeam save failed for {}", userTeam, e);
            throw e;
        }
    }

    public void update(UserTeam userTeam) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(userTeam);
            tx.commit();
            log.info("UserTeam updated with id: {}", userTeam.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("UserTeam update failed for {}", userTeam, e);
            throw e;
        }
    }

    public void delete(UserTeam userTeam) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.remove(session.contains(userTeam) ? userTeam : session.merge(userTeam));
            tx.commit();
            log.info("UserTeam deleted with id: {}", userTeam.getId());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("UserTeam deletion failed for {}", userTeam, e);
            throw e;
        }
    }

    public boolean deleteById(UUID id) {
        log.info("Delete UserTeam for id {}", id);
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            UserTeam ut = session.find(UserTeam.class, id);
            if (ut != null) {
                session.remove(ut);
                tx.commit();
                log.info("UserTeam deleted with id: {}", id);
                return true;
            }
            tx.commit();
            log.info("UserTeam with id {} not found for deletion", id);
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("UserTeam deletion failed for id: {}", id, e);
            throw e;
        }
    }

    public boolean deleteByUserAndTeam(UUID userId, UUID teamId) {
        log.info("Delete UserTeam for userId {} and teamId {}", userId, teamId);
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            UserTeam ut = session.createQuery(
                    "from UserTeam ut where ut.user.id = :userId and ut.team.id = :teamId", UserTeam.class)
                    .setParameter("userId", userId)
                    .setParameter("teamId", teamId)
                    .uniqueResult();
            if (ut != null) {
                session.remove(ut);
                tx.commit();
                log.info("UserTeam deleted for user {} and team {}", userId, teamId);
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("UserTeam deletion failed for user {} and team {}", userId, teamId, e);
            throw e;
        }
    }

    public List<UserTeam> findAll() {
        log.info("findAll UserTeams requested");
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            List<UserTeam> results = session.createQuery("from UserTeam", UserTeam.class).list();
            log.info("findAll UserTeams found: {}", results.size());
            return results;
        }
    }
}
