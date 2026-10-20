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
public class TeamServiceImpl implements TeamService {

    private final TeamDao teamDao;
    private final UserDao userDao;
    private final UserTeamDao userTeamDao;

    @Autowired
    public TeamServiceImpl(TeamDao teamDao, UserDao userDao, UserTeamDao userTeamDao) {
        this.teamDao = Objects.requireNonNull(teamDao, "TeamDao must not be null");
        this.userDao = Objects.requireNonNull(userDao, "UserDao must not be null");
        this.userTeamDao = Objects.requireNonNull(userTeamDao, "UserTeamDao must not be null");
    }

    public TeamServiceImpl() {
        this(new TeamDao(), new UserDao(), new UserTeamDao());
    }

    @Override
    public Team createTeam(String name, String description, UUID ownerId) {
        validateTeamName(name);
        String trimmedName = name.trim();

        if (existsByName(trimmedName)) {
            log.warn("Creation rejected: team name '{}' is already taken", trimmedName);
            throw new IllegalArgumentException("Team name '" + trimmedName + "' is already taken");
        }

        User owner = null;
        if (ownerId != null) {
            owner = userDao.findById(ownerId);
            if (owner == null) {
                log.warn("Owner not found with id: {}", ownerId);
                throw new NoSuchElementException("Owner user not found with id: " + ownerId);
            }
        }

        Team team = new Team(trimmedName, description, owner);
        teamDao.save(team);
        log.info("Team created successfully with id: {}", team.getId());

        if (owner != null) {
            UserTeam ownerMembership = new UserTeam(owner, team, TeamRole.OWNER);
            userTeamDao.save(ownerMembership);
            team.getMembers().add(ownerMembership);
            if (owner.getUserTeams() != null && !owner.getUserTeams().contains(ownerMembership)) {
                owner.getUserTeams().add(ownerMembership);
            }
            log.info("Assigned user {} as OWNER of team {}", owner.getId(), team.getId());
        }

        return team;
    }

    @Override
    public Team createTeam(String name, String description, User owner) {
        UUID ownerId = (owner != null) ? owner.getId() : null;
        if (ownerId != null) {
            return createTeam(name, description, ownerId);
        }

        validateTeamName(name);
        String trimmedName = name.trim();

        if (existsByName(trimmedName)) {
            log.warn("Creation rejected: team name '{}' is already taken", trimmedName);
            throw new IllegalArgumentException("Team name '" + trimmedName + "' is already taken");
        }

        Team team = new Team(trimmedName, description, owner);
        teamDao.save(team);

        if (owner != null) {
            UserTeam ownerMembership = new UserTeam(owner, team, TeamRole.OWNER);
            userTeamDao.save(ownerMembership);
            team.getMembers().add(ownerMembership);
            if (owner.getUserTeams() != null && !owner.getUserTeams().contains(ownerMembership)) {
                owner.getUserTeams().add(ownerMembership);
            }
        }

        return team;
    }

    @Override
    public Optional<Team> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(teamDao.findById(id));
    }

    @Override
    public Team getById(UUID id) {
        return findById(id).orElseThrow(() -> {
            log.warn("Team with id {} not found", id);
            return new NoSuchElementException("Team not found with id: " + id);
        });
    }

    @Override
    public Optional<Team> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(teamDao.findByName(name.trim()));
    }

    @Override
    public List<Team> findAll() {
        return teamDao.findAll();
    }

    @Override
    public List<Team> searchByName(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return teamDao.searchByName(query.trim());
    }

    @Override
    public void updateTeam(Team team) {
        Objects.requireNonNull(team, "Team cannot be null");
        if (team.getId() == null) {
            throw new IllegalArgumentException("Team ID cannot be null when updating");
        }
        validateTeamName(team.getName());
        String trimmedName = team.getName().trim();
        team.setName(trimmedName);

        getById(team.getId());

        Optional<Team> existing = findByName(trimmedName);
        if (existing.isPresent() && !existing.get().getId().equals(team.getId())) {
            log.warn("Update rejected: team name '{}' is already taken", trimmedName);
            throw new IllegalArgumentException("Team name '" + trimmedName + "' is already taken");
        }

        teamDao.update(team);
        log.info("Team updated successfully: id={}", team.getId());
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) {
            return false;
        }
        log.info("Deleting team by id: {}", id);
        return teamDao.deleteById(id);
    }

    @Override
    public void delete(Team team) {
        Objects.requireNonNull(team, "Team cannot be null");
        log.info("Deleting team entity: id={}", team.getId());
        teamDao.delete(team);
    }

    @Override
    public boolean existsById(UUID id) {
        if (id == null) {
            return false;
        }
        return teamDao.findById(id) != null;
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return teamDao.findByName(name.trim()) != null;
    }

    private void validateTeamName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be empty");
        }
    }
}
