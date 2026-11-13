package backend.dao;

import backend.models.team.UserTeam;
import backend.utils.HibernateFactory;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Reads for board sharing. Shared boards are the boards whose team the user belongs to. */
@Log4j2
@Repository
public class BoardSharingDao {

    /** Memberships of a team with the users loaded, so the caller can read usernames. */
    public List<UserTeam> findTeamMembers(UUID teamId) {
        if (teamId == null) return Collections.emptyList();
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select ut from UserTeam ut join fetch ut.user where ut.team.id = :teamId "
                                    + "order by ut.user.username", UserTeam.class)
                    .setParameter("teamId", teamId)
                    .list();
        }
    }

    /**
     * Boards the user can reach through a team, but does not own, with the user's role in that team.
     * Each row is {Board, TeamRole}.
     */
    public List<Object[]> findBoardsSharedWith(UUID userId) {
        if (userId == null) return Collections.emptyList();
        try (Session session = HibernateFactory.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select b, ut.role from Board b left join fetch b.user "
                                    + "join UserTeam ut on ut.team = b.team "
                                    + "where ut.user.id = :userId and (b.user is null or b.user.id <> :userId) "
                                    + "order by b.name, b.id", Object[].class)
                    .setParameter("userId", userId)
                    .list();
        }
    }
}
