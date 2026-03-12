package uk.co.ogauthority.pwa.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.epa.organisationgroup.OrganisationGroupDto;
import uk.co.ogauthority.pwa.integrations.epa.organisationgroup.OrganisationGroupQueryService;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class EnergyPortalAllowedDomainServiceTest {

  private static final String USER_EMAIL = "user@example.com";

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @InjectMocks
  private EnergyPortalAllowedDomainService energyPortalAllowedDomainService;

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_industry(String domain, boolean isAllowed) {
    var industryTeam = new Team(UUID.randomUUID());
    industryTeam.setTeamType(TeamType.ORGANISATION);
    industryTeam.setName("industry team");
    industryTeam.setScopeType("ORGGRP");
    industryTeam.setScopeId("1");

    var orgGroup = new OrganisationGroupDto(
        1,
        "Org group",
        List.of(domain)
    );

    when(organisationGroupQueryService.getOrganisationGroupById(Integer.parseInt(industryTeam.getScopeId()))).thenReturn(
        Optional.of(orgGroup)
    );

    assertThat(energyPortalAllowedDomainService.isAllowedDomain(USER_EMAIL, industryTeam)).isEqualTo(isAllowed);
  }

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_regulator(String domain, boolean isAllowed) {
    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeam.setName("regulator team");

    var orgGroup = new OrganisationGroupDto(
        1,
        "Org group",
        List.of(domain)
    );

    when(organisationGroupQueryService.getRegulatorOrganisationGroup()).thenReturn(
        Optional.of(orgGroup)
    );

    assertThat(energyPortalAllowedDomainService.isAllowedDomain(USER_EMAIL, regTeam)).isEqualTo(isAllowed);
  }

  @ParameterizedTest
  @MethodSource("provideDomainIsAllowedCombinations")
  void isAllowedDomain_consultee(String domain, boolean isAllowed) {
    var consulteeTeam = new Team(UUID.randomUUID());
    consulteeTeam.setTeamType(TeamType.CONSULTEE);
    consulteeTeam.setName("consultee team");
    consulteeTeam.setScopeType("CONSULTEE");
    consulteeTeam.setScopeId("1");

    var orgGroup = new OrganisationGroupDto(
        1,
        "Org group",
        List.of(domain)
    );

    when(organisationGroupQueryService.getConsulteeOrganisationGroup(consulteeTeam.getScopeId())).thenReturn(
        Optional.of(orgGroup)
    );

    assertThat(energyPortalAllowedDomainService.isAllowedDomain(USER_EMAIL, consulteeTeam)).isEqualTo(isAllowed);
  }

  private static Stream<Arguments> provideDomainIsAllowedCombinations() {
    return Stream.of(
        Arguments.of("example.com", true),
        Arguments.of("domain.com", false)
    );
  }
}