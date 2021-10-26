package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadOrganisationRolesDtoRepository {
  List<OrganisationRoleInstanceDto> findOrganisationRoleDtoByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByPwaApplicationDetail(
      PwaApplicationDetail pwaApplicationDetail);

}
