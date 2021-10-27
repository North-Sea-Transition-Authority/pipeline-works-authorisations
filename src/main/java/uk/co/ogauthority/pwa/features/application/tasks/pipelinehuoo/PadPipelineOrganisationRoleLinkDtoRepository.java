package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo;


import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
interface PadPipelineOrganisationRoleLinkDtoRepository {

  List<OrganisationPipelineRoleInstanceDto> findOrganisationPipelineRoleDtoByPwaApplicationDetail(
      PwaApplicationDetail applicationDetail);


}