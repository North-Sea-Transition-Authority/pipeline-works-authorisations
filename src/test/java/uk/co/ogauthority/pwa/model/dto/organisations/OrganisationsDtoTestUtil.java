package uk.co.ogauthority.pwa.model.dto.organisations;

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
