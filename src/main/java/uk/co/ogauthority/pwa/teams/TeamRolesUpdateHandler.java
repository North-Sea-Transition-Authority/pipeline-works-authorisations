package uk.co.ogauthority.pwa.teams;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamRolesEpasMessage;
import uk.co.fivium.energyportal.starter.configuration.EnergyPortalAccountsConfigurationProperties;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamRolesUpdateHandler;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.model.auditrevisions.AuditRevisionUtil;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@Component
public class TeamRolesUpdateHandler implements EnergyPortalServiceProviderTeamRolesUpdateHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(TeamRolesUpdateHandler.class);

  private final UserAccountService userAccountService;
  private final TeamManagementService teamManagementService;
  private final String serviceName;

  TeamRolesUpdateHandler(
      UserAccountService userAccountService, TeamManagementService teamManagementService,
      EnergyPortalAccountsConfigurationProperties energyPortalAccountsConfigurationProperties
  ) {
    this.userAccountService = userAccountService;
    this.teamManagementService = teamManagementService;
    this.serviceName = energyPortalAccountsConfigurationProperties.serviceName();
  }

  @Override
  public void accept(ServiceProviderTeamRolesEpasMessage serviceProviderTeamRolesEpasMessage) {
    if (!serviceName.equals(serviceProviderTeamRolesEpasMessage.getService())) {
      return;
    }

    var serviceProviderUserTeamRolesDto = serviceProviderTeamRolesEpasMessage.getServiceProviderUserTeamRolesDto();

    var optionalTeam = teamManagementService.getTeam(UUID.fromString(serviceProviderUserTeamRolesDto.teamId()));

    if (optionalTeam.isEmpty()) {
      LOGGER.error("Team not found for id: {}, when updating team_roles from epas team roles update message. correlationId: {}",
          serviceProviderUserTeamRolesDto.teamId(),
          serviceProviderTeamRolesEpasMessage.getCorrelationId()
      );
      return;
    }

    var invokingUser = userAccountService.getWebUserAccount(
        Math.toIntExact(serviceProviderTeamRolesEpasMessage.getDeciderWuaId())
    );

    AuditRevisionUtil.withFallbackAuditUser(
        AuthenticatedUserAccount.from(invokingUser),
        () -> teamManagementService.setUserTeamRoles(
            serviceProviderUserTeamRolesDto.wuaId(),
            optionalTeam.get(),
            serviceProviderUserTeamRolesDto.roles().stream().map(Role::valueOf).toList(),
            serviceProviderTeamRolesEpasMessage.getDeciderWuaId()
        )
    );
  }
}