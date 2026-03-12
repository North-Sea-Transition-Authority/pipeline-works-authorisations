package uk.co.ogauthority.pwa.integrations.epa.organisationgroup;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportal.starter.configuration.WellKnownOrganisationGroupsConfigurationProperties;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationUnitProjectionRoot;
import uk.co.ogauthority.pwa.config.ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties;

@Service
public class OrganisationGroupQueryService {
  public static final OrganisationUnitProjectionRoot ORGANISATION_GROUP_FROM_UNIT_PROJECTION_ROOT =
      new OrganisationUnitProjectionRoot()
          .organisationUnitId()
          .name()
          .organisationGroups()
            .organisationGroupId()
            .name()
            .emailDomains()
              .domain()
          .root();

  public static final OrganisationGroupProjectionRoot ORGANISATION_GROUP_PROJECTION_ROOT =
      new OrganisationGroupProjectionRoot()
          .organisationGroupId()
          .name()
          .emailDomains()
            .domain()
          .root();

  private final OrganisationApi organisationApi;
  private final WellKnownOrganisationGroupsConfigurationProperties wellKnownOrganisationGroups;
  private final ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties consulteeGroupIdToEpasScopeTypeAndIdConfigProps;

  OrganisationGroupQueryService(
      OrganisationApi organisationApi,
      WellKnownOrganisationGroupsConfigurationProperties wellKnownOrganisationGroups,
      ConsulteeGroupIdToEpasScopeTypeAndIdConfigurationProperties consulteeGroupIdToEpasScopeTypeAndIdConfigProps
  ) {
    this.organisationApi = organisationApi;
    this.wellKnownOrganisationGroups = wellKnownOrganisationGroups;
    this.consulteeGroupIdToEpasScopeTypeAndIdConfigProps = consulteeGroupIdToEpasScopeTypeAndIdConfigProps;
  }

  public Optional<OrganisationGroupDto> getOrganisationGroupById(Integer id) {
    return organisationApi.findOrganisationGroup(
            id,
            ORGANISATION_GROUP_PROJECTION_ROOT,
            new RequestPurpose("getOrganisationGroupById")
        )
        .map(OrganisationGroupDto::from);
  }

  public Optional<OrganisationGroupDto> getOrganisationGroupFromOrganisationById(Integer id) {
    return organisationApi.findOrganisationUnit(
        id,
        ORGANISATION_GROUP_FROM_UNIT_PROJECTION_ROOT,
        new RequestPurpose("getOrganisationById")
    )
    .map(OrganisationGroupDto::fromUnit);
  }

  public Optional<OrganisationGroupDto> getRegulatorOrganisationGroup() {
    return getOrganisationGroupById(wellKnownOrganisationGroups.nsta().idAsInteger());
  }

  public Optional<OrganisationGroupDto> getConsulteeOrganisationGroup(String scopeId) {
    var scopeTypeAndId = consulteeGroupIdToEpasScopeTypeAndIdConfigProps.getScopeTypeAndEpasScopeId(scopeId);
    return switch (scopeTypeAndId.scopeType()) {
      case ORGANISATION_GROUP -> getOrganisationGroupById(scopeTypeAndId.scopeId());
      case ORGANISATION_UNIT ->  getOrganisationGroupFromOrganisationById(scopeTypeAndId.scopeId());
    };
  }
}
