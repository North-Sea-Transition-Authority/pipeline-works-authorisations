package uk.co.ogauthority.pwa.model.dto.consents;


import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationRoleOwnerDtoTest {

  @Test
  public void createOrganisationUnitRoleOwner_setsAttributesAsExpected() {

    var orgRoleOwner = OrganisationRoleOwnerDto.createOrganisationUnitRoleOwner(new OrganisationUnitId(1));

    assertThat(orgRoleOwner.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(orgRoleOwner.getManualOrganisationName()).isNull();
    assertThat(orgRoleOwner.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(1));
    assertThat(orgRoleOwner.getTreatyAgreement()).isNull();
  }

  @Test
  public void createTreatyRoleOwner_setsAttributesAsExpected() {

    var orgRoleOwner = OrganisationRoleOwnerDto.createTreatyRoleOwner(TreatyAgreement.BELGIUM);

    assertThat(orgRoleOwner.getHuooType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(orgRoleOwner.getManualOrganisationName()).isNull();
    assertThat(orgRoleOwner.getOrganisationUnitId()).isNull();
    assertThat(orgRoleOwner.getTreatyAgreement()).isEqualTo(TreatyAgreement.BELGIUM);
  }

  @Test
  public void createMigratedOrganisationRoleOwner_setsAttributesAsExpected() {

    var orgRoleOwner = OrganisationRoleOwnerDto.createMigratedOrganisationRoleOwner("MigratedOrg");

    assertThat(orgRoleOwner.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(orgRoleOwner.getManualOrganisationName()).isEqualTo("MigratedOrg");
    assertThat(orgRoleOwner.getOrganisationUnitId()).isNull();
    assertThat(orgRoleOwner.getTreatyAgreement()).isNull();
  }

  @Test
  public void testEquals(){

    EqualsVerifier.forClass(OrganisationRoleOwnerDto.class)
        .withPrefabValues(OrganisationUnitId.class, new OrganisationUnitId(1), new OrganisationUnitId(2))
        .withPrefabValues(TreatyAgreement.class, TreatyAgreement.BELGIUM, TreatyAgreement.IRELAND)
        .withPrefabValues(HuooType.class, HuooType.PORTAL_ORG, HuooType.TREATY_AGREEMENT)
        .withPrefabValues(String.class, "one", "two")
        .withNonnullFields("huooType")
        .verify();
  }


}