package uk.co.ogauthority.pwa.features.application.tasks.huoo;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadHuooSummaryViewServiceTest {

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  private PadHuooSummaryViewService padHuooSummaryViewService;

  private PwaApplicationDetail detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

  private PadOrganisationRole padOrgUnit1UserRole, padOrgUnit2OwnerRole, padAnyTreatyCountryRole;
  private PortalOrganisationUnitDetail org1Detail, org2Detail;

  private PortalOrganisationUnit orgUnit1;
  private PortalOrganisationUnit orgUnit2;


  @Before
  public void setUp() throws Exception {

    padHuooSummaryViewService = new PadHuooSummaryViewService(padOrganisationRoleService, portalOrganisationsAccessor);

    var orgGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "Group1", "G1");
    var orgGroup2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "Group2", "G2");

    orgUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ZZZ", orgGroup1);
    orgUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "AAA", orgGroup2);

    org1Detail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(orgUnit1, "add1", "111");
    org2Detail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(orgUnit2, "add2", "222");

    padOrgUnit1UserRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnit1, HuooRole.USER);
    padOrgUnit2OwnerRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnit2, HuooRole.OWNER);

    padAnyTreatyCountryRole = new PadOrganisationRole();
    padAnyTreatyCountryRole.setAgreement(TreatyAgreement.ANY_TREATY_COUNTRY);
    padAnyTreatyCountryRole.setRole(HuooRole.USER);
    padAnyTreatyCountryRole.setPwaApplicationDetail(detail);
    padAnyTreatyCountryRole.setType(HuooType.TREATY_AGREEMENT);
  }

  @Test
  public void canShowHolderGuidance_appTypes() {
    EnumSet.allOf(PwaApplicationType.class).forEach(pwaApplicationType -> {
      detail.getPwaApplication().setApplicationType(pwaApplicationType);
      boolean result = padHuooSummaryViewService.canShowHolderGuidance(detail);
      switch (pwaApplicationType) {
        case INITIAL:
          assertThat(result).isTrue();
          break;
        default:
          assertThat(result).isFalse();
      }
    });
  }

  @Test
  public void getHuooOrganisationUnitRoleViews() {

    var rolesList = List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole, padAnyTreatyCountryRole);

    padOrgUnit1UserRole.setRole(HuooRole.USER);
    padOrgUnit2OwnerRole.setRole(HuooRole.OWNER);

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(any())).thenReturn(List.of(
        org1Detail,
        org2Detail
    ));

    var viewList = padHuooSummaryViewService.getHuooOrganisationUnitRoleViews(detail, rolesList);

    assertThat(viewList.size()).isEqualTo(2);

    var org1View = viewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit1UserRole.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = viewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit2OwnerRole.getOrganisationUnit().getName())).findFirst().orElseThrow();

    assertThat(org1View.getCompanyAddress()).isEqualTo(org1Detail.getLegalAddress());
    assertThat(org1View.getRegisteredNumber()).isEqualTo(org1Detail.getRegisteredNumber());
    assertThat(org1View.getRoleSet()).containsExactlyInAnyOrderElementsOf(Set.of(padOrgUnit1UserRole.getRole()));
    assertThat(org1View.getRoles()).isEqualTo("User");

    assertThat(org2View.getCompanyAddress()).isEqualTo(org2Detail.getLegalAddress());
    assertThat(org2View.getRegisteredNumber()).isEqualTo(org2Detail.getRegisteredNumber());
    assertThat(org2View.getRoleSet()).containsExactlyInAnyOrderElementsOf(Set.of(padOrgUnit2OwnerRole.getRole()));
    assertThat(org2View.getRoles()).isEqualTo("Owner");

  }

  @Test
  public void getHuooOrganisationUnitRoleViews_sorting_holderTakesPrecedence() {

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(any())).thenReturn(List.of(
        org1Detail,
        org2Detail
    ));

    var orgRoleViewList = padHuooSummaryViewService.getHuooOrganisationUnitRoleViews(detail, List.of(
        padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    var org1View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit1UserRole.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit2OwnerRole.getOrganisationUnit().getName())).findFirst().orElseThrow();

    int comparison = org1View.compareTo(org2View);

    // org1 comes before org2 in the role list
    // although org2 wins by alphabetical name, roles are compared first and have higher precedence
    // org1 has Holder role, which is ranked higher
    assertThat(comparison).isEqualTo(-1);

    assertThat(orgRoleViewList.indexOf(org1View)).isZero();
    assertThat(orgRoleViewList.indexOf(org2View)).isEqualTo(1);

  }

  @Test
  public void getHuooOrganisationUnitRoleViews_sorting_orgNameBreaksTie() {

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(any())).thenReturn(List.of(
        org1Detail,
        org2Detail
    ));

    padOrgUnit1UserRole.setRole(padOrgUnit2OwnerRole.getRole()); // equalise the roles between the two orgs

    var orgRoleViewList = padHuooSummaryViewService.getHuooOrganisationUnitRoleViews(detail, List.of(
        padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    var org1View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit1UserRole.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit2OwnerRole.getOrganisationUnit().getName())).findFirst().orElseThrow();

    int comparison = org1View.compareTo(org2View);

    // org1 comes after org2 in the list
    // as the roles are equal, company name is used, and org2:AAA beats org1:ZZZ
    assertThat(comparison).isEqualTo(1);

    assertThat(orgRoleViewList.indexOf(org2View)).isZero();
    assertThat(orgRoleViewList.indexOf(org1View)).isEqualTo(1);

  }

  @Test
  public void getTreatyAgreementViews() {

    var rolesList = List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole, padAnyTreatyCountryRole);

    var viewList = padHuooSummaryViewService.getTreatyAgreementViews(detail, rolesList);

    assertThat(viewList.size()).isEqualTo(1);

    var anyCountry = viewList.stream().filter(
        r -> r.getCountry().equals(TreatyAgreement.ANY_TREATY_COUNTRY.getCountry())).findFirst().orElseThrow();

    assertThat(anyCountry.getTreatyAgreementText()).isEqualTo(TreatyAgreement.ANY_TREATY_COUNTRY.getAgreementText());
    assertThat(anyCountry.getRoles()).isEqualTo(padAnyTreatyCountryRole.getRole().getDisplayText());
    assertThat(anyCountry.getRoles()).isEqualTo("User");

  }
}