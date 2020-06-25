package uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo;


import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;

public interface PadPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PadPipelineOrganisationRoleLink, Integer>,
    PadPipelineOrganisationRoleLinkDtoRepository {

  @EntityGraph(attributePaths = {"padOrgRole"})
  List<PadPipelineOrganisationRoleLink> findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_Role(
      PwaApplicationDetail pwaApplicationDetail,
      HuooRole huooRole
  );

}