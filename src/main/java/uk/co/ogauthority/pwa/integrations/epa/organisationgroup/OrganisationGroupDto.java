package uk.co.ogauthority.pwa.integrations.epa.organisationgroup;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroupEmailDomain;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

public record OrganisationGroupDto(
    Integer organisationGroupId,
    String organisationGroupName,
    List<String> emailDomains
) {

  public static OrganisationGroupDto from(OrganisationGroup organisationGroup) {
    return new OrganisationGroupDto(
        organisationGroup.getOrganisationGroupId(),
        organisationGroup.getName(),
        Optional.ofNullable(organisationGroup.getEmailDomains())
            .orElse(Collections.emptyList())
            .stream()
            .map(OrganisationGroupEmailDomain::getDomain)
            .toList()
    );
  }

  public static OrganisationGroupDto fromUnit(OrganisationUnit organisationUnit) {
    return Optional.ofNullable(organisationUnit)
        .map(OrganisationUnit::getOrganisationGroups)
        .filter(groups -> !groups.isEmpty())
        .map(List::getFirst)
        .map(OrganisationGroupDto::from)
        .orElse(null);
  }

}
