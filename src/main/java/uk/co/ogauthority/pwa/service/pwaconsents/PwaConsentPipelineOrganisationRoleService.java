package uk.co.ogauthority.pwa.service.pwaconsents;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentPipelineOrganisationRoleLinkRepository;

@Service
public class PwaConsentPipelineOrganisationRoleService {

  private final PwaConsentPipelineOrganisationRoleLinkRepository roleLinkRepository;
  private final Clock clock;

  @Autowired
  public PwaConsentPipelineOrganisationRoleService(PwaConsentPipelineOrganisationRoleLinkRepository roleLinkRepository,
                                                   @Qualifier("utcClock") Clock clock) {
    this.roleLinkRepository = roleLinkRepository;
    this.clock = clock;
  }

  public List<PwaConsentPipelineOrganisationRoleLink> getActiveConsentedPipelineOrgRoleLinks(MasterPwa masterPwa) {
    return roleLinkRepository.findByAddedByPwaConsent_MasterPwaAndEndedByPwaConsentIsNull(masterPwa);
  }

  public void endRoleLinks(Collection<PwaConsentPipelineOrganisationRoleLink> links,
                           PwaConsent pwaConsent) {

    links.forEach(link -> {
      link.setEndTimestamp(clock.instant());
      link.setEndedByPwaConsent(pwaConsent);
    });

    roleLinkRepository.saveAll(links);

  }

  public List<PwaConsentPipelineOrganisationRoleLink> createRoleLinks(
      Collection<PadPipelineOrganisationRoleLink> padPipeLinksToWriteToConsent,
      Collection<PwaConsentOrganisationRole> activeConsentRoles,
      PwaConsent pwaConsent) {

    var orgUnitIdToRolesMap = activeConsentRoles.stream()
        .filter(r -> r.getOrganisationUnitId() != null)
        .collect(Collectors.groupingBy(PwaConsentOrganisationRole::getOrganisationUnitId));

    var treatyToRolesMap = activeConsentRoles.stream()
        .filter(r -> r.getAgreement() != null)
        .collect(Collectors.groupingBy(PwaConsentOrganisationRole::getAgreement));

    var newConsentLinks = padPipeLinksToWriteToConsent.stream()
        .map(padLink -> {

          var consentLink = new PwaConsentPipelineOrganisationRoleLink();

          // if org unit role exists on consent for pad link org, set on consent link
          Optional.ofNullable(padLink.getPadOrgRole().getOrganisationUnit())
              .map(ou -> orgUnitIdToRolesMap.get(ou.getOuId()))
              .map(List::stream)
              .flatMap(ouRoles -> ouRoles
                  .filter(r -> r.getRole() == padLink.getPadOrgRole().getRole())
                  .findFirst())
              .ifPresent(consentLink::setPwaConsentOrganisationRole);

          // if treaty role exists on consent for pad link treaty, set on consent link
          Optional.ofNullable(padLink.getPadOrgRole().getAgreement())
              .map(treatyToRolesMap::get)
              .map(List::stream)
              .flatMap(treatyRoles -> treatyRoles
                  .filter(r -> r.getRole() == padLink.getPadOrgRole().getRole())
                  .findFirst())
              .ifPresent(consentLink::setPwaConsentOrganisationRole);

          // if consent link role is null there is a problem, throw
          if (consentLink.getPwaConsentOrganisationRole() == null) {
            throw new RuntimeException(String.format("Couldn't find a consent role for padLink with ID: %s", padLink.getId()));
          }

          consentLink.setPipeline(padLink.getPipeline());

          consentLink.setFromLocation(padLink.getFromLocation());
          consentLink.setFromLocationIdentInclusionMode(padLink.getFromLocationIdentInclusionMode());

          consentLink.setToLocation(padLink.getToLocation());
          consentLink.setToLocationIdentInclusionMode(padLink.getToLocationIdentInclusionMode());

          consentLink.setSectionNumber(padLink.getSectionNumber());

          consentLink.setAddedByPwaConsent(pwaConsent);
          consentLink.setStartTimestamp(clock.instant());

          return consentLink;

        })
        .collect(Collectors.toList());

    return IterableUtils.toList(roleLinkRepository.saveAll(newConsentLinks));

  }

}
