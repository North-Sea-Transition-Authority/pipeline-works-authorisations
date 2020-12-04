package uk.co.ogauthority.pwa.service.documents.generation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.generation.DocumentSectionData;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
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
  public DocumentSectionData getDocumentSectionData(PwaApplicationDetail pwaApplicationDetail) {

    var depositForPipelinesMap = permanentDepositService.getDepositForDepositPipelinesMap(pwaApplicationDetail);
    var depositAndDrawingMap = depositDrawingsService.getDepositAndDrawingLinksMapForDeposits(
        depositForPipelinesMap.keySet());
    List<DepositTableRowView> depositTableRowViews = new ArrayList<>();

    depositForPipelinesMap.forEach((deposit, depositPipelines) -> {

      var pipelineIds = depositPipelines.stream()
          .map(padDepositPipeline -> padDepositPipeline.getPipeline().getPipelineId())
          .collect(Collectors.toList());

      var pipelineOverviews = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
          pwaApplicationDetail, pipelineIds).values();

      var drawingLinks = depositAndDrawingMap.getOrDefault(deposit, List.of());
      var drawingRefs = drawingLinks.stream().map(drawingLink -> drawingLink.getPadDepositDrawing().getReference())
          .collect(Collectors.toList());

      pipelineOverviews.forEach(pipelineOverview -> depositTableRowViews.add(
          mapDepositAndPipelinesToTableRowView(deposit, pipelineOverview.getPipelineNumber(), drawingRefs)));

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


  private DepositTableRowView mapDepositAndPipelinesToTableRowView(
      PadPermanentDeposit deposit, String pipelineColumnText, List<String> drawingRefs) {

    return new DepositTableRowView(
        pipelineColumnText,
        DateUtils.createDateEstimateString(deposit.getFromMonth(), deposit.getFromYear()) + "-" +
            DateUtils.createDateEstimateString(deposit.getToMonth(), deposit.getToYear()),
        deposit.getMaterialType().getDisplayText() + ", " + deposit.getMaterialSize(),
        String.valueOf(deposit.getQuantity()),
        deposit.getFromCoordinates(),
        deposit.getToCoordinates(),
        drawingRefs
    );

  }

}
