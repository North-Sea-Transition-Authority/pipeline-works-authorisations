package uk.co.ogauthority.pwa.service.masterpwas;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.repository.masterpwas.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Service
public class NonConsentedPwaService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NonConsentedPwaService.class);

  private final EntityManager entityManager;
  private final MasterPwaDetailRepository masterPwaDetailRepository;

  @Autowired
  public NonConsentedPwaService(EntityManager entityManager,
                                MasterPwaDetailRepository masterPwaDetailRepository) {
    this.entityManager = entityManager;
    this.masterPwaDetailRepository = masterPwaDetailRepository;
  }

  /**
   * Returns DTO wrapping information about non consented master pwas where any of the provided holder orgs are a Holder.
   */
  public List<MasterPwaDetail> getNonConsentedMasterPwaDetailByHolderOrgUnits(
      Collection<PortalOrganisationUnit> holderOrganisationUnits) {

    var dtos = getNonConsentedMasterPwaDtoByHolderOrgUnits(holderOrganisationUnits);

    var nonConsentedMasterPwaDetailIds =  dtos.stream()
        .map(NonConsentedMasterPwaDto::getMasterPwaDetailId)
        .collect(Collectors.toSet());

    return IterableUtils.toList(masterPwaDetailRepository.findAllById(nonConsentedMasterPwaDetailIds));
  }

  /**
   * Returns DTO wrapping information about non consented master pwas where any of the provided holder orgs are a Holder.
   */
  private Set<NonConsentedMasterPwaDto> getNonConsentedMasterPwaDtoByHolderOrgUnits(
      Collection<PortalOrganisationUnit> holderOrganisationUnitSet) {

    if (holderOrganisationUnitSet.isEmpty()) {
      LOGGER.debug("no holder org units provided");
      return Set.of();
    }

    var query = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.service.masterpwas.NonConsentedMasterPwaDto(" +
            "  mpd.masterPwa.id" +
            ", mpd.id" +
            ", mpd.reference" +
            ", pa.id" +
            ", pad.id" +
            ", pad.status" +
            ") " +
            "FROM MasterPwaDetail mpd " +
            "JOIN PwaApplication pa ON mpd.masterPwa = pa.masterPwa " +
            "JOIN PadVersionLookup pvl ON pvl.pwaApplicationId = pa.id " +
            "JOIN PwaApplicationDetail pad ON pad.pwaApplication = pa " +
            "  AND pad.versionNo = pvl.latestSubmittedVersionNo " +
            "JOIN PadOrganisationRole por ON por.pwaApplicationDetail = pad " +
            "WHERE mpd.endInstant IS NULL " + // latest master pwa detail
            "AND mpd.masterPwaDetailStatus = :masterPwaDetailStatus " +
            // need to filter on app type to avoid returning multiple app types e.g DEPOSIT + INITIAL
            "AND pa.applicationType = :applicationType " +
            "AND por.organisationUnit IN :holderOrgUnits " +
            "AND por.role = :holderRole ",
        NonConsentedMasterPwaDto.class)
        .setParameter("masterPwaDetailStatus", MasterPwaDetailStatus.APPLICATION)
        .setParameter("applicationType", PwaApplicationType.INITIAL)
        .setParameter("holderOrgUnits", holderOrganisationUnitSet)
        .setParameter("holderRole", HuooRole.HOLDER);

    return query.getResultList()
        .stream()
        .collect(Collectors.toUnmodifiableSet());
  }

}
