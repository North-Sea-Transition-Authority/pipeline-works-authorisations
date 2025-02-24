package uk.co.ogauthority.pwa.domain.pwa.huoo.model;


import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;

@ExtendWith(MockitoExtension.class)
class OrganisationRoleOwnerDtoTest {

  @Test
  void organisationRoleOwnerDto_setsAttributesAsExpected_whenGivenPortalOrg() {

    var orgRoleOwner =  new OrganisationRoleOwnerDto(HuooType.PORTAL_ORG, new OrganisationUnitId(1), null, null);

    assertThat(orgRoleOwner.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(orgRoleOwner.getManualOrganisationName()).isNull();
    assertThat(orgRoleOwner.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(1));
    assertThat(orgRoleOwner.getTreatyAgreement()).isNull();
  }

  @Test
  void organisationRoleOwnerDto_setsAttributesAsExpected_whenGivenTreatyAgreement() {

    var orgRoleOwner = new OrganisationRoleOwnerDto(HuooType.TREATY_AGREEMENT, null, null, TreatyAgreement.ANY_TREATY_COUNTRY);

    assertThat(orgRoleOwner.getHuooType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(orgRoleOwner.getManualOrganisationName()).isNull();
    assertThat(orgRoleOwner.getOrganisationUnitId()).isNull();
    assertThat(orgRoleOwner.getTreatyAgreement()).isEqualTo(TreatyAgreement.ANY_TREATY_COUNTRY);
  }

  @Test
  void organisationRoleOwnerDto_setsAttributesAsExpected_whenGivenMigratedOrg() {

    var orgRoleOwner = new OrganisationRoleOwnerDto(HuooType.PORTAL_ORG, null, "MigratedOrg",null);

    assertThat(orgRoleOwner.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(orgRoleOwner.getManualOrganisationName()).isEqualTo("MigratedOrg");
    assertThat(orgRoleOwner.getOrganisationUnitId()).isNull();
    assertThat(orgRoleOwner.getTreatyAgreement()).isNull();
  }

  @Test
  void equals(){

    EqualsVerifier.forClass(OrganisationRoleOwnerDto.class)
        .withPrefabValues(OrganisationUnitId.class, new OrganisationUnitId(1), new OrganisationUnitId(2))
        .withPrefabValues(HuooType.class, HuooType.PORTAL_ORG, HuooType.TREATY_AGREEMENT)
        .withPrefabValues(String.class, "one", "two")
        .withNonnullFields("huooType")
        .verify();
  }


}