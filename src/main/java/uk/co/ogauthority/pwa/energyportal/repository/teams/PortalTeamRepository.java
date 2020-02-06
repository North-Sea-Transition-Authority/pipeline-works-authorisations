package uk.co.ogauthority.pwa.energyportal.repository.teams;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import uk.co.ogauthority.pwa.energyportal.model.entity.teams.PortalTeam;

public interface PortalTeamRepository extends CrudRepository<PortalTeam, Integer> {

  @Procedure("team_management.update_user_roles")
  void updateUserRoles(@Param("p_res_id") Integer resId,
                       @Param("p_person_id") Integer personId,
                       @Param("p_role_names_csv") String roleNames,
                       @Param("p_requesting_wua_id") Integer wuaId);

  @Procedure("team_management.remove_user_from_team")
  void removeUserFromTeam(@Param("p_res_id") Integer resId,
                          @Param("p_person_id") Integer personId,
                          @Param("p_requesting_wua_id") Integer wuaId);

}
