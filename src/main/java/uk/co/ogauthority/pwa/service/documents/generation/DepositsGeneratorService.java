package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdepositdrawings.PadDepositDrawingLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.service.documents.views.DepositTableRowView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.DepositDrawingsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class DepositsGeneratorService implements DocumentSectionGenerator {


  private final PipelineAndIdentViewFactory pipelineAndIdentViewFactory;
  private final PermanentDepositService permanentDepositService;
  private final DepositDrawingsService depositDrawingsService;

  @Autowired
  public DepositsGeneratorService(
      PipelineAndIdentViewFactory pipelineAndIdentViewFactory,
      PermanentDepositService permanentDepositService,
      DepositDrawingsService depositDrawingsService) {
    this.pipelineAndIdentViewFactory = pipelineAndIdentViewFactory;
    this.permanentDepositService = permanentDepositService;
    this.depositDrawingsService = depositDrawingsService;
  }

  @Override
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail,
                                                    DocumentInstance documentInstance) {

    var depositForPipelinesMap = permanentDepositService.getDepositForDepositPipelinesMap(pwaApplicationDetail);
    var depositsWithPipelinesFromOtherApps = permanentDepositService.getAllDepositsWithPipelinesFromOtherApps(pwaApplicationDetail);
    var allDeposits = Stream.of(depositsWithPipelinesFromOtherApps, depositForPipelinesMap.keySet())
        .flatMap(Collection::stream).collect(Collectors.toList());

    // short-circuit early if no deposits, nothing to show
    if (allDeposits.isEmpty()) {
      return null;
    }

    var depositAndDrawingMap = depositDrawingsService.getDepositAndDrawingLinksMapForDeposits(allDeposits);
    List<DepositTableRowView> depositTableRowViews = new ArrayList<>();

    depositForPipelinesMap.forEach((deposit, depositPipelines) -> {

      var pipelineIds = depositPipelines.stream()
          .map(padDepositPipeline -> padDepositPipeline.getPipeline().getPipelineId())
          .collect(Collectors.toList());

      var pipelineOverviews = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
          pwaApplicationDetail, pipelineIds).values();

      var drawingRefs = getDrawingRefs(deposit, depositAndDrawingMap);

      pipelineOverviews.forEach(pipelineOverview -> depositTableRowViews.add(
          mapDepositAndPipelinesToTableRowView(deposit, pipelineOverview.getPipelineNumber(), drawingRefs)));
    });

    depositsWithPipelinesFromOtherApps.forEach(deposit -> {
      var drawingRefs = getDrawingRefs(deposit, depositAndDrawingMap);
      if (deposit.getDepositIsForPipelinesOnOtherApp()) {
        depositTableRowViews.add(mapDepositAndPipelinesToTableRowView(
            deposit, deposit.getAppRefAndPipelineNum(), drawingRefs));
      }
    });

    depositTableRowViews.sort(Comparator.comparing(DepositTableRowView::getPipelineNumber));

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.DEPOSITS.getDisplayName(),
        "depositTableRowViews", depositTableRowViews
    );

    return new DocumentSectionData("documents/consents/sections/deposits", modelMap);

  }


  private List<String> getDrawingRefs(PadPermanentDeposit deposit,
                                      Map<PadPermanentDeposit, List<PadDepositDrawingLink>> depositAndDrawingMap) {
    var drawingLinks = depositAndDrawingMap.getOrDefault(deposit, List.of());
    return drawingLinks.stream().map(drawingLink -> drawingLink.getPadDepositDrawing().getReference())
        .collect(Collectors.toList());
  }


  private DepositTableRowView mapDepositAndPipelinesToTableRowView(
      PadPermanentDeposit deposit, String pipelineColumnText, List<String> drawingRefs) {

    var materialUnitMeasurement = deposit.getMaterialType().equals(MaterialType.ROCK) ? UnitMeasurement.ROCK_GRADE.getSuffixDisplay() : "";
    var materialSize = deposit.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)
        ? deposit.getConcreteMattressLength() + "x" + deposit.getConcreteMattressWidth() + "x" + deposit.getConcreteMattressDepth()
        : deposit.getMaterialSize();

    return new DepositTableRowView(
        pipelineColumnText,
        DateUtils.createDateEstimateString(deposit.getFromMonth(), deposit.getFromYear()) + "-" +
            DateUtils.createDateEstimateString(deposit.getToMonth(), deposit.getToYear()),
        deposit.getMaterialType().getDisplayText() + ", " + materialSize + " " + materialUnitMeasurement,
        deposit.getQuantity() % 1 == 0 ? String.valueOf((int) deposit.getQuantity()) : String.valueOf(deposit.getQuantity()),
        deposit.getFromCoordinates(),
        deposit.getToCoordinates(),
        drawingRefs
    );

  }

}
