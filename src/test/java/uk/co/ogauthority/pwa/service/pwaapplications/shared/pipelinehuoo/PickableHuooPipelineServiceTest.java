package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PickableHuooPipelineServiceTest {

  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;

  private final int CONSENTED_PIPELINE_ID = 1;
  private final int APPLICATION_ONLY_PIPELINE_ID = 2;

  private PipelineType CONSENTED_PIPELINE_TYPE = PipelineType.PRODUCTION_FLOWLINE;
  private PipelineType APPLICATION_NEW_PIPELINE_TYPE = PipelineType.GAS_LIFT_JUMPER;
  // used to check pickable option has the correct details.
  private PipelineType IMPORTED_CONSENTED_PIPELINE_TYPE = PipelineType.CONTROL_JUMPER;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  private PickableHuooPipelineService pickableHuooPipelineService;

  private PwaApplicationDetail pwaApplicationDetail;
  private MasterPwa masterPwa;

  private Pipeline consentedPipeline;
  private PipelineDetail consentedPipelineDetail;
  private String consentedPipelinePickableId;


  private Pipeline applicationNewPipeline;
  private PadPipeline applicationNewPadPipeline;
  private String applicationNewPipelinePickableId;

  private PadPipeline importedConsentedPadPipeline;
  private String importedConsentedPipelinePickableId;

  private void setupConsentedPipeline() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    masterPwa = pwaApplicationDetail.getPwaApplication().getMasterPwa();

    consentedPipeline = new Pipeline();
    consentedPipeline.setId(CONSENTED_PIPELINE_ID);
    consentedPipeline.setMasterPwa(masterPwa);
    consentedPipelineDetail = new PipelineDetail(consentedPipeline);
    consentedPipelineDetail.setPipelineType(CONSENTED_PIPELINE_TYPE);
    consentedPipelinePickableId = PickableHuooPipelineType.createPickableStringFrom(consentedPipeline.getPipelineId());
  }

  private void setupApplicationPipelines() {
    applicationNewPipeline = new Pipeline(pwaApplicationDetail.getPwaApplication());
    applicationNewPipeline.setId(APPLICATION_ONLY_PIPELINE_ID);
    applicationNewPadPipeline = new PadPipeline(pwaApplicationDetail);
    applicationNewPadPipeline.setPipeline(applicationNewPipeline);
    applicationNewPadPipeline.setId(200);
    applicationNewPadPipeline.setPipelineType(APPLICATION_NEW_PIPELINE_TYPE);
    applicationNewPipelinePickableId = PickableHuooPipelineType.createPickableStringFrom(
        applicationNewPipeline.getPipelineId());

    // same pipeline as consented pipeline but within the application
    importedConsentedPadPipeline = new PadPipeline(pwaApplicationDetail);
    importedConsentedPadPipeline.setPipeline(consentedPipeline);
    importedConsentedPadPipeline.setId(300);
    importedConsentedPadPipeline.setPipelineType(IMPORTED_CONSENTED_PIPELINE_TYPE);
    importedConsentedPipelinePickableId = PickableHuooPipelineType.createPickableStringFrom(
        // same as consented as the same pipeline is represented
        consentedPipeline.getPipelineId());
  }


  @Before
  public void setup() {

    setupConsentedPipeline();
    setupApplicationPipelines();

    pickableHuooPipelineService = new PickableHuooPipelineService(
        pipelineDetailService,
        padPipelineService,
        padOrganisationRoleService);

    // Default behaviour is that the application contains a new pipeline and updates a consented pipeline, so
    // the pickable options should reflect details set in application not the consented model.
    when(padPipelineService.getAllPadPipelineSummaryDtosForApplicationDetail(pwaApplicationDetail))
        .thenReturn(
            List.of(generateFrom(applicationNewPadPipeline), generateFrom(importedConsentedPadPipeline))
        );

    when(pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwa(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(
            List.of(consentedPipelineDetail)
        );
  }


  @Test
  public void getAllPickablePipelinesForApplicationAndRole_returnsApplicationVersionOfImportedPipeline() {

    var pickablePipelineOptions = pickableHuooPipelineService.getAllPickablePipelinesForApplicationAndRole(
        pwaApplicationDetail, HuooRole.HOLDER);

    assertThat(pickablePipelineOptions).hasSize(2);

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getPickableString()).isEqualTo(importedConsentedPipelinePickableId);
      assertThat(pickablePipelineOption.getPipelineTypeDisplay()).isEqualTo(IMPORTED_CONSENTED_PIPELINE_TYPE.getDisplayName());
    });

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getPickableString()).isEqualTo(applicationNewPipelinePickableId);
      assertThat(pickablePipelineOption.getPipelineTypeDisplay()).isEqualTo(APPLICATION_NEW_PIPELINE_TYPE.getDisplayName());
    });
  }

  @Test
  public void getAllPickablePipelinesForApplicationAndRole_removesWholePipelineWhenSplitsExist() {

    var appPipelineSplit1 = PipelineSegment.from(
        applicationNewPipeline.getPipelineId(),
        PipelineIdentPoint.inclusivePoint("START"),
        PipelineIdentPoint.exclusivePoint("MID")
    );
    var appPipelineSplit2 = PipelineSegment.from(
        applicationNewPipeline.getPipelineId(),
        PipelineIdentPoint.exclusivePoint("MID"),
        PipelineIdentPoint.inclusivePoint("END")
    );

    when(padOrganisationRoleService.getPipelineSplitsForRole(
        pwaApplicationDetail,
        DEFAULT_ROLE
    )).thenReturn(Set.of(appPipelineSplit1, appPipelineSplit2));

    var pickablePipelineOptions = pickableHuooPipelineService.getAllPickablePipelinesForApplicationAndRole(
        pwaApplicationDetail, HuooRole.HOLDER);

    assertThat(pickablePipelineOptions).hasSize(3);

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getPickableString()).isEqualTo(PickableHuooPipelineType.createPickableString(appPipelineSplit1));
      assertThat(pickablePipelineOption.getSplitInfo()).isEqualToIgnoringCase(appPipelineSplit1.getDisplayString());
    });

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getPickableString()).isEqualTo(PickableHuooPipelineType.createPickableString(appPipelineSplit2));
      assertThat(pickablePipelineOption.getSplitInfo()).isEqualToIgnoringCase(appPipelineSplit2.getDisplayString());
    });

    assertThat(pickablePipelineOptions).noneSatisfy(pickablePipelineOption -> {
      assertThat(pickablePipelineOption.getPickableString()).isEqualTo(applicationNewPipelinePickableId);
    });
  }


  @Test
  public void getAllPipelineNumbersAndSplitsForApplicationDetailAndRole() {

    Set<PipelineIdentifier> pipelineIdentifiers = Set.of(applicationNewPipeline.getPipelineId());
    var pipelineNumbersAndSplits = pickableHuooPipelineService.getAllPipelineNumbersAndSplitsForApplicationDetailAndRole(
        pwaApplicationDetail, HuooRole.HOLDER, pipelineIdentifiers);

    assertThat(pipelineNumbersAndSplits).hasSize(1);

    assertThat(pipelineNumbersAndSplits.get(0).getPipelineIdentifier().getPipelineIdAsInt())
        .isEqualTo(applicationNewPipeline.getPipelineId().asInt());
  }

  @Test
  public void getPickedPipelinesFromStrings_allStringIdsReconciled_whenNoPipelinesSplit() {
    var pipelines = pickableHuooPipelineService.getPickedPipelinesFromStrings(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(
            importedConsentedPipelinePickableId,
            applicationNewPipelinePickableId
        ));

    assertThat(pipelines).containsExactlyInAnyOrder(
        consentedPipeline.getPipelineId(),
        applicationNewPipeline.getPipelineId()
    );

  }

  @Test
  public void getPickedPipelinesFromStrings_noIdReconciled() {
    var pipelines = pickableHuooPipelineService.getPickedPipelinesFromStrings(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(
            importedConsentedPipelinePickableId + "abc",
            applicationNewPipelinePickableId + "123",
            consentedPipelinePickableId + "xyz"
        ));

    assertThat(pipelines).isEmpty();

  }

  @Test
  public void reconcilePickablePipelineIdentifiers_correctlyAssociatesPickableIdToPipelineId_whenNoPipelineSplits() {

    var newPipelineOption = PickablePipelineOptionTestUtil
        .createOption(applicationNewPadPipeline.getPipelineId(), "new");
    var importedPipelineOption = PickablePipelineOptionTestUtil
        .createOption(importedConsentedPadPipeline.getPipelineId(), "imported");

    var reconciledPipelines = pickableHuooPipelineService.reconcilePickablePipelineIds(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(
            newPipelineOption.generatePickableHuooPipelineId(),
            importedPipelineOption.generatePickableHuooPipelineId())
    );

    assertThat(reconciledPipelines).hasSize(2);
    assertThat(reconciledPipelines).containsExactlyInAnyOrder(
        new ReconciledHuooPickablePipeline(
            PickableHuooPipelineId.from(consentedPipelinePickableId),
            PipelineId.from(consentedPipeline)),
        new ReconciledHuooPickablePipeline(
            PickableHuooPipelineId.from(applicationNewPipelinePickableId),
            PipelineId.from(applicationNewPipeline))
    );

  }

  private PadPipelineSummaryDto generateFrom(PadPipeline padPipeline) {

    return new PadPipelineSummaryDto(
        padPipeline.getId(),
        padPipeline.getPipeline().getId(),
        padPipeline.getPipelineType(),
        padPipeline.toString(),
        BigDecimal.TEN,
        "OIL",
        "PRODUCTS",
        1L,
        "STRUCT_A",
        45,
        45,
        BigDecimal.valueOf(45),
        LatitudeDirection.NORTH,
        1,
        1,
        BigDecimal.ONE,
        LongitudeDirection.EAST,
        "STRUCT_B",
        46,
        46,
        BigDecimal.valueOf(46),
        LatitudeDirection.NORTH,
        2,
        2,
        BigDecimal.valueOf(2),
        LongitudeDirection.EAST,
        padPipeline.getMaxExternalDiameter(),
        padPipeline.getPipelineInBundle(),
        padPipeline.getBundleName(),
        padPipeline.getPipelineFlexibility(),
        padPipeline.getPipelineMaterial(),
        padPipeline.getOtherPipelineMaterialUsed(),
        padPipeline.getTrenchedBuriedBackfilled(),
        padPipeline.getTrenchingMethodsDescription(),
        padPipeline.getPipelineStatus(),
        padPipeline.getPipelineStatusReason());


  }

  @Test
  public void getAllOrganisationRolePipelineGroupView_includesPortalOrgsAndTreaty() {

    //Organisation Roles Summary DTO
    var orgPipelineRoleInstanceDto1 = new OrganisationPipelineRoleInstanceDto(
        1,
        null,
        HuooRole.HOLDER,
        HuooType.PORTAL_ORG,
        1,
        null, null, null, null);

    var orgPipelineRoleInstanceDto2 = new OrganisationPipelineRoleInstanceDto(
        2,
        TreatyAgreement.BELGIUM,
        HuooRole.USER,
        HuooType.TREATY_AGREEMENT,
        1,
        null, null, null, null);

    var orgPipelineRoleInstanceDto3 = new OrganisationPipelineRoleInstanceDto(
        3,
        null,
        HuooRole.OPERATOR,
        HuooType.PORTAL_ORG,
        1,
        null, null, null, null);

    var orgPipelineRoleInstanceDto4 = new OrganisationPipelineRoleInstanceDto(
        4,
        null,
        HuooRole.OWNER,
        HuooType.PORTAL_ORG,
        1,
        null, null, null, null);

    var orgRolesSummaryDto = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        List.of(orgPipelineRoleInstanceDto1, orgPipelineRoleInstanceDto2, orgPipelineRoleInstanceDto3, orgPipelineRoleInstanceDto4));
    when(padOrganisationRoleService.getOrganisationRoleSummary(pwaApplicationDetail)).thenReturn(orgRolesSummaryDto);

    //Portal org units
    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(1, "company"), "address", "123");
    var organisationUnitDetailDto1 = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);

    var portalOrgUnitDetail3 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(3, "company3"), "address3", "1234");
    var organisationUnitDetailDto3 = OrganisationUnitDetailDto.from(portalOrgUnitDetail3);

    var portalOrgUnitDetail4 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(4, "company4"), "address4", "12345");
    var organisationUnitDetailDto4 = OrganisationUnitDetailDto.from(portalOrgUnitDetail4);

    when(padOrganisationRoleService.getOrganisationUnitDetailDtosByOrganisationUnitId(
        List.of(new OrganisationUnitId(1))))
        .thenReturn(List.of(organisationUnitDetailDto1));

    when(padOrganisationRoleService.getOrganisationUnitDetailDtosByOrganisationUnitId(
        List.of(new OrganisationUnitId(3))))
        .thenReturn(List.of(organisationUnitDetailDto3));

    when(padOrganisationRoleService.getOrganisationUnitDetailDtosByOrganisationUnitId(
        List.of(new OrganisationUnitId(4))))
        .thenReturn(List.of(organisationUnitDetailDto4));

    //asserts
    var actualView = pickableHuooPipelineService.getAllOrganisationRolePipelineGroupView(pwaApplicationDetail);

    var holderPortalOrgRolePipelineGroup = actualView.getHolderOrgRolePipelineGroups().get(0);
    assertThat(holderPortalOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(holderPortalOrgRolePipelineGroup.getCompanyName()).isEqualTo("company");
    assertThat(holderPortalOrgRolePipelineGroup.getTreatyAgreement()).isNull();
    assertThat(holderPortalOrgRolePipelineGroup.getRegisteredNumber()).isEqualTo("123");
    assertThat(holderPortalOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("address");
    assertThat(holderPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(holderPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

    var userTreatyOrgRolePipelineGroup = actualView.getUserOrgRolePipelineGroups().get(0);
    assertThat(userTreatyOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(userTreatyOrgRolePipelineGroup.getCompanyName()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getTreatyAgreement()).isEqualTo(TreatyAgreement.BELGIUM);
    assertThat(userTreatyOrgRolePipelineGroup.getRegisteredNumber()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getCompanyAddress()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(userTreatyOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

    var operatorPortalOrgRolePipelineGroup = actualView.getOperatorOrgRolePipelineGroups().get(0);
    assertThat(operatorPortalOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(operatorPortalOrgRolePipelineGroup.getCompanyName()).isEqualTo("company3");
    assertThat(operatorPortalOrgRolePipelineGroup.getTreatyAgreement()).isNull();
    assertThat(operatorPortalOrgRolePipelineGroup.getRegisteredNumber()).isEqualTo("1234");
    assertThat(operatorPortalOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("address3");
    assertThat(operatorPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(operatorPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

    var ownerPortalOrgRolePipelineGroup = actualView.getOwnerOrgRolePipelineGroups().get(0);
    assertThat(ownerPortalOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(ownerPortalOrgRolePipelineGroup.getCompanyName()).isEqualTo("company4");
    assertThat(ownerPortalOrgRolePipelineGroup.getTreatyAgreement()).isNull();
    assertThat(ownerPortalOrgRolePipelineGroup.getRegisteredNumber()).isEqualTo("12345");
    assertThat(ownerPortalOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("address4");
    assertThat(ownerPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(ownerPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

  }

}