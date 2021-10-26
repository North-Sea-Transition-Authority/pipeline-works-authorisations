package uk.co.ogauthority.pwa.domain.energyportal.organisations.model;

public class OrganisationsDtoTestUtil {

  public static OrganisationUnitDetailDto createDetailDto(int ouId, String name, String registeredNumber){
    return new OrganisationUnitDetailDto(
        ouId,
        registeredNumber,
        name,
        null
    );

  }
}
