package uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo;


import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

public interface PadPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PadPipelineOrganisationRoleLink, Integer>,
    PadPipelineOrganisationRoleLinkDtoRepository {

  @EntityGraph(attributePaths = {"padOrgRole"})
  List<PadPipelineOrganisationRoleLink> findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_Role(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole
  );

  @EntityGraph(attributePaths = {"padOrgRole", "pipeline"})
  List<PadPipelineOrganisationRoleLink> findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Set<Integer> pipelineIds
  );

  @EntityGraph(attributePaths = {"padOrgRole"})
  List<PadPipelineOrganisationRoleLink> findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
      List<PadOrganisationRole> orgRoles,
      PwaApplicationDetail pwaApplicationDetail
  );

  List<PadPipelineOrganisationRoleLink> getAllByPadOrgRole_PwaApplicationDetailAndPipeline(PwaApplicationDetail detail,
                                                                                           Pipeline pipeline);

}