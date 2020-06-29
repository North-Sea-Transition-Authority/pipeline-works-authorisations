package uk.co.ogauthority.pwa.repository.pwaapplications.huoo;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadOrganisationRolesDtoRepository {
  List<OrganisationRoleDto> findOrganisationRoleDtoByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
