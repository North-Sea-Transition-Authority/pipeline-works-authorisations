package uk.co.ogauthority.pwa.repository.pwaconsents;


import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;

public interface PwaConsentOrganisationRoleRepository extends CrudRepository<PwaConsentOrganisationRole, Integer> {

  @EntityGraph(attributePaths = {"addedByPwaConsent", "addedByPwaConsent.masterPwa"})
  List<PwaConsentOrganisationRole> findByOrganisationUnitIdInAndRoleInAndEndTimestampIsNull(
      Collection<Integer> organisationUnitIds,
      Collection<HuooRole> roles);

  @EntityGraph(attributePaths = {"addedByPwaConsent", "addedByPwaConsent.masterPwa"})
  List<PwaConsentOrganisationRole> findByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
      Collection<PwaConsent> pwaConsents,
      Collection<HuooRole> roles);

  Long countByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
      Collection<PwaConsent> pwaConsents,
      Collection<HuooRole> roles);

  @EntityGraph(attributePaths = {"addedByPwaConsent", "addedByPwaConsent.masterPwa"})
  List<PwaConsentOrganisationRole> findByAddedByPwaConsentInAndEndTimestampIsNull(Collection<PwaConsent> consents);

}