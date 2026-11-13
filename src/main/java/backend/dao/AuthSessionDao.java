package backend.dao;

import backend.models.user.AuthSession;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Log4j2
@Repository
public class AuthSessionDao {

    public void save(AuthSession authSession) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(authSession);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("AuthSession save failed", e);
            throw e;
        }
    }

    public Optional<AuthSession> findByTokenHash(String tokenHash) {
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery("from AuthSession s where s.tokenHash = :hash", AuthSession.class)
                    .setParameter("hash", tokenHash)
                    .uniqueResultOptional();
        }
    }

    /** Logout. Returns how many rows were removed (0 = there was no such session). */
    public int deleteByTokenHash(String tokenHash) {
        return executeDelete("delete from AuthSession s where s.tokenHash = :hash", "hash", tokenHash);
    }

    /** Housekeeping: rows whose time is over are of no use to anybody. */
    public int deleteExpired(Instant now) {
        return executeDelete("delete from AuthSession s where s.expiresAt < :now", "now", now);
    }

    private int executeDelete(String hql, String param, Object value) {
        Transaction tx = null;
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int removed = session.createMutationQuery(hql).setParameter(param, value).executeUpdate();
            tx.commit();
            return removed;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            log.error("AuthSession delete failed", e);
            throw e;
        }
    }
}
