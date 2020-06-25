package uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo;


import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
interface PadPipelineOrganisationRoleLinkDtoRepository {

  List<OrganisationPipelineRoleDto> findOrganisationPipelineRoleDtoByPwaApplicationDetail(
      PwaApplicationDetail applicationDetail);

}