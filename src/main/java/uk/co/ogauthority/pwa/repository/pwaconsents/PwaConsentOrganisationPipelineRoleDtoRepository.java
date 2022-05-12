package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Repository
public interface PwaConsentOrganisationPipelineRoleDtoRepository {


  List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByMasterPwa(MasterPwa masterPwa);

  List<OrganisationPipelineRoleInstanceDto> findActiveOrganisationPipelineRolesByPwaConsent(
      Collection<PwaConsent> pwaConsents, Pipeline pipeline);

}
