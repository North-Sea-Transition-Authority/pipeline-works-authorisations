package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.features.email.EmailRecipientWithName;
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.HolderChangeConsentedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

@Service
public class HolderChangeEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HolderChangeEmailService.class);

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final MasterPwaService masterPwaService;
  private final EmailService emailService;
  private final PwaHolderTeamService pwaHolderTeamService;

  @Autowired
  public HolderChangeEmailService(PortalOrganisationsAccessor portalOrganisationsAccessor,
                                  MasterPwaService masterPwaService,
                                  EmailService emailService,
                                  PwaHolderTeamService pwaHolderTeamService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.masterPwaService = masterPwaService;
    this.emailService = emailService;
    this.pwaHolderTeamService = pwaHolderTeamService;
  }

  public void sendHolderChangeEmail(PwaApplication pwaApplication,
                                    List<PwaConsentOrganisationRole> endedRoles,
                                    List<PwaConsentOrganisationRole> addedRoles) {

    var holderOuIdsEnded = endedRoles.stream()
        .filter(r -> r.getRole() == HuooRole.HOLDER)
        .map(PwaConsentOrganisationRole::getOrganisationUnitId)
        .collect(Collectors.toList());

    var holderOuIdsAdded = addedRoles.stream()
        .filter(r -> r.getRole() == HuooRole.HOLDER)
        .map(PwaConsentOrganisationRole::getOrganisationUnitId)
        .collect(Collectors.toList());

    var holderOuIds = new ArrayList<Integer>();
    holderOuIds.addAll(holderOuIdsEnded);
    holderOuIds.addAll(holderOuIdsAdded);

    var addedOrgUnits = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(holderOuIdsAdded);
    var endedOrgUnits = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(holderOuIdsEnded);
    var holderOrgUnits = Stream.of(addedOrgUnits.stream(), endedOrgUnits.stream())
        .flatMap(e -> e)
        .toList();

    var ouIdsWithNoOrgGroupCsv = holderOrgUnits.stream()
        .filter(o -> o.getPortalOrganisationGroup().isEmpty())
        .map(o -> String.valueOf(o.getOuId()))
        .collect(Collectors.joining(","));

    var addedOrgUnitPortalOrgGroupIds = getPortalOrganisationGroupIdsFromOrgUnits(addedOrgUnits);
    var endedOrgUnitPortalOrgGroupIds = getPortalOrganisationGroupIdsFromOrgUnits(endedOrgUnits);

    addedOrgUnitPortalOrgGroupIds.removeAll(endedOrgUnitPortalOrgGroupIds);

    var primaryHolderIdentical = addedOrgUnitPortalOrgGroupIds.isEmpty();

    if (primaryHolderIdentical) {
      var endedOrgUnitNames = endedOrgUnits.stream()
          .map(PortalOrganisationUnit::getName)
          .collect(Collectors.joining(","));
      LOGGER.info(String.format("Holder(s) [%s] was changed however belongs to same org group on application [%d]",
              endedOrgUnitNames, pwaApplication.getId()));
      return;
    }

    if (!StringUtils.isEmpty(ouIdsWithNoOrgGroupCsv)) {
      LOGGER.error("Found holder org units not associated with org groups and cannot email. paId={} ouIds={}",
          pwaApplication.getId(), ouIdsWithNoOrgGroupCsv);
    }

    var ouIdToOrgGroupMap = holderOrgUnits.stream()
        .filter(o -> o.getPortalOrganisationGroup().isPresent())
        .collect(Collectors.toMap(PortalOrganisationUnit::getOuId, o -> o.getPortalOrganisationGroup().get()));

    var oldHolderCsv = getHolderNameCsv(holderOuIdsEnded, ouIdToOrgGroupMap);

    var newHolderCsv = getHolderNameCsv(holderOuIdsAdded, ouIdToOrgGroupMap);

    var orgGroupTeamMembers = pwaHolderTeamService.getMembersWithinHolderTeamForOrgGroups(ouIdToOrgGroupMap.values());

    var pwaReference = masterPwaService.getCurrentDetailOrThrow(pwaApplication.getMasterPwa()).getReference();

    orgGroupTeamMembers.forEach(member -> {

      var emailProps = new HolderChangeConsentedEmailProps(
          member.getFullName(),
          pwaApplication.getApplicationType().getDisplayName(),
          pwaApplication.getAppReference(),
          pwaReference,
          oldHolderCsv,
          newHolderCsv
      );

      emailService.sendEmail(emailProps, EmailRecipientWithName.from(member), pwaApplication.getAppReference());
    });

  }

  private Set<Integer> getPortalOrganisationGroupIdsFromOrgUnits(List<PortalOrganisationUnit> portalOrganisationUnits) {
    return portalOrganisationUnits.stream()
        .filter(portalOrganisationUnit -> portalOrganisationUnit.getPortalOrganisationGroup().isPresent())
        .flatMap(portalOrganisationUnit -> portalOrganisationUnit.getPortalOrganisationGroup().stream())
        .map(PortalOrganisationGroup::getOrgGrpId)
        .collect(Collectors.toSet());
  }

  private String getHolderNameCsv(List<Integer> holderOuIds,
                                  Map<Integer, PortalOrganisationGroup> ouIdToOrgGroupMap) {
    return holderOuIds.stream()
        .map(ouIdToOrgGroupMap::get)
        .map(PortalOrganisationGroup::getName)
        .sorted()
        .collect(Collectors.joining(", "));
  }

}
