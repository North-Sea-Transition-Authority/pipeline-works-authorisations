package uk.co.ogauthority.pwa.repository.pwaconsents;


import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;

@Repository
public interface PwaConsentPipelineOrganisationRoleLinkRepository extends
    CrudRepository<PwaConsentPipelineOrganisationRoleLink, Integer>,
    PwaConsentOrganisationPipelineRoleDtoRepository {

  List<PwaConsentPipelineOrganisationRoleLink> findByAddedByPwaConsent_MasterPwaAndEndedByPwaConsentIsNull(
      MasterPwa masterPwa);

  @Query("SELECT cporl " +
         "FROM PwaConsentPipelineOrganisationRoleLink cporl " +
         "JOIN PwaConsentOrganisationRole cor ON cporl.pwaConsentOrganisationRole = cor " +
         "WHERE cporl.addedByPwaConsent IN (:pwaConsents) " +
         "AND (cor.endedByPwaConsent IS NULL OR cor.endedByPwaConsent NOT IN (:pwaConsents)) " +
         "AND (cporl.endedByPwaConsent IS NULL OR cporl.endedByPwaConsent NOT IN (:pwaConsents)) ")
  List<PwaConsentPipelineOrganisationRoleLink> findActiveLinksAtTimeOfPwaConsents(Collection<PwaConsent> pwaConsents);

}