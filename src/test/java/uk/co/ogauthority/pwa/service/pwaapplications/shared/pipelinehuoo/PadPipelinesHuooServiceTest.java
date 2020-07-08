package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelineValidationType;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelinesFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelinesHuooServiceTest {

  private final int CONSENTED_PIPELINE_ID = 10;
  private final int APPLICATION_PIPELINE_ID = 20;
  private final int APPLICATION_PAD_PIPELINE_ID = 30;
  private final PickablePipelineOption PIPELINE_WITH_ROLE = PickablePipelineOptionTestUtil.createOption(
      CONSENTED_PIPELINE_ID,
      PickablePipelineType.CONSENTED,
      "PL1");
  private final PickablePipelineOption PIPELINE_WITHOUT_ROLE = PickablePipelineOptionTestUtil.createOption(
      APPLICATION_PAD_PIPELINE_ID,
      PickablePipelineType.APPLICATION,
      "TEMP_1");

  @Mock
  private PickablePipelineService pickablePipelineService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator;

  @Mock
  private PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;

  private PwaApplicationDetail pwaApplicationDetail;
  private PickHuooPipelinesForm form;

  private PortalOrganisationUnit organisationUnit1;
  private PortalOrganisationUnit organisationUnit2;

  private TreatyAgreement treatyAgreement = TreatyAgreement.BELGIUM;

  private PadPipelinesHuooService padPipelinesHuooService;

  @Before
  public void setup() {
    var orgGrp = PortalOrganisationTestUtils.generateOrganisationGroup(10, "ArbitraryOrgGroup", "ArbOrgGrp");
    organisationUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(100, "org1", orgGrp);
    organisationUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(200, "org2", orgGrp);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    form = new PickHuooPipelinesForm();

    padPipelinesHuooService = new PadPipelinesHuooService(
        pickablePipelineService,
        portalOrganisationsAccessor,
        padOrganisationRoleService,
        pickHuooPipelinesFormValidator,
        padPipelineOrganisationRoleLinkRepository);

    when(padOrganisationRoleService.getPipelineIdsWhereRoleOfTypeSet(pwaApplicationDetail, HuooRole.OWNER))
        .thenReturn(Set.of(new PipelineId(CONSENTED_PIPELINE_ID)));

    when(pickablePipelineService
        .getAllPickablePipelinesForApplication(pwaApplicationDetail)).thenReturn(
        Set.of(PIPELINE_WITH_ROLE, PIPELINE_WITHOUT_ROLE)
    );

    // mimic successful reconciliation
    when(pickablePipelineService.reconcilePickablePipelineOptions(any())).thenReturn(
        Set.of(
            new ReconciledPickablePipeline(PickablePipelineId.from(PIPELINE_WITH_ROLE), new PipelineId(
                CONSENTED_PIPELINE_ID)),
            new ReconciledPickablePipeline(PickablePipelineId.from(PIPELINE_WITHOUT_ROLE), new PipelineId(
                APPLICATION_PIPELINE_ID))
        )
    );

  }

  @Test
  public void validateAddPipelineHuooForm_serviceInteraction() {

    var formBindingResult = new BeanPropertyBindingResult(form, "form");

    padPipelinesHuooService.validateAddPipelineHuooForm(
        pwaApplicationDetail,
        form,
        formBindingResult,
        PickHuooPipelineValidationType.PIPELINES,
        HuooRole.HOLDER
    );

    verify(pickHuooPipelinesFormValidator, times(1)).validate(
        form,
        formBindingResult,
        HuooRole.HOLDER,
        pwaApplicationDetail,
        PickHuooPipelineValidationType.PIPELINES);

  }

  @Test
  public void getPadOrganisationRolesFrom_noRolesFoundForOrgs() {

    var foundRoles = padPipelinesHuooService.getPadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(organisationUnit1.getOuId(), organisationUnit2.getOuId()),
        Set.of(treatyAgreement));

    assertThat(foundRoles).isEmpty();
  }

  @Test
  public void getPadOrganisationRolesFrom_rolesFoundForOrgs_treatyRoleNotFound() {
    var org1HolderRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit1,
        HuooRole.HOLDER);
    var org2HolderRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2,
        HuooRole.HOLDER);

    when(padOrganisationRoleService.getOrgRolesForDetailByRole(any(), any()))
        .thenReturn(List.of(org1HolderRole, org2HolderRole));

    var foundRoles = padPipelinesHuooService.getPadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(organisationUnit1.getOuId(), organisationUnit2.getOuId()),
        Set.of(treatyAgreement));

    assertThat(foundRoles).containsExactlyInAnyOrder(org1HolderRole, org2HolderRole);
    verify(padOrganisationRoleService, times(1)).getOrgRolesForDetailByRole(
        pwaApplicationDetail,
        HuooRole.HOLDER
    );
  }

  @Test
  public void getPadOrganisationRolesFrom_onlyTreatyRoleFound() {
    var treatyRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, treatyAgreement,
        HuooRole.HOLDER);


    when(padOrganisationRoleService.getOrgRolesForDetailByRole(any(), any()))
        .thenReturn(List.of(treatyRole));

    var foundRoles = padPipelinesHuooService.getPadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(organisationUnit1.getOuId(), organisationUnit2.getOuId()),
        Set.of(treatyAgreement));

    assertThat(foundRoles).containsExactlyInAnyOrder(treatyRole);

  }

  @Test
  public void createPipelineOrganisationRoles_everyPipelineGetsLinkedToEveryRole() {
    var org1HolderRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit1,
        HuooRole.HOLDER);
    var org2HolderRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2,
        HuooRole.HOLDER);

    var pipeline1 = new Pipeline();
    var pipeline2 = new Pipeline();

    padPipelinesHuooService.createPipelineOrganisationRoles(
        pwaApplicationDetail,
        List.of(org1HolderRole, org2HolderRole),
        Set.of(pipeline1, pipeline2));

    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org1HolderRole, pipeline1);
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org1HolderRole, pipeline2);
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org2HolderRole, pipeline1);
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org2HolderRole, pipeline2);

  }

  @Test
  public void getPickablePipelineOptionsWithNoRoleOfType_pipelineWithMatchingRoleExcluded() {

    var pickablePipelineOptions = padPipelinesHuooService.getPickablePipelineOptionsWithNoRoleOfType(
        pwaApplicationDetail, HuooRole.OWNER
    );

    assertThat(pickablePipelineOptions).hasOnlyOneElementSatisfying(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getRawId()).isEqualTo(APPLICATION_PAD_PIPELINE_ID);
      assertThat(pickablePipelineOption.getPickablePipelineType()).isEqualTo(PickablePipelineType.APPLICATION);
    });

  }

  @Test
  public void getPickablePipelineOptionsWithNoRoleOfType_noPipelinesWithRole() {

    when(padOrganisationRoleService.getPipelineIdsWhereRoleOfTypeSet(pwaApplicationDetail, HuooRole.OPERATOR))
        .thenReturn(Set.of());

    var pickablePipelineOptions = padPipelinesHuooService.getPickablePipelineOptionsWithNoRoleOfType(
        pwaApplicationDetail, HuooRole.OPERATOR
    );

    assertThat(pickablePipelineOptions).hasSize(2);
    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getRawId()).isEqualTo(CONSENTED_PIPELINE_ID);
      assertThat(pickablePipelineOption.getPickablePipelineType()).isEqualTo(PickablePipelineType.CONSENTED);
    });
    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getRawId()).isEqualTo(APPLICATION_PAD_PIPELINE_ID);
      assertThat(pickablePipelineOption.getPickablePipelineType()).isEqualTo(PickablePipelineType.APPLICATION);
    });
  }

  @Test
  public void getAvailableOrgUnitDetailsForRole_filtersOrgRolesNotOfDesiredType() {
    //noinspection unchecked
    ArgumentCaptor<List<PortalOrganisationUnit>> orgsforRoleCapture = ArgumentCaptor.forClass(List.class);

    when(padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail)).thenReturn(
        List.of(
            PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit1, HuooRole.HOLDER),
            PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2, HuooRole.OWNER)
        )
    );

    var orgs = padPipelinesHuooService.getAvailableOrgUnitDetailsForRole(pwaApplicationDetail, HuooRole.HOLDER);

    verify(padOrganisationRoleService, times(1)).getOrgRolesForDetail(pwaApplicationDetail);
    verify(portalOrganisationsAccessor, times(1)).getOrganisationUnitDetailDtos(orgsforRoleCapture.capture());

    assertThat(orgsforRoleCapture.getValue()).containsExactly(organisationUnit1);

  }

  @Test
  public void getAvailableTreatyAgreementsForRole_filtersTreatiesByRole(){
    var holderRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, TreatyAgreement.BELGIUM, HuooRole.HOLDER);
    var userRole = PadOrganisationRole.fromTreatyAgreement(
        pwaApplicationDetail,
        TreatyAgreement.IRELAND,
        HuooRole.USER);
    var orgRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2, HuooRole.USER);
    when(padOrganisationRoleService.getOrgRolesForDetail(any())).thenReturn(List.of(
        holderRole, userRole, orgRole
        ));


    var foundTreatiesWithRole = padPipelinesHuooService.getAvailableTreatyAgreementsForRole(pwaApplicationDetail, HuooRole.USER);

    assertThat(foundTreatiesWithRole).containsExactlyInAnyOrder(userRole.getAgreement());
  }

}