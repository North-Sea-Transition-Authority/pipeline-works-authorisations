package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadHuooValidationServiceTest {

  @Mock
  private PadOrganisationRolesRepository padOrganisationRolesRepository;

  @Mock
  private PadHuooRoleMetadataProvider padHuooRoleMetadataProvider;


  private PadHuooValidationService padHuooValidationService;

  private PwaApplicationDetail detail;

  private PortalOrganisationUnit activeOrganisation;
  private PortalOrganisationUnit inactiveOrganisation;

  @BeforeEach
  void setup() {
    padHuooValidationService = new PadHuooValidationService(padOrganisationRolesRepository, padHuooRoleMetadataProvider);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    activeOrganisation = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Active organisation");
    inactiveOrganisation = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();
  }

  @Test
  void getInactiveOrganisationNamesWithRole_when_treatyRoleExists_andInactiveOrgRoleExists(){

    var inactivePortalOrgUnit = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, inactivePortalOrgUnit),
        PadOrganisationRoleTestUtil.createTreatyRole(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY)
    ));

    var inactiveOrgNames = padHuooValidationService.getInactiveOrganisationNamesWithRole(detail);

    assertThat(inactiveOrgNames).containsExactly(inactivePortalOrgUnit.getName());

  }


  @Test
  void doesApplicationHaveValidUsers_invalid() {

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.PORTAL_ORG))
        .thenReturn(1L);

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.TREATY_AGREEMENT))
        .thenReturn(1L);

    var result = padHuooValidationService.doesApplicationHaveValidUsers(detail);
    assertThat(result).isFalse();
  }

  @Test
  void doesApplicationHaveValidUsers_valid_treatyAndNoOrg() {

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.PORTAL_ORG))
        .thenReturn(0L);

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.TREATY_AGREEMENT))
        .thenReturn(1L);

    var result = padHuooValidationService.doesApplicationHaveValidUsers(detail);
    assertThat(result).isTrue();
  }

  @Test
  void doesApplicationHaveValidUsers_valid_orgAndNoTreaty() {

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.PORTAL_ORG))
        .thenReturn(1L);

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.TREATY_AGREEMENT))
        .thenReturn(0L);

    var result = padHuooValidationService.doesApplicationHaveValidUsers(detail);
    assertThat(result).isTrue();
  }

  // getHuooSummaryValidationResult tests adapted from previous task section isComplete tests during rafactor.
  @Test
  void getHuooSummaryValidationResult_valid() {

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, activeOrganisation)
    ));

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.PORTAL_ORG))
        .thenReturn(0L);

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.TREATY_AGREEMENT))
        .thenReturn(1L);

    var result = padHuooValidationService.getHuooSummaryValidationResult(detail);
    assertThat(result.isValid()).isTrue();
  }

  @Test
  void getHuooSummaryValidationResult_invalid_missingRoleInstance() {

    // for each role, create a map where the role is has zero instances
    for (HuooRole role : HuooRole.values()) {
      var rolesMap = EnumSet.complementOf(EnumSet.of(role))
          .stream()
          .collect(Collectors.toMap(o -> o, o -> 1));

      rolesMap.put(role, 0);

      when(padHuooRoleMetadataProvider.getRoleCountMap(any())).thenReturn(rolesMap);

      var result = padHuooValidationService.getHuooSummaryValidationResult(detail);
      assertThat(result.isValid()).isFalse();
    }

  }

  @Test
  void getHuooSummaryValidationResult_invalid_inactiveOrgUnitHasRole() {

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, inactiveOrganisation)
    ));

    var result = padHuooValidationService.getHuooSummaryValidationResult(detail);
    assertThat(result.isValid()).isFalse();
  }

  @Test
  void getHuooSummaryValidationResult_invalid_invalidUsers() {

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of(
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, activeOrganisation),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, activeOrganisation)
    ));

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.PORTAL_ORG))
        .thenReturn(1L);

    when(padOrganisationRolesRepository.countPadOrganisationRoleByPwaApplicationDetailAndRoleAndType(
        detail, HuooRole.USER, HuooType.TREATY_AGREEMENT))
        .thenReturn(1L);

    var result = padHuooValidationService.getHuooSummaryValidationResult(detail);
    assertThat(result.isValid()).isFalse();
  }

}