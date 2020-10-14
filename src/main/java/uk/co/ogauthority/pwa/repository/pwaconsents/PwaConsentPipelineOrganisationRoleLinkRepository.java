package uk.co.ogauthority.pwa.repository.pwaconsents;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;

public interface PwaConsentPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PwaConsentPipelineOrganisationRoleLink, Integer>,
    PwaConsentOrganisationPipelineRoleDtoRepository {



  List<PwaConsentPipelineOrganisationRoleLink> findByAddedByPwaConsent_MasterPwaAndEndedByPwaConsentIsNull(
      MasterPwa masterPwa);

}