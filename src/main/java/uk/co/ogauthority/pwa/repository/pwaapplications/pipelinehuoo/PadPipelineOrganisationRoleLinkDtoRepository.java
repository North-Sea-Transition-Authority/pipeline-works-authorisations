package uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo;


import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
interface PadPipelineOrganisationRoleLinkDtoRepository {

  List<OrganisationPipelineRoleInstanceDto> findOrganisationPipelineRoleDtoByPwaApplicationDetail(
      PwaApplicationDetail applicationDetail);

}