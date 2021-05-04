package uk.co.ogauthority.pwa.repository.pwaconsents;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;

@Repository
public interface PwaConsentPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PwaConsentPipelineOrganisationRoleLink, Integer>,
    PwaConsentOrganisationPipelineRoleDtoRepository {

  List<PwaConsentPipelineOrganisationRoleLink> findByAddedByPwaConsent_MasterPwaAndEndedByPwaConsentIsNull(
      MasterPwa masterPwa);

}