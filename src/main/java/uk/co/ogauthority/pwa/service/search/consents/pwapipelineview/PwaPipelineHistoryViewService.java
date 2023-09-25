package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static java.util.stream.Collectors.groupingBy;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class PwaPipelineHistoryViewService {

  private final PipelineDiffableSummaryService pipelineDiffableSummaryService;
  private final PipelinesSummaryService pipelinesSummaryService;
  private final PipelineDetailService pipelineDetailService;


  @Autowired
  public PwaPipelineHistoryViewService(PipelineDiffableSummaryService pipelineDiffableSummaryService,
                                       PipelinesSummaryService pipelinesSummaryService,
                                       PipelineDetailService pipelineDetailService) {
    this.pipelineDiffableSummaryService = pipelineDiffableSummaryService;
    this.pipelinesSummaryService = pipelinesSummaryService;
    this.pipelineDetailService = pipelineDetailService;
  }


  private String createPipelineVersionOption(PipelineDetail pipelineDetail, Integer order) {

    var orderTagDisplay = order != null ? String.format(" (%s)", order) : "";
    var consentReferenceDisplay = pipelineDetail.getPwaConsent().getReference() != null
        ? " - " + pipelineDetail.getPwaConsent().getReference() : "";
    return DateUtils.formatDate(pipelineDetail.getStartTimestamp()) + orderTagDisplay + consentReferenceDisplay;
  }

  public Map<String, String> getPipelinesVersionSearchSelectorItems(Integer pipelineId) {

    var pipelineDetails = pipelineDetailService.getAllPipelineDetailsForPipeline(new PipelineId(pipelineId));
    //group all the details for the pipeline by the day they were created (for easier order tagging of pipelines changed on the same day)
    var dateToPipelineDetailsMap = pipelineDetails.stream()
        .sorted(Comparator.comparing(PipelineDetail::getStartTimestamp).reversed())
        .collect(groupingBy(pipelineDetail ->
                DateUtils.instantToLocalDate(pipelineDetail.getStartTimestamp()), LinkedHashMap::new, Collectors.toList()));

    Map<String, String> detailIdToOptionMap = new LinkedHashMap<>();
    dateToPipelineDetailsMap.forEach((startDate, pipelineDetailsForDate) -> {
      //this list of pipeline details are already ordered from newest
      for (var x  = 0; x < pipelineDetailsForDate.size(); x++) {
        var pipelineDetail = pipelineDetailsForDate.get(x);
        var pipelineOrderTagNumber = pipelineDetailsForDate.size() > 1 ? pipelineDetailsForDate.size() - x : null;
        detailIdToOptionMap.put(
            pipelineDetail.getId().toString(), createPipelineVersionOption(pipelineDetail, pipelineOrderTagNumber));
      }
    });

    var latestPipelineVersionEntryOpt = detailIdToOptionMap.entrySet().stream().findFirst();
    if (latestPipelineVersionEntryOpt.isPresent()) {
      var latestPipelineVersionEntry = latestPipelineVersionEntryOpt.get();
      latestPipelineVersionEntry.setValue(String.format("Latest version (%s)", latestPipelineVersionEntry.getValue()));
    }

    return detailIdToOptionMap;
  }



  public Map<String, Object> getDiffedPipelineSummaryModel(Integer selectedPipelineDetailId, Integer pipelineId) {

    PipelineDetail previousVersionPipelineDetail = null;
    var pipelineDetails = pipelineDetailService.getAllPipelineDetailsForPipeline(new PipelineId(pipelineId))
        .stream().sorted(Comparator.comparing(PipelineDetail::getStartTimestamp))
        .collect(Collectors.toList());

    for (var x = 0; x < pipelineDetails.size(); x++) {
      if (pipelineDetails.get(x).getId().equals(selectedPipelineDetailId) && x - 1 >= 0) {
        previousVersionPipelineDetail = pipelineDetails.get(x - 1);
        break;
      }
    }

    var selectedDetailDiffableSummary = pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(selectedPipelineDetailId);
    var previousDetailDiffableSummary = previousVersionPipelineDetail != null
        ? pipelineDiffableSummaryService.getConsentedPipelineDetailSummary(previousVersionPipelineDetail.getId()) : selectedDetailDiffableSummary;
    return pipelinesSummaryService.produceDiffedPipelineModel(selectedDetailDiffableSummary, previousDetailDiffableSummary);

  }


}
