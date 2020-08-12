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
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
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
        padPipeline.getPipelineStatus());


  }
}