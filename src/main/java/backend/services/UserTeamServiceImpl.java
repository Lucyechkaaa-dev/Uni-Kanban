package backend.services;

import backend.dao.TeamDao;
import backend.dao.UserDao;
import backend.dao.UserTeamDao;
import backend.models.team.Team;
import backend.models.team.TeamRole;
import backend.models.team.UserTeam;
import backend.models.user.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
public class UserTeamServiceImpl implements UserTeamService {

    private final UserTeamDao userTeamDao;
    private final TeamDao teamDao;
    private final UserDao userDao;

    @Autowired
    public UserTeamServiceImpl(UserTeamDao userTeamDao, TeamDao teamDao, UserDao userDao) {
        this.userTeamDao = Objects.requireNonNull(userTeamDao, "UserTeamDao must not be null");
        this.teamDao = Objects.requireNonNull(teamDao, "TeamDao must not be null");
        this.userDao = Objects.requireNonNull(userDao, "UserDao must not be null");
    }

    public UserTeamServiceImpl() {
        this(new UserTeamDao(), new TeamDao(), new UserDao());
    }

    @Override
    public UserTeam addMember(UUID teamId, UUID userId, TeamRole role) {
        Objects.requireNonNull(teamId, "Team ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        log.info("Adding user {} to team {} with role {}", userId, teamId, role);

        Team team = teamDao.findById(teamId);
        if (team == null) {
            log.warn("Team not found with id: {}", teamId);
            throw new NoSuchElementException("Team not found with id: " + teamId);
        }

        User user = userDao.findById(userId);
        if (user == null) {
            log.warn("User not found with id: {}", userId);
            throw new NoSuchElementException("User not found with id: " + userId);
        }

        UserTeam existing = userTeamDao.findByUserAndTeam(userId, teamId);
        if (existing != null) {
            log.warn("User {} is already a member of team {}", userId, teamId);
            throw new IllegalArgumentException("User " + userId + " is already a member of team " + teamId);
        }

        TeamRole assignedRole = (role != null) ? role : TeamRole.MEMBER;
        UserTeam membership = new UserTeam(user, team, assignedRole);
        userTeamDao.save(membership);

        if (team.getMembers() != null && !team.getMembers().contains(membership)) {
            team.getMembers().add(membership);
        }
        if (user.getUserTeams() != null && !user.getUserTeams().contains(membership)) {
            user.getUserTeams().add(membership);
        }

        log.info("User {} added to team {} successfully with role {}", userId, teamId, assignedRole);
        return membership;
    }

    @Override
    public void removeMember(UUID teamId, UUID userId) {
        Objects.requireNonNull(teamId, "Team ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        log.info("Removing user {} from team {}", userId, teamId);

        UserTeam membership = userTeamDao.findByUserAndTeam(userId, teamId);
        if (membership == null) {
            log.warn("Membership not found for user {} in team {}", userId, teamId);
            throw new NoSuchElementException("User " + userId + " is not a member of team " + teamId);
        }

        if (membership.getRole() == TeamRole.OWNER) {
            List<UserTeam> owners = userTeamDao.findByTeamIdAndRole(teamId, TeamRole.OWNER);
            if (owners.size() <= 1) {
                log.warn("Cannot remove the only OWNER {} from team {}", userId, teamId);
                throw new IllegalStateException("Cannot remove the sole OWNER of the team. Transfer ownership or assign another OWNER first.");
            }
        }

        userTeamDao.delete(membership);

        if (membership.getTeam() != null && membership.getTeam().getMembers() != null) {
            membership.getTeam().getMembers().remove(membership);
        }
        if (membership.getUser() != null && membership.getUser().getUserTeams() != null) {
            membership.getUser().getUserTeams().remove(membership);
        }

        log.info("User {} removed from team {} successfully", userId, teamId);
    }

    @Override
    public UserTeam updateRole(UUID teamId, UUID userId, TeamRole newRole) {
        Objects.requireNonNull(teamId, "Team ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(newRole, "New role cannot be null");
        log.info("Updating role for user {} in team {} to {}", userId, teamId, newRole);

        UserTeam membership = userTeamDao.findByUserAndTeam(userId, teamId);
        if (membership == null) {
            log.warn("Membership not found for user {} in team {}", userId, teamId);
            throw new NoSuchElementException("User " + userId + " is not a member of team " + teamId);
        }

        if (membership.getRole() == TeamRole.OWNER && newRole != TeamRole.OWNER) {
            List<UserTeam> owners = userTeamDao.findByTeamIdAndRole(teamId, TeamRole.OWNER);
            if (owners.size() <= 1) {
                log.warn("Cannot downgrade the only OWNER {} of team {}", userId, teamId);
                throw new IllegalStateException("Cannot demote the sole OWNER of the team. Assign another OWNER first.");
            }
        }

        membership.setRole(newRole);
        userTeamDao.update(membership);
        log.info("Updated role for user {} in team {} to {}", userId, teamId, newRole);
        return membership;
    }

    @Override
    public Optional<UserTeam> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userTeamDao.findById(id));
    }

    @Override
    public Optional<UserTeam> findByUserAndTeam(UUID userId, UUID teamId) {
        if (userId == null || teamId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userTeamDao.findByUserAndTeam(userId, teamId));
    }

    @Override
    public List<UserTeam> getMembershipsByUser(UUID userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return userTeamDao.findByUserId(userId);
    }

    @Override
    public List<UserTeam> getMembershipsByTeam(UUID teamId) {
        if (teamId == null) {
            return Collections.emptyList();
        }
        return userTeamDao.findByTeamId(teamId);
    }

    @Override
    public List<UserTeam> getMembersByRole(UUID teamId, TeamRole role) {
        if (teamId == null || role == null) {
            return Collections.emptyList();
        }
        return userTeamDao.findByTeamIdAndRole(teamId, role);
    }

    @Override
    public boolean isMember(UUID teamId, UUID userId) {
        if (teamId == null || userId == null) {
            return false;
        }
        return userTeamDao.findByUserAndTeam(userId, teamId) != null;
    }

    @Override
    public Optional<TeamRole> getUserRole(UUID teamId, UUID userId) {
        if (teamId == null || userId == null) {
            return Optional.empty();
        }
        UserTeam membership = userTeamDao.findByUserAndTeam(userId, teamId);
        return Optional.ofNullable(membership).map(UserTeam::getRole);
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) {
            return false;
        }
        log.info("Deleting UserTeam by id: {}", id);
        return userTeamDao.deleteById(id);
    }

    @Override
    public List<UserTeam> findAll() {
        return userTeamDao.findAll();
    }
}
