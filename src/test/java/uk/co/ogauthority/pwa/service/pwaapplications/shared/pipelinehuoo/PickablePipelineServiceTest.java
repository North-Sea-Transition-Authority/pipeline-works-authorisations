package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PickablePipelineServiceTest {

  @Mock
  private PipelineService pipelineService;

  @Mock
  private PadPipelineService padPipelineService;

  private PickablePipelineService pickablePipelineService;

  private PwaApplicationDetail pwaApplicationDetail;
  private MasterPwa masterPwa;

  private Pipeline consentedPipeline;
  private PipelineDetail consentedPipelineDetail;
  private String consentedPipelinePickableId;

  private Pipeline applicationNewPipeline;
  private PadPipeline applicationNewPadPipeline;
  private String applicationNewPipelinePickableId;

  private Pipeline applicationImportedPipeline;
  private PadPipeline applicationImportedPadPipeline;
  private PipelineDetail applicationImportedPipelineDetail;
  private String applicationImportedPipelinePickableId;

  private void setupConsentedPipeline() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    masterPwa = pwaApplicationDetail.getPwaApplication().getMasterPwa();

    consentedPipeline = new Pipeline();
    consentedPipeline.setMasterPwa(masterPwa);
    consentedPipeline.setId(1);
    consentedPipelineDetail = new PipelineDetail(consentedPipeline);
    consentedPipelinePickableId = PickablePipelineType.CONSENTED.createIdString(consentedPipeline.getId());
  }

  private void setupApplicationPipelines() {
    applicationNewPipeline = new Pipeline(pwaApplicationDetail.getPwaApplication());
    applicationNewPipeline.setId(2);
    applicationNewPadPipeline = new PadPipeline(pwaApplicationDetail);
    applicationNewPadPipeline.setPipeline(applicationNewPipeline);
    applicationNewPadPipeline.setId(200);
    applicationNewPipelinePickableId = PickablePipelineType.APPLICATION.createIdString(
        applicationNewPadPipeline.getId());

    applicationImportedPipeline = new Pipeline(pwaApplicationDetail.getPwaApplication());
    applicationImportedPipeline.setId(3);
    applicationImportedPadPipeline = new PadPipeline(pwaApplicationDetail);
    applicationImportedPadPipeline.setPipeline(applicationImportedPipeline);
    applicationImportedPadPipeline.setId(300);
    applicationImportedPipelineDetail = new PipelineDetail(applicationImportedPipeline);
    applicationImportedPipelinePickableId = PickablePipelineType.APPLICATION.createIdString(
        applicationImportedPadPipeline.getId());
  }


  @Before
  public void setup() {

    setupConsentedPipeline();
    setupApplicationPipelines();

    pickablePipelineService = new PickablePipelineService(pipelineService, padPipelineService);

    when(padPipelineService.getAllPadPipelineSummaryDtosForApplicationDetail(pwaApplicationDetail))
        .thenReturn(
            List.of(generateFrom(applicationNewPadPipeline), generateFrom(applicationImportedPadPipeline))
        );

    when(pipelineService.getActivePipelineDetailsForApplicationMasterPwa(pwaApplicationDetail.getPwaApplication()))
        .thenReturn(
            List.of(consentedPipelineDetail, applicationImportedPipelineDetail)
        );

    // need to make sure we only return requested pipelines like real service
    when(pipelineService.getPipelinesFromIds(any())).thenAnswer(invocation -> {
      var pipelines = new HashSet<Pipeline>();
      Set<PipelineId> ids = invocation.getArgument(0);
      if (ids.contains(PipelineId.from(consentedPipeline))) {
        pipelines.add(consentedPipeline);
      }
      if (ids.contains(PipelineId.from(applicationImportedPipeline))) {
        pipelines.add(applicationImportedPipeline);
      }
      if (ids.contains(PipelineId.from(applicationNewPipeline))) {
        pipelines.add(applicationNewPipeline);
      }
      return pipelines;
    });

    // need to make sure we only return requested padpipelines like real service
    when(padPipelineService.getPadPipelinesByPadPipelineIds(any())).thenAnswer(invocation -> {
      var padPipelines = new ArrayList<PadPipeline>();
      Set<Integer> ids = invocation.getArgument(0);
      if (ids.contains(applicationNewPadPipeline.getId())) {
        padPipelines.add(applicationNewPadPipeline);
      }
      if (ids.contains(applicationImportedPadPipeline.getId())) {
        padPipelines.add(applicationImportedPadPipeline);
      }
      return padPipelines;
    });
  }

  private PadPipelineSummaryDto generateFrom(PadPipeline padPipeline) {

    return new PadPipelineSummaryDto(
        padPipeline.getId(),
        padPipeline.getPipeline().getId(),
        PipelineType.PRODUCTION_FLOWLINE,
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
        LongitudeDirection.EAST
    );

  }


  @Test
  public void getPickablePipelinesFromApplication_filtersOutConsentedPipelines() {

    var pickablePipelineOptions = pickablePipelineService.getPickablePipelinesFromApplication(pwaApplicationDetail);
    assertThat(pickablePipelineOptions).hasSize(2);

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption ->
        assertThat(pickablePipelineOption.getPickableString())
            .isEqualTo(applicationImportedPipelinePickableId)
    );

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption ->
        assertThat(pickablePipelineOption.getPickableString())
            .isEqualTo(applicationNewPipelinePickableId)
    );

  }


  @Test
  public void getPickablePipelinesFromApplicationMasterPwa_filtersOutApplicationPipelines() {

    var pickablePipelineOptions = pickablePipelineService.getPickablePipelinesFromApplicationMasterPwa(
        pwaApplicationDetail);
    assertThat(pickablePipelineOptions).hasOnlyOneElementSatisfying(pickablePipelineOption ->
        assertThat(pickablePipelineOption.getPickableString())
            .isEqualTo(consentedPipelinePickableId)
    );

  }

  @Test
  public void getAllPickablePipelinesForApplication_returnApplicationVersionOfImportedPipeline() {

    var pickablePipelineOptions = pickablePipelineService.getAllPickablePipelinesForApplication(pwaApplicationDetail);

    assertThat(pickablePipelineOptions).hasSize(3);

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption ->
        assertThat(pickablePipelineOption.getPickableString()).isEqualTo(applicationImportedPipelinePickableId)
    );

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption ->
        assertThat(pickablePipelineOption.getPickableString()).isEqualTo(applicationNewPipelinePickableId)
    );

    assertThat(pickablePipelineOptions).anySatisfy(pickablePipelineOption ->
        assertThat(pickablePipelineOption.getPickableString()).isEqualTo(consentedPipelinePickableId)
    );

  }

  @Test
  public void getPickedPipelinesFromStrings_allPipelinesFoundFromString() {
      var pipelines = pickablePipelineService.getPickedPipelinesFromStrings(Set.of(
          applicationImportedPipelinePickableId,
          applicationNewPipelinePickableId,
          consentedPipelinePickableId
      ));

      assertThat(pipelines).containsExactlyInAnyOrder(consentedPipeline, applicationNewPipeline, applicationImportedPipeline);

  }

  @Test
  public void getPickedPipelinesFromStrings_noPipelineFound() {
    var pipelines = pickablePipelineService.getPickedPipelinesFromStrings(Set.of(
        applicationImportedPipelinePickableId,
        applicationNewPipelinePickableId,
        consentedPipelinePickableId
    ));

    assertThat(pipelines).containsExactlyInAnyOrder(consentedPipeline, applicationNewPipeline, applicationImportedPipeline);

  }

  @Test
  public void reconcilePickablePipelineOptions_correctlyAssociatesPickableIdToPipelineId(){

    var consentedOption = PickablePipelineOptionTestUtil.createOption(consentedPipeline);
    var newPipelineOption = PickablePipelineOptionTestUtil.createOption(applicationNewPadPipeline);
    var importedPipelineOption = PickablePipelineOptionTestUtil.createOption(applicationImportedPadPipeline);

    var reconciledPipelines = pickablePipelineService.reconcilePickablePipelineOptions(
        Set.of(consentedOption, newPipelineOption, importedPipelineOption)
    );

    assertThat(reconciledPipelines).hasSize(3);
    assertThat(reconciledPipelines).containsExactlyInAnyOrder(
        new ReconciledPickablePipeline(
            PickablePipelineId.from(consentedPipelinePickableId),
            PipelineId.from(consentedPipeline)),
        new ReconciledPickablePipeline(
            PickablePipelineId.from(applicationImportedPipelinePickableId),
            PipelineId.from(applicationImportedPipeline)),
        new ReconciledPickablePipeline(
            PickablePipelineId.from(applicationNewPipelinePickableId),
            PipelineId.from(applicationNewPipeline))
    );

  }

}