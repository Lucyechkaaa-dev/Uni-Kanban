package backend.services;

import backend.models.team.TeamRole;
import backend.models.team.UserTeam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTeamService {
    UserTeam addMember(UUID teamId, UUID userId, TeamRole role);
    void removeMember(UUID teamId, UUID userId);
    UserTeam updateRole(UUID teamId, UUID userId, TeamRole newRole);
    Optional<UserTeam> findById(UUID id);
    Optional<UserTeam> findByUserAndTeam(UUID userId, UUID teamId);
    List<UserTeam> getMembershipsByUser(UUID userId);
    List<UserTeam> getMembershipsByTeam(UUID teamId);
    List<UserTeam> getMembersByRole(UUID teamId, TeamRole role);
    boolean isMember(UUID teamId, UUID userId);
    Optional<TeamRole> getUserRole(UUID teamId, UUID userId);
    boolean deleteById(UUID id);
    List<UserTeam> findAll();
}
