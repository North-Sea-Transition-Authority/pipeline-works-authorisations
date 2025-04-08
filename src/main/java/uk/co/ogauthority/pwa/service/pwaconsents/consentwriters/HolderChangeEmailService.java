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
import uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow.HolderChangeConsentedEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class HolderChangeEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HolderChangeEmailService.class);

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final TeamService teamService;
  private final MasterPwaService masterPwaService;
  private final EmailService emailService;

  @Autowired
  public HolderChangeEmailService(PortalOrganisationsAccessor portalOrganisationsAccessor,
                                  TeamService teamService,
                                  MasterPwaService masterPwaService,
                                  EmailService emailService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.teamService = teamService;
    this.masterPwaService = masterPwaService;
    this.emailService = emailService;
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
        .collect(Collectors.toList());

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

    var orgGroupTeamMembers = teamService.getOrganisationTeamsForOrganisationGroups(ouIdToOrgGroupMap.values())
        .stream()
        .map(teamService::getTeamMembers)
        .flatMap(List::stream)
        .collect(Collectors.toList());

    var pwaReference = masterPwaService.getCurrentDetailOrThrow(pwaApplication.getMasterPwa())
        .getReference();

    orgGroupTeamMembers.forEach(teamMember -> {

      var person = teamMember.getPerson();

      var emailProps = new HolderChangeConsentedEmailProps(
          person.getFullName(),
          pwaApplication.getApplicationType().getDisplayName(),
          pwaApplication.getAppReference(),
          pwaReference,
          oldHolderCsv,
          newHolderCsv
      );

      emailService.sendEmail(emailProps, person, pwaApplication.getAppReference());
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
