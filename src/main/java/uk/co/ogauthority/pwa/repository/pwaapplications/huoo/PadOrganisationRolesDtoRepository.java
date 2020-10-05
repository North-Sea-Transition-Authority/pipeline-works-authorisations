package uk.co.ogauthority.pwa.repository.pwaapplications.huoo;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadOrganisationRolesDtoRepository {
  List<OrganisationRoleInstanceDto> findOrganisationRoleDtoByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

}
