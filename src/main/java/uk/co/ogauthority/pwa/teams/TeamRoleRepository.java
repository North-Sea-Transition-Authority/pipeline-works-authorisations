package uk.co.ogauthority.pwa.teams;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRoleRepository extends CrudRepository<TeamRole, UUID> {
  List<TeamRole> findByWuaIdAndRole(Long wuaId, Role role);

  List<TeamRole> findByWuaIdAndTeam(Long wuaId, Team team);

  List<TeamRole> findByTeam(Team team);

  List<TeamRole> findByTeamAndRole(Team team, Role role);

  void deleteByWuaIdAndTeam(Long wuaId, Team team);

  boolean existsByTeamAndWuaId(Team team, Long wuaId);

  List<TeamRole> findAllByWuaId(long wuaId);

  List<TeamRole> findByWuaIdAndTeam_TeamTypeAndRoleIn(Long wuaId, TeamType teamType, Collection<Role> roles);
}
