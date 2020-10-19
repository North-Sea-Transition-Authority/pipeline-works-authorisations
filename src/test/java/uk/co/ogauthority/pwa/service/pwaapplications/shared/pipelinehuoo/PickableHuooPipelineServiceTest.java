package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PickableHuooPipelineServiceTest {

  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;

  private final PipelineId CONSENTED_PIPELINE_ID = new PipelineId(1);
  private final PipelineId APPLICATION_NEW_PIPELINE_ID = new PipelineId(2);

  private PipelineType CONSENTED_PIPELINE_TYPE = PipelineType.PRODUCTION_FLOWLINE;
  private PipelineType APPLICATION_NEW_PIPELINE_TYPE = PipelineType.GAS_LIFT_JUMPER;


  @Mock
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PipelineOverview consentedPipelineOverview;

  @Mock
  private PipelineOverview applicationOnlyPipelineOverview;

  private PickableHuooPipelineService pickableHuooPipelineService;

  private PwaApplicationDetail pwaApplicationDetail;

  private String consentedPipelinePickableId;

  private String applicationNewPipelinePickableId;

  private String importedConsentedPipelinePickableId;

  private void setupConsentedPipeline() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    consentedPipelinePickableId = PickableHuooPipelineType.createPickableStringFrom(CONSENTED_PIPELINE_ID);

    when(consentedPipelineOverview.getPipelineId()).thenReturn(CONSENTED_PIPELINE_ID.asInt());
    when(consentedPipelineOverview.getPipelineType()).thenReturn(CONSENTED_PIPELINE_TYPE);

  }

  private void setupApplicationPipelines() {

    applicationNewPipelinePickableId = PickableHuooPipelineType.createPickableStringFrom(
       APPLICATION_NEW_PIPELINE_ID);

    when(applicationOnlyPipelineOverview.getPipelineId()).thenReturn(APPLICATION_NEW_PIPELINE_ID.asInt());
    when(applicationOnlyPipelineOverview.getPipelineType()).thenReturn(APPLICATION_NEW_PIPELINE_TYPE);

    importedConsentedPipelinePickableId = PickableHuooPipelineType.createPickableStringFrom(
        // same as consented as the same pipeline ID is represented
       CONSENTED_PIPELINE_ID);

  }

  @Before
  public void setup() {

    setupConsentedPipeline();
    setupApplicationPipelines();

    pickableHuooPipelineService = new PickableHuooPipelineService(
        pipelineAndIdentViewFactory,
        padOrganisationRoleService
    );

    var allPipelineMap = new HashMap<PipelineId, PipelineOverview>();
    allPipelineMap.put(CONSENTED_PIPELINE_ID, consentedPipelineOverview);
    allPipelineMap.put(APPLICATION_NEW_PIPELINE_ID, applicationOnlyPipelineOverview);
    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
        .thenReturn(allPipelineMap);

  }

  @Test
  public void getAllPickablePipelinesForApplicationAndRole_removesWholePipelineWhenSplitsExist() {

    var appPipelineSplit1 = PipelineSection.from(
        APPLICATION_NEW_PIPELINE_ID,
        1,
        PipelineIdentPoint.inclusivePoint("START"),
        PipelineIdentPoint.exclusivePoint("MID")
    );
    var appPipelineSplit2 = PipelineSection.from(
        APPLICATION_NEW_PIPELINE_ID,
        2,
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
       CONSENTED_PIPELINE_ID,
        APPLICATION_NEW_PIPELINE_ID
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
        .createOption(APPLICATION_NEW_PIPELINE_ID, "new");
    var importedPipelineOption = PickablePipelineOptionTestUtil
        .createOption(CONSENTED_PIPELINE_ID, "imported");

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
            CONSENTED_PIPELINE_ID),
        new ReconciledHuooPickablePipeline(
            PickableHuooPipelineId.from(applicationNewPipelinePickableId),
            APPLICATION_NEW_PIPELINE_ID)
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
}