package uk.co.ogauthority.pwa.user;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.epa.organisationgroup.OrganisationGroupDto;
import uk.co.ogauthority.pwa.integrations.epa.organisationgroup.OrganisationGroupQueryService;
import uk.co.ogauthority.pwa.teams.Team;

@Service
public class AllowedDomainService {

  private final OrganisationGroupQueryService organisationGroupQueryService;

  AllowedDomainService(OrganisationGroupQueryService organisationGroupQueryService) {
    this.organisationGroupQueryService = organisationGroupQueryService;
  }

  public boolean isAllowedDomain(String userEmail, Team team) {
    Optional<OrganisationGroupDto> group;
    switch (team.getTeamType()) {
      case ORGANISATION -> group = organisationGroupQueryService.getOrganisationGroupById(Integer.parseInt(team.getScopeId()));
      case REGULATOR -> group = organisationGroupQueryService.getRegulatorOrganisationGroup();
      case CONSULTEE -> group = organisationGroupQueryService.getConsulteeOrganisationGroup(team.getScopeId());
      default -> throw new IllegalStateException("Unexpected value: " + team.getTeamType());
    }

    List<String> emailDomains = List.of();
    if (group.isPresent()) {
      emailDomains = group.get().emailDomains();
    }

    return emailDomains.isEmpty() || emailDomains
        .stream()
        .map(String::toLowerCase)
        .anyMatch(domain -> userEmail.toLowerCase().endsWith('@' + domain));
  }
}
