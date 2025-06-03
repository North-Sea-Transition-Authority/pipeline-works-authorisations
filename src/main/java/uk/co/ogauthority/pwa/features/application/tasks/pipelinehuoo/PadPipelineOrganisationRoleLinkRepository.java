package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Repository
public interface PadPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PadPipelineOrganisationRoleLink, Integer>,
    PadPipelineOrganisationRoleLinkDtoRepository {

  @EntityGraph(attributePaths = {
      "padOrgRole",
      "padOrgRole.organisationUnit",
      "padOrgRole.organisationUnit.portalOrganisationGroup"
  })// TODO: understand why eagerly loading pipeline breaks this method
  List<PadPipelineOrganisationRoleLink> findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_Role(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole
  );

  @EntityGraph(attributePaths = {"padOrgRole"})// TODO: understand why eagerly loading pipeline breaks this method
  List<PadPipelineOrganisationRoleLink> findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Set<Integer> pipelineIds
  );


  Long countByPadOrgRole(PadOrganisationRole padOrganisationRole);


  Long countByPadOrgRole_PwaApplicationDetailAndPadOrgRole_RoleAndPipelineAndSectionNumber(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole,
      Pipeline pipeline,
      Integer sectionNumber);

  @EntityGraph(attributePaths = {"padOrgRole"})
  List<PadPipelineOrganisationRoleLink> findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
      Collection<PadOrganisationRole> orgRoles,
      PwaApplicationDetail pwaApplicationDetail
  );

  List<PadPipelineOrganisationRoleLink> getAllByPadOrgRole_PwaApplicationDetailAndPipeline(PwaApplicationDetail detail,
                                                                                           Pipeline pipeline);


  List<PadPipelineOrganisationRoleLink> getAllByPadOrgRole_PwaApplicationDetailAndPipelineIn(PwaApplicationDetail detail,
                                                                                           Collection<Pipeline> pipelines);


  List<PadPipelineOrganisationRoleLink> getAllByPadOrgRole_PwaApplicationDetail(PwaApplicationDetail detail);

  @Query("SELECT pporl " +
      "FROM PwaApplicationDetail pad " +
      "JOIN PadOrganisationRole por ON pad.id = por.pwaApplicationDetail.id " +
      "JOIN PadPipelineOrganisationRoleLink pporl ON por.id = pporl.padOrgRole.id " +
      "WHERE pad.tipFlag = true " +
      "AND pad.status IN :statusesToUpdate " +
      "AND pporl.pipeline IN :retiredPipelines")
  List<PadPipelineOrganisationRoleLink> findAllDraftLinksForRetiredPipelines(
      @Param("statusesToUpdate") Collection<PwaApplicationStatus> statusesToUpdate,
      @Param("retiredPipelines") Collection<Pipeline> pipelines);

}
