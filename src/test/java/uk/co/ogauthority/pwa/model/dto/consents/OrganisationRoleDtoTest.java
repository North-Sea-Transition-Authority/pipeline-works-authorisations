package uk.co.ogauthority.pwa.model.dto.consents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationRoleDtoTest {

  @Test
  public void getOrganisationUnitId_whenIdProvided() {
    var orgRole = new OrganisationRoleDto(1, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(1));
  }

  @Test
  public void getOrganisationUnitId_whenIdNotProvided() {
    var orgRole = new OrganisationRoleDto(null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getOrganisationUnitId()).isNull();
  }

  @Test
  public void isPortalOrgRole_whenIdProvided() {
    var orgRole = new OrganisationRoleDto(1, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.isPortalOrgRole()).isTrue();
  }

  @Test
  public void isPortalOrgRole_whenIdNotProvided() {
    var orgRole = new OrganisationRoleDto(null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.isPortalOrgRole()).isFalse();
  }

  @Test
  public void getManualOrganisationName_whenNameProvided() {
    var orgRole = new OrganisationRoleDto(null, "some name", HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getManualOrganisationName()).containsSame("some name");
  }

  @Test
  public void getManualOrganisationName_whenNameNotProvided() {
    var orgRole = new OrganisationRoleDto(null, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
    assertThat(orgRole.getManualOrganisationName()).isEmpty();
  }
}