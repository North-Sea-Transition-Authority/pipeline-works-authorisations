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
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
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

      var pipelineNumbersDisplay = pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
          pwaApplicationDetail, pipelineIds).values()
          .stream().sorted(Comparator.comparing(PipelineOverview::getPipelineNumber))
          .map(PipelineOverview::getPipelineNumber)
          .collect(Collectors.joining(", "));

      var drawingRefs = getDrawingRefs(deposit, depositAndDrawingMap);

      depositTableRowViews.add(mapDepositAndPipelinesToTableRowView(deposit, pipelineNumbersDisplay, drawingRefs));
    });

    depositsWithPipelinesFromOtherApps.forEach(deposit -> {
      var drawingRefs = getDrawingRefs(deposit, depositAndDrawingMap);
      if (deposit.getDepositIsForPipelinesOnOtherApp()) {
        depositTableRowViews.add(mapDepositAndPipelinesToTableRowView(
            deposit, deposit.getAppRefAndPipelineNum(), drawingRefs));
      }
    });

    depositTableRowViews.sort(Comparator.comparing(DepositTableRowView::getDepositReference));

    var depositFootnotes = allDeposits.stream()
        .filter(deposit -> deposit.getFootnote() != null)
        .sorted(Comparator.comparing(PadPermanentDeposit::getReference))
        .map(deposit -> String.format("%s: %s", deposit.getReference(), deposit.getFootnote()))
        .collect(Collectors.toList());

    Map<String, Object> modelMap = Map.of(
        "sectionName", DocumentSection.DEPOSITS.getDisplayName(),
        "depositTableRowViews", depositTableRowViews,
        "depositFootnotes", depositFootnotes
    );

    return new DocumentSectionData("documents/consents/sections/deposits", modelMap);

  }


  private List<String> getDrawingRefs(PadPermanentDeposit deposit,
                                      Map<PadPermanentDeposit, List<PadDepositDrawingLink>> depositAndDrawingMap) {
    var drawingLinks = depositAndDrawingMap.getOrDefault(deposit, List.of());
    return drawingLinks.stream().map(drawingLink -> drawingLink.getPadDepositDrawing().getReference())
        .collect(Collectors.toList());
  }

  private String getUnitMeasurementDisplay(PadPermanentDeposit deposit) {
    var unitMeasurementDisplay = "";
    if (deposit.getMaterialType().equals(MaterialType.ROCK)) {
      unitMeasurementDisplay = " " + UnitMeasurement.ROCK_GRADE.getSuffixDisplay();

    } else if (deposit.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      unitMeasurementDisplay = UnitMeasurement.KILOGRAM.getSuffixDisplay();
    }

    return unitMeasurementDisplay;
  }

  private String getMaterialSizeDisplay(PadPermanentDeposit deposit) {
    var joiner = ", ";
    var materialSizeDisplay = joiner + deposit.getMaterialSize();

    if (deposit.getMaterialType().equals(MaterialType.CONCRETE_MATTRESSES)) {
      materialSizeDisplay = joiner + deposit.getConcreteMattressLength() + "x" +
          deposit.getConcreteMattressWidth() + "x" + deposit.getConcreteMattressDepth();
    }

    return materialSizeDisplay;
  }

  private String getMaterialsPropertiesDisplay(PadPermanentDeposit deposit) {
    if (deposit.getMaterialType().equals(MaterialType.GROUT_BAGS)) {
      return deposit.getGroutBagsBioDegradable() ? ", Biodegradable" : ", Non-biodegradable";
    }
    return "";
  }


  private DepositTableRowView mapDepositAndPipelinesToTableRowView(
      PadPermanentDeposit deposit, String pipelineColumnText, List<String> drawingRefs) {

    var proposedDate = DateUtils.createDateEstimateString(deposit.getFromMonth(), deposit.getFromYear()) + "-" +
        DateUtils.createDateEstimateString(deposit.getToMonth(), deposit.getToYear());

    var typeAndSizeOfMaterials = deposit.getMaterialType().getDisplayText() +
        getMaterialSizeDisplay(deposit) + getUnitMeasurementDisplay(deposit) + getMaterialsPropertiesDisplay(deposit);

    return new DepositTableRowView(
        deposit.getReference(),
        pipelineColumnText,
        proposedDate,
        typeAndSizeOfMaterials,
        deposit.getQuantity() % 1 == 0 ? String.valueOf((int) deposit.getQuantity()) : String.valueOf(deposit.getQuantity()),
        deposit.getFromCoordinates(),
        deposit.getToCoordinates(),
        drawingRefs
    );

  }

}
