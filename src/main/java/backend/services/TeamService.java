package backend.services;

import backend.models.team.Team;
import backend.models.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamService {
    Team createTeam(String name, String description, UUID ownerId);
    Team createTeam(String name, String description, User owner);
    Optional<Team> findById(UUID id);
    Team getById(UUID id);
    Optional<Team> findByName(String name);
    List<Team> findAll();
    List<Team> searchByName(String query);
    void updateTeam(Team team);
    boolean deleteById(UUID id);
    void delete(Team team);
    boolean existsById(UUID id);
    boolean existsByName(String name);
}
