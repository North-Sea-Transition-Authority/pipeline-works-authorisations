package uk.co.ogauthority.pwa.controller.documents.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.service.documents.generation.DepositsGeneratorService;
import uk.co.ogauthority.pwa.service.documents.views.DepositTableRowView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class DepositGeneratorServiceTest {

  @Mock
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Mock
  private DepositDrawingsService depositDrawingsService;

  private PwaApplicationDetail pwaApplicationDetail;

  private DepositsGeneratorService depositsGeneratorService;


  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL, 1, 1);
    depositsGeneratorService = new DepositsGeneratorService(pipelineAndIdentViewFactory, permanentDepositService, depositDrawingsService);
  }



  private PadPermanentDeposit createDeposit(int id) {
    var deposit =  PadPermanentDepositTestUtil.createRockPadDeposit(
        id, "ref " + id, pwaApplicationDetail, "3", 5, null,
        LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 5),
        CoordinatePairTestUtil.getDefaultCoordinate(), CoordinatePairTestUtil.getDefaultCoordinate());
        deposit.setDepositIsForPipelinesOnOtherApp(false);
        return deposit;
  }

  private PadDepositDrawing createDepositDrawing(int id) {
    var drawing = new PadDepositDrawing();
    drawing.setId(id);
    drawing.setReference("drawing ref " + id);
    return drawing;
  }

  private Pipeline createPipeline(int id) {
    var pipeline = new Pipeline();
    pipeline.setId(id);
    return pipeline;
  }

  private PipelineOverview createPipelineOverview(Pipeline pipeline, String pipelineNumber) {
    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setPipelineStatus(PipelineStatus.IN_SERVICE);
    pipelineDetail.setPipeline(pipeline);
    pipelineDetail.setPipelineNumber(pipelineNumber);
    return new PipelineHeaderView(pipelineDetail);
  }



  @Test
  public void getDocumentSectionData() {

    var deposit1 = createDeposit(1);
    var deposit2 = createDeposit(2);
    var deposit3 = createDeposit(3);
    deposit2.setDepositIsForPipelinesOnOtherApp(true);
    deposit2.setAppRefAndPipelineNum("App refs and pipeline numbers abc");
    deposit3.setDepositIsForPipelinesOnOtherApp(true);
    deposit3.setAppRefAndPipelineNum("App refs and pipeline numbers xyz");

    //deposits for pipelines
    var pipeline1 = createPipeline(1);
    var pipeline2 = createPipeline(2);

    var deposit1AndPipeline1 = PadPermanentDepositTestUtil.createDepositPipeline(deposit1, pipeline1);
    var deposit1AndPipeline2 = PadPermanentDepositTestUtil.createDepositPipeline(deposit1, pipeline2);
    var deposit2AndPipeline1 = PadPermanentDepositTestUtil.createDepositPipeline(deposit2, pipeline1);

    Map<PadPermanentDeposit, List<PadDepositPipeline>> depositForPipelinesMap = new HashMap<>();
    depositForPipelinesMap.put(deposit1, List.of(deposit1AndPipeline1, deposit1AndPipeline2));
    depositForPipelinesMap.put(deposit2, List.of(deposit2AndPipeline1));

    when(permanentDepositService.getDepositForDepositPipelinesMap(pwaApplicationDetail)).thenReturn(depositForPipelinesMap);


    //deposits that have pipelines on other apps
    when(permanentDepositService.getAllDepositsWithPipelinesFromOtherApps(pwaApplicationDetail)).thenReturn(List.of(deposit2, deposit3));


    //deposit drawings
    var drawing1 = createDepositDrawing(1);
    var drawing2 =  createDepositDrawing(2);

    var deposit1drawingLink1 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit1, drawing1);
    var deposit1drawingLink2 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit1, drawing2);
    var deposit2drawingLink1 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit2, drawing1);
    var deposit3drawingLink1 = PadPermanentDepositTestUtil.createPadDepositDrawingLink(deposit3, drawing1);

    var allDeposits = new ArrayList<PadPermanentDeposit>();
    allDeposits.addAll(List.of(deposit2, deposit3));
    allDeposits.addAll(depositForPipelinesMap.keySet());

    when(depositDrawingsService.getDepositAndDrawingLinksMapForDeposits(allDeposits)).thenReturn(Map.of(
        deposit1, List.of(deposit1drawingLink1, deposit1drawingLink2),
        deposit2, List.of(deposit2drawingLink1),
        deposit3, List.of(deposit3drawingLink1)
    ));


    //pipeline overviews
    var overviewForPipeline1 = createPipelineOverview(pipeline1, "PL1");
    var overviewForPipeline2 = createPipelineOverview(pipeline2, "PL2");

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
        pwaApplicationDetail, List.of(pipeline1.getPipelineId(), pipeline2.getPipelineId())))
        .thenReturn(Map.of(
          pipeline1.getPipelineId(), overviewForPipeline1,
          pipeline2.getPipelineId(), overviewForPipeline2)
        );

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
        pwaApplicationDetail, List.of(pipeline1.getPipelineId()))).thenReturn(Map.of(
        pipeline1.getPipelineId(), overviewForPipeline1
    ));



    var documentSectionData = depositsGeneratorService.getDocumentSectionData(pwaApplicationDetail);
    var depositTableRowViews = (List<DepositTableRowView>) documentSectionData.getTemplateModel().get("depositTableRowViews");
    var sectionName = documentSectionData.getTemplateModel().get("sectionName");

    assertThat(sectionName).isEqualTo(DocumentSection.DEPOSITS.getDisplayName());
    assertThat(depositTableRowViews).hasSize(5);

    var expectedProposedStartDateDeposit1 = DateUtils.createDateEstimateString(deposit1.getFromMonth(), deposit1.getFromYear()) + "-" +
        DateUtils.createDateEstimateString(deposit1.getToMonth(), deposit1.getToYear());

    var expectedTableRowViewForDep1AndPipeline1 = new DepositTableRowView(
        overviewForPipeline1.getPipelineNumber(),
        expectedProposedStartDateDeposit1,
        deposit1.getMaterialType().getDisplayText() + ", " + deposit1.getMaterialSize() + " " + UnitMeasurement.ROCK_GRADE.getSuffixDisplay(),
        String.valueOf((int) deposit1.getQuantity()),
        deposit1.getFromCoordinates(),
        deposit1.getToCoordinates(),
        List.of(drawing1.getReference(), drawing2.getReference())
    );

    var expectedTableRowViewForDep1AndPipeline2 = new DepositTableRowView(
        overviewForPipeline2.getPipelineNumber(),
        expectedProposedStartDateDeposit1,
        deposit1.getMaterialType().getDisplayText() + ", " + deposit1.getMaterialSize() + " " + UnitMeasurement.ROCK_GRADE.getSuffixDisplay(),
        String.valueOf((int) deposit1.getQuantity()),
        deposit1.getFromCoordinates(),
        deposit1.getToCoordinates(),
        List.of(drawing1.getReference(), drawing2.getReference())
    );


    var expectedProposedStartDateDeposit2 = DateUtils.createDateEstimateString(deposit2.getFromMonth(), deposit2.getFromYear()) + "-" +
        DateUtils.createDateEstimateString(deposit2.getToMonth(), deposit2.getToYear());

    var expectedTableRowViewForDep2AndPipeline1 = new DepositTableRowView(
        overviewForPipeline1.getPipelineNumber(),
        expectedProposedStartDateDeposit2,
        deposit2.getMaterialType().getDisplayText() + ", " + deposit2.getMaterialSize() + " " + UnitMeasurement.ROCK_GRADE.getSuffixDisplay(),
        String.valueOf((int) deposit2.getQuantity()),
        deposit2.getFromCoordinates(),
        deposit2.getToCoordinates(),
        List.of(drawing1.getReference())
    );

    var expectedTableRowViewForDep2AndPipelineFreeText = new DepositTableRowView(
        deposit2.getAppRefAndPipelineNum(),
        expectedProposedStartDateDeposit2,
        deposit2.getMaterialType().getDisplayText() + ", " + deposit2.getMaterialSize() + " " + UnitMeasurement.ROCK_GRADE.getSuffixDisplay(),
        String.valueOf((int) deposit2.getQuantity()),
        deposit2.getFromCoordinates(),
        deposit2.getToCoordinates(),
        List.of(drawing1.getReference())
    );

    var expectedTableRowViewForDep3AndPipelineFreeText = new DepositTableRowView(
        deposit3.getAppRefAndPipelineNum(),
        expectedProposedStartDateDeposit2,
        deposit3.getMaterialType().getDisplayText() + ", " + deposit3.getMaterialSize() + " " + UnitMeasurement.ROCK_GRADE.getSuffixDisplay(),
        String.valueOf((int) deposit3.getQuantity()),
        deposit3.getFromCoordinates(),
        deposit3.getToCoordinates(),
        List.of(drawing1.getReference())
    );

    assertThat(depositTableRowViews.get(0)).isEqualTo(expectedTableRowViewForDep2AndPipelineFreeText);
    assertThat(depositTableRowViews.get(1)).isEqualTo(expectedTableRowViewForDep3AndPipelineFreeText);
    assertThat(depositTableRowViews.get(2)).isIn(expectedTableRowViewForDep1AndPipeline1, expectedTableRowViewForDep2AndPipeline1);
    assertThat(depositTableRowViews.get(3)).isIn(expectedTableRowViewForDep1AndPipeline1, expectedTableRowViewForDep2AndPipeline1);
    assertThat(depositTableRowViews.get(4)).isEqualTo(expectedTableRowViewForDep1AndPipeline2);
  }

}
