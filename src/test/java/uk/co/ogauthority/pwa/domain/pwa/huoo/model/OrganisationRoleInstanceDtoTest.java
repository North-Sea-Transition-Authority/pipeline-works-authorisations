package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;

@ExtendWith(MockitoExtension.class)
class OrganisationRoleInstanceDtoTest {

  @Test
  void getOrganisationRoleOwnerDto_whenTreatyParamsGiven() {
    var orgRole = new OrganisationRoleInstanceDto(null, null, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.HOLDER,
        HuooType.TREATY_AGREEMENT);
    assertThat(orgRole.getOrganisationRoleOwnerDto()).extracting(
        OrganisationRoleOwnerDto::getHuooType,
        OrganisationRoleOwnerDto::getManualOrganisationName,
        OrganisationRoleOwnerDto::getOrganisationUnitId,
        OrganisationRoleOwnerDto::getTreatyAgreement
    ).containsExactly(
        HuooType.TREATY_AGREEMENT,
        null,
        null,
        TreatyAgreement.ANY_TREATY_COUNTRY
    );
  }

  @Test
  void getOrganisationRoleOwnerDto_whenPortalOrgParamsGiven() {
    var orgRole = new OrganisationRoleInstanceDto(1, null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getOrganisationRoleOwnerDto()).extracting(
        OrganisationRoleOwnerDto::getHuooType,
        OrganisationRoleOwnerDto::getManualOrganisationName,
        OrganisationRoleOwnerDto::getOrganisationUnitId,
        OrganisationRoleOwnerDto::getTreatyAgreement
    ).containsExactly(
        HuooType.PORTAL_ORG,
        null,
        new OrganisationUnitId(1),
        null
    );
  }

  @Test
  void getOrganisationUnitId_whenIdProvided() {
    var orgRole = new OrganisationRoleInstanceDto(1, null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(1));
  }

  @Test
  void getOrganisationUnitId_whenIdNotProvided() {
    var orgRole = new OrganisationRoleInstanceDto(null, null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getOrganisationUnitId()).isNull();
  }

  @Test
  void isPortalOrgRole_whenIdProvided() {
    var orgRole = new OrganisationRoleInstanceDto(1, null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.isPortalOrgRole()).isTrue();
  }

  @Test
  void isPortalOrgRole_whenIdNotProvided() {
    var orgRole = new OrganisationRoleInstanceDto(null, null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.isPortalOrgRole()).isFalse();
  }

  @Test
  void getManualOrganisationName_whenNameProvided() {
    var orgRole = new OrganisationRoleInstanceDto(null, "some name", null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getManualOrganisationName()).containsSame("some name");
  }

  @Test
  void getManualOrganisationName_whenNameNotProvided() {
    var orgRole = new OrganisationRoleInstanceDto(null, null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getManualOrganisationName()).isEmpty();
  }

  @Test
  void equals() {

    EqualsVerifier.forClass(OrganisationRoleInstanceDto.class)
        .withNonnullFields("huooRole")
        .verify();
  }
}