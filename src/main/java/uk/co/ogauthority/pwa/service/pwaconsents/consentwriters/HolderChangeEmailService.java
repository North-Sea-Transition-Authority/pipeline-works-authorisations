package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow.HolderChangeConsentedEmailProps;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class HolderChangeEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HolderChangeEmailService.class);

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final TeamService teamService;
  private final NotifyService notifyService;
  private final MasterPwaService masterPwaService;

  @Autowired
  public HolderChangeEmailService(PortalOrganisationsAccessor portalOrganisationsAccessor,
                                  TeamService teamService,
                                  NotifyService notifyService,
                                  MasterPwaService masterPwaService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.teamService = teamService;
    this.notifyService = notifyService;
    this.masterPwaService = masterPwaService;
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

    var holderOrgUnits = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(holderOuIds);

    var ouIdsWithNoOrgGroupCsv = holderOrgUnits.stream()
        .filter(o -> o.getPortalOrganisationGroup().isEmpty())
        .map(o -> String.valueOf(o.getOuId()))
        .collect(Collectors.joining(","));

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

      notifyService.sendEmail(emailProps, person.getEmailAddress());

    });

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
