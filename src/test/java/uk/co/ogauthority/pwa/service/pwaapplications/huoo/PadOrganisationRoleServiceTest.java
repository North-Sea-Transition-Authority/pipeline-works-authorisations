package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.PortalOrganisationTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadOrganisationRoleServiceTest {

  @Mock
  private PadOrganisationRolesRepository repository;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  private PadOrganisationRoleService padOrganisationRoleService;

  private PwaApplicationDetail detail;
  private PadOrganisationRole org1, org2, treaty1, treaty2;
  private PortalOrganisationUnitDetail org1Detail, org2Detail;

  @Captor
  private ArgumentCaptor<PadOrganisationRole> roleCaptor;

  @Before
  public void setUp() {

    padOrganisationRoleService = new PadOrganisationRoleService(repository, portalOrganisationsAccessor);

    detail = new PwaApplicationDetail();
    detail.setPwaApplication(new PwaApplication());
    detail.getPwaApplication().setApplicationType(PwaApplicationType.INITIAL);

    var orgGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "Group1", "G1");
    var orgGroup2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "Group2", "G2");

    org1 = new PadOrganisationRole();
    org1.setOrganisationUnit(PortalOrganisationTestUtils.generateOrganisationUnit(1, "ZZZ", orgGroup1));
    org1.setPwaApplicationDetail(detail);
    org1.setRoles(Set.of(HuooRole.HOLDER, HuooRole.USER));
    org1.setType(HuooType.PORTAL_ORG);

    org2 = new PadOrganisationRole();
    org2.setOrganisationUnit(PortalOrganisationTestUtils.generateOrganisationUnit(2, "AAA", orgGroup2));
    org2.setPwaApplicationDetail(detail);
    org2.setRoles(Set.of(HuooRole.OWNER));
    org2.setType(HuooType.PORTAL_ORG);

    treaty1 = new PadOrganisationRole();
    treaty1.setAgreement(TreatyAgreement.NORWAY);
    treaty1.setRoles(Set.of(HuooRole.USER));
    treaty1.setPwaApplicationDetail(detail);
    treaty1.setType(HuooType.TREATY_AGREEMENT);

    treaty2 = new PadOrganisationRole();
    treaty2.setAgreement(TreatyAgreement.BELGIUM);
    treaty2.setRoles(Set.of(HuooRole.USER));
    treaty2.setPwaApplicationDetail(detail);
    treaty2.setType(HuooType.TREATY_AGREEMENT);

    when(repository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(org1, org2, treaty1, treaty2));

    org1Detail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(org1.getOrganisationUnit(), "add1", "111");
    org2Detail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(org2.getOrganisationUnit(), "add2", "222");

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(List.of(org1.getOrganisationUnit(), org2.getOrganisationUnit())))
        .thenReturn(List.of(org1Detail, org2Detail));

  }

  @Test
  public void getHuooOrganisationUnitRoleViews() {

    var rolesList = List.of(org1, org2, treaty1, treaty2);

    var viewList = padOrganisationRoleService.getHuooOrganisationUnitRoleViews(detail, rolesList);

    assertThat(viewList.size()).isEqualTo(2);

    var org1View = viewList.stream().filter(r -> r.getCompanyName().equals(org1.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = viewList.stream().filter(r -> r.getCompanyName().equals(org2.getOrganisationUnit().getName())).findFirst().orElseThrow();

    assertThat(org1View.getCompanyAddress()).isEqualTo(org1Detail.getLegalAddress());
    assertThat(org1View.getRegisteredNumber()).isEqualTo(org1Detail.getRegisteredNumber());
    assertThat(org1View.getRoleSet()).containsExactlyInAnyOrderElementsOf(org1.getRoles());
    assertThat(org1View.getRoles()).isEqualTo("Holder, User");

    assertThat(org2View.getCompanyAddress()).isEqualTo(org2Detail.getLegalAddress());
    assertThat(org2View.getRegisteredNumber()).isEqualTo(org2Detail.getRegisteredNumber());
    assertThat(org2View.getRoleSet()).containsExactlyInAnyOrderElementsOf(org2.getRoles());
    assertThat(org2View.getRoles()).isEqualTo("Owner");

  }

  @Test
  public void getHuooOrganisationUnitRoleViews_sorting_holderTakesPrecedence() {

    var orgRoleViewList = padOrganisationRoleService.getHuooOrganisationUnitRoleViews(detail, List.of(org1, org2));

    var org1View = orgRoleViewList.stream().filter(r -> r.getCompanyName().equals(org1.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = orgRoleViewList.stream().filter(r -> r.getCompanyName().equals(org2.getOrganisationUnit().getName())).findFirst().orElseThrow();

    int comparison = org1View.compareTo(org2View);

    // org1 comes before org2 in the role list
    // although org2 wins by alphabetical name, roles are compared first and have higher precedence
    // org1 has Holder role, which is ranked higher
    assertThat(comparison).isEqualTo(-1);

    assertThat(orgRoleViewList.indexOf(org1View)).isEqualTo(0);
    assertThat(orgRoleViewList.indexOf(org2View)).isEqualTo(1);

  }

  @Test
  public void getHuooOrganisationUnitRoleViews_sorting_orgNameBreaksTie() {

    org1.setRoles(org2.getRoles()); // equalise the roles between the two orgs

    var orgRoleViewList = padOrganisationRoleService.getHuooOrganisationUnitRoleViews(detail, List.of(org1, org2));

    var org1View = orgRoleViewList.stream().filter(r -> r.getCompanyName().equals(org1.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = orgRoleViewList.stream().filter(r -> r.getCompanyName().equals(org2.getOrganisationUnit().getName())).findFirst().orElseThrow();

    int comparison = org1View.compareTo(org2View);

    // org1 comes after org2 in the list
    // as the roles are equal, company name is used, and org2:AAA beats org1:ZZZ
    assertThat(comparison).isEqualTo(1);

    assertThat(orgRoleViewList.indexOf(org2View)).isEqualTo(0);
    assertThat(orgRoleViewList.indexOf(org1View)).isEqualTo(1);

  }

  @Test
  public void getTreatyAgreementViews() {

    var rolesList = List.of(org1, org2, treaty1, treaty2);

    var viewList = padOrganisationRoleService.getTreatyAgreementViews(detail, rolesList);

    assertThat(viewList.size()).isEqualTo(2);

    var norway = viewList.stream().filter(r -> r.getCountry().equals(TreatyAgreement.NORWAY.getCountry())).findFirst().orElseThrow();
    var belgium = viewList.stream().filter(r -> r.getCountry().equals(TreatyAgreement.BELGIUM.getCountry())).findFirst().orElseThrow();

    assertThat(norway.getTreatyAgreementText()).isEqualTo(TreatyAgreement.NORWAY.getAgreementText());
    assertThat(norway.getRoleSet()).containsExactlyInAnyOrderElementsOf(treaty1.getRoles());
    assertThat(norway.getRoles()).isEqualTo("User");
    assertThat(viewList.indexOf(norway)).isEqualTo(1); // sorted alphabetically

    assertThat(belgium.getTreatyAgreementText()).isEqualTo(TreatyAgreement.BELGIUM.getAgreementText());
    assertThat(belgium.getRoleSet()).containsExactlyInAnyOrderElementsOf(treaty2.getRoles());
    assertThat(belgium.getRoles()).isEqualTo("User");
    assertThat(viewList.indexOf(belgium)).isEqualTo(0); // sorted alphabetically

  }

  @Test
  public void canRemoveOrganisationRole_multipleHolders() {

    assertThat(org1.getRoles()).contains(HuooRole.HOLDER);
    org2.setRoles(Set.of(HuooRole.HOLDER));

    boolean canRemove = padOrganisationRoleService.canRemoveOrganisationRole(detail, org1);

    assertThat(canRemove).isTrue();

  }

  @Test
  public void canRemoveOrganisationRole_singleHolder() {

    assertThat(org1.getRoles()).contains(HuooRole.HOLDER);
    assertThat(org2.getRoles()).doesNotContain(HuooRole.HOLDER);

    boolean canRemove = padOrganisationRoleService.canRemoveOrganisationRole(detail, org1);

    assertThat(canRemove).isFalse();

  }

  @Test
  public void canRemoveOrganisationRole_removingNonHolder() {

    assertThat(org2.getRoles()).doesNotContain(HuooRole.HOLDER);

    boolean canRemove = padOrganisationRoleService.canRemoveOrganisationRole(detail, org2);

    assertThat(canRemove).isTrue();

  }

  @Test
  public void mapPadOrganisationRoleToForm_org() {

    var form = new HuooForm();

    padOrganisationRoleService.mapPadOrganisationRoleToForm(org1, form);

    assertThat(form.getHuooType()).isEqualTo(org1.getType());
    assertThat(form.getHuooRoles()).containsExactlyInAnyOrderElementsOf(org1.getRoles());
    assertThat(form.getOrganisationUnit()).isEqualTo(org1.getOrganisationUnit());
    assertThat(form.getTreatyAgreement()).isNull();

  }

  @Test
  public void mapPadOrganisationRoleToForm_treaty() {

    var form = new HuooForm();

    padOrganisationRoleService.mapPadOrganisationRoleToForm(treaty1, form);

    assertThat(form.getHuooType()).isEqualTo(treaty1.getType());
    assertThat(form.getHuooRoles()).containsExactlyInAnyOrderElementsOf(treaty1.getRoles());
    assertThat(form.getOrganisationUnit()).isNull();
    assertThat(form.getTreatyAgreement()).isEqualTo(treaty1.getAgreement());

  }

  @Test
  public void saveEntityUsingForm_org() {

    var form = new HuooForm();
    var newOrgUnit = PortalOrganisationTestUtils.getOrganisationUnit();

    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnit(newOrgUnit);
    form.setHuooRoles(Set.of(HuooRole.OPERATOR));

    padOrganisationRoleService.createAndSaveEntityUsingForm(detail, form);

    verify(repository, times(1)).save(roleCaptor.capture());

    var newRole = roleCaptor.getValue();

    assertThat(newRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newRole.getType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(newRole.getAgreement()).isNull();
    assertThat(newRole.getOrganisationUnit()).isEqualTo(newOrgUnit);
    assertThat(newRole.getRoles()).containsExactlyInAnyOrder(HuooRole.OPERATOR);

  }

  @Test
  public void saveEntityUsingForm_treaty() {

    var form = new HuooForm();

    form.setHuooType(HuooType.TREATY_AGREEMENT);
    form.setTreatyAgreement(TreatyAgreement.NETHERLANDS);
    form.setHuooRoles(Set.of(HuooRole.USER));

    padOrganisationRoleService.createAndSaveEntityUsingForm(detail, form);

    verify(repository, times(1)).save(roleCaptor.capture());

    var newRole = roleCaptor.getValue();

    assertThat(newRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newRole.getType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(newRole.getOrganisationUnit()).isNull();
    assertThat(newRole.getAgreement()).isEqualTo(TreatyAgreement.NETHERLANDS);
    assertThat(newRole.getRoles()).containsExactlyInAnyOrder(HuooRole.USER);

  }

  @Test
  public void addHolder() {

    var newOrgUnit = PortalOrganisationTestUtils.getOrganisationUnit();

    padOrganisationRoleService.addHolder(detail, newOrgUnit);

    verify(repository, times(1)).save(roleCaptor.capture());

    var newHolderRole = roleCaptor.getValue();

    assertThat(newHolderRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newHolderRole.getType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(newHolderRole.getRoles()).containsExactly(HuooRole.HOLDER);
    assertThat(newHolderRole.getOrganisationUnit()).isEqualTo(newOrgUnit);
    assertThat(newHolderRole.getAgreement()).isNull();

  }

  @Test
  public void getOrgRolesForDetail() {
    padOrganisationRoleService.getOrgRolesForDetail(detail);
    verify(repository, times(1)).getAllByPwaApplicationDetail(detail);
  }

  @Test
  public void getRoleCountMap() {
    when(padOrganisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(
        createOrgRole(HuooRole.HOLDER),
        createOrgRole(HuooRole.USER),
        createOrgRole(HuooRole.USER),
        createOrgRole(HuooRole.OPERATOR),
        createOrgRole(HuooRole.OPERATOR),
        createOrgRole(HuooRole.OPERATOR)
    ));

    var result = padOrganisationRoleService.getRoleCountMap(detail);
    assertThat(result).containsExactlyInAnyOrderEntriesOf(
        Map.ofEntries(
            entry(HuooRole.HOLDER, 1),
            entry(HuooRole.USER, 2),
            entry(HuooRole.OPERATOR, 3),
            entry(HuooRole.OWNER, 0)
        ));
  }

  private PadOrganisationRole createOrgRole(HuooRole role) {
    var org = new PadOrganisationRole();
    org.setRoles(Set.of(role));
    return org;
  }
}
