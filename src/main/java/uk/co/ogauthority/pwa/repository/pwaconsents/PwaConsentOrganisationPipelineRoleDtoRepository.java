package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Repository
public interface PwaConsentOrganisationPipelineRoleDtoRepository {


  List<OrganisationPipelineRoleDto> findActiveOrganisationPipelineRolesByMasterPwa(MasterPwa masterPwa);

}
