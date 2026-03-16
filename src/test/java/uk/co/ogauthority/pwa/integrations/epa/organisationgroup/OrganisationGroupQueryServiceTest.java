package uk.co.ogauthority.pwa.integrations.epa.organisationgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.serviceproviders.epmq.ScopeType;
import uk.co.fivium.energyportal.starter.configuration.WellKnownOrganisationGroupsConfigurationProperties;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroupEmailDomain;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.ogauthority.pwa.config.ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class OrganisationGroupQueryServiceTest {

  private static final int GROUP_ID = 111;
  private static final int UNIT_ID = 222;

  private static final ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties CONFIGURATION_PROPERTIES
      = new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties(Map.of(
      1, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_GROUP,
          GROUP_ID
      ),
      2, new ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties.ScopeTypeAndIdDto(
          ScopeType.ORGANISATION_UNIT,
          UNIT_ID
      )
  ));

  private static final OrganisationGroup GROUP = OrganisationGroup.newBuilder()
      .organisationGroupId(GROUP_ID)
      .name("Company")
      .emailDomains(List.of(OrganisationGroupEmailDomain.newBuilder().domain("company1.com").build()))
      .build();

  private static final OrganisationUnit UNIT = OrganisationUnit.newBuilder()
      .organisationUnitId(UNIT_ID)
      .name("Company Unit")
      .organisationGroups(List.of(GROUP))
      .build();

  @Mock
  private OrganisationApi organisationApi;

  @Mock
  private WellKnownOrganisationGroupsConfigurationProperties wellKnownGroups;

  @Mock
  private WellKnownOrganisationGroupsConfigurationProperties.WellKnownOrgGroup nsta;

  private OrganisationGroupQueryService organisationGroupQueryService;

  @BeforeEach
  void setUp() {
    organisationGroupQueryService = new OrganisationGroupQueryService(
        organisationApi,
        wellKnownGroups,
        CONFIGURATION_PROPERTIES
    );
  }

  @Test
  void getOrganisationGroupById() {
    when(organisationApi.findOrganisationGroup(
        eq(GROUP_ID),
        any(OrganisationGroupProjectionRoot.class),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(GROUP));

    var organisationGroupOptional = organisationGroupQueryService.getOrganisationGroupById(GROUP_ID);

    var argumentCaptor = ArgumentCaptor.forClass(OrganisationGroupProjectionRoot.class);

    verify(organisationApi).findOrganisationGroup(
        eq(GROUP_ID),
        argumentCaptor.capture(),
        any(RequestPurpose.class)
    );

    assertThat(argumentCaptor.getValue().getFields()).containsKeys("organisationGroupId", "name", "emailDomains");

    assertThat(organisationGroupOptional)
        .contains(
            new OrganisationGroupDto(
                GROUP.getOrganisationGroupId(),
                GROUP.getName(),
                GROUP.getEmailDomains()
                    .stream()
                    .map(OrganisationGroupEmailDomain::getDomain)
                    .toList()
            )
        );
  }

  @Test
  void getOrganisationGroupById_noGroupFound() {

    when(organisationApi.findOrganisationGroup(
        eq(GROUP_ID),
        any(),
        any()
    )).thenReturn(Optional.empty());

    var organisationGroupOptional = organisationGroupQueryService.getOrganisationGroupById(GROUP_ID);

    assertThat(organisationGroupOptional).isEmpty();
  }

  @Test
  void getOrganisationGroupFromOrganisationById() {
    when(organisationApi.findOrganisationUnit(
        eq(UNIT_ID),
        any(OrganisationUnitProjectionRoot.class),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(UNIT));

    var organisationGroupOptional = organisationGroupQueryService.getOrganisationGroupFromOrganisationById(UNIT_ID);

    var argumentCaptor = ArgumentCaptor.forClass(OrganisationUnitProjectionRoot.class);

    verify(organisationApi).findOrganisationUnit(
        eq(UNIT_ID),
        argumentCaptor.capture(),
        any(RequestPurpose.class)
    );

    assertThat(argumentCaptor.getValue().getFields()).containsKeys("organisationUnitId", "name", "organisationGroups");

    assertThat(organisationGroupOptional)
        .contains(
            new OrganisationGroupDto(
                GROUP.getOrganisationGroupId(),
                GROUP.getName(),
                GROUP.getEmailDomains()
                    .stream()
                    .map(OrganisationGroupEmailDomain::getDomain)
                    .toList()
            )
        );
  }

  @Test
  void getOrganisationGroupFromOrganisationById_noGroupFound() {

    when(organisationApi.findOrganisationUnit(
        eq(UNIT_ID),
        any(),
        any()
    )).thenReturn(Optional.empty());

    var organisationGroupOptional = organisationGroupQueryService.getOrganisationGroupFromOrganisationById(UNIT_ID);

    assertThat(organisationGroupOptional).isEmpty();
  }

  @Test
  void getRegulatorOrganisationGroup() {
    var expectedId = Math.toIntExact(10001L);

    when(wellKnownGroups.nsta()).thenReturn(nsta);
    when(nsta.idAsInteger()).thenReturn(expectedId);

    organisationGroupQueryService.getRegulatorOrganisationGroup();

    verify(organisationApi).findOrganisationGroup(
        eq(expectedId),
        any(),
        any(RequestPurpose.class)
    );
  }

  @Test
  void getConsulteeOrganisationGroup_whenScopeTypeIsOrganisationGroup() {
    when(organisationApi.findOrganisationGroup(
        eq(GROUP_ID),
        any(OrganisationGroupProjectionRoot.class),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(GROUP));

    var organisationGroup = organisationGroupQueryService.getConsulteeOrganisationGroup("1");

    assertThat(organisationGroup)
        .contains(
            new OrganisationGroupDto(
                GROUP.getOrganisationGroupId(),
                GROUP.getName(),
                GROUP.getEmailDomains()
                    .stream()
                    .map(OrganisationGroupEmailDomain::getDomain)
                    .toList()
            )
        );
  }

  @Test
  void getConsulteeOrganisationGroup_whenScopeTypeIsOrganisationUnit() {
    when(organisationApi.findOrganisationUnit(
        eq(UNIT_ID),
        any(OrganisationUnitProjectionRoot.class),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(UNIT));

    var organisationGroup = organisationGroupQueryService.getConsulteeOrganisationGroup("2");

    assertThat(organisationGroup)
        .contains(
            new OrganisationGroupDto(
                GROUP.getOrganisationGroupId(),
                GROUP.getName(),
                GROUP.getEmailDomains()
                    .stream()
                    .map(OrganisationGroupEmailDomain::getDomain)
                    .toList()
            )
        );
  }
}