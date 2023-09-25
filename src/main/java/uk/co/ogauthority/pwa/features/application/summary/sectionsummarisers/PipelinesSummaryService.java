package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import static java.util.stream.Collectors.toMap;

import com.google.common.annotations.VisibleForTesting;
import io.micrometer.core.instrument.util.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingUrlFactory;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentDiffableView;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;

/**
 * Construct summary of pipelines for a given application.
 */
@Service
public class PipelinesSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PipelineDiffableSummaryService pipelineDiffableSummaryService;
  private final DiffService diffService;

  @Autowired
  public PipelinesSummaryService(
      TaskListService taskListService,
      PipelineDiffableSummaryService pipelineDiffableSummaryService,
      DiffService diffService) {
    this.taskListService = taskListService;
    this.pipelineDiffableSummaryService = pipelineDiffableSummaryService;
    this.diffService = diffService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PIPELINES);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var applicationPipelineSummaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(pwaApplicationDetail);

    var consentedPipelineSummaryList = pipelineDiffableSummaryService.getConsentedPipelines(
        pwaApplicationDetail.getPwaApplication(),
        applicationPipelineSummaryList.stream()
            .map(PipelineDiffableSummary::getPipelineId)
            .collect(Collectors.toSet())
    );

    var diffedPipelineSummaryList = getDiffedPipelineSummaryList(applicationPipelineSummaryList, consentedPipelineSummaryList);

    var sectionDisplayText = ApplicationTask.PIPELINES.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("pipelines", diffedPipelineSummaryList);
    summaryModel.put("pipelineDrawingUrlFactory", new PipelineDrawingUrlFactory(pwaApplicationDetail));
    summaryModel.put("unitMeasurements", UnitMeasurement.toMap());
    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#pipelinesHeader"
        )),
        summaryModel
    );
  }


  public Map<String, Object> produceDiffedPipelineModel(PipelineDiffableSummary currentSummaryView,
                                                        PipelineDiffableSummary previousSummaryView) {

    var pipelineDiffMap = new HashMap<String, Object>();
    // we need to ignore the nested complex list of idents so we can do this diff separately
    // the diff service does not handle nested complex properties natively.
    var currentPipelineHeaderView = currentSummaryView.getPipelineHeaderView();
    var previousPipelineHeaderView = previousSummaryView.getPipelineHeaderView();
    Map<String, Object> pipelineHeaderMap = new HashMap<>(diffService.diff(
        currentPipelineHeaderView, previousPipelineHeaderView,
        Set.of("identViews", "pipelineStatus", "headerQuestions")));
    pipelineHeaderMap.put("headerQuestions", currentSummaryView.getHeaderQuestions());
    pipelineHeaderMap.put("hasTemporaryPipelineNumber", StringUtils.isNotEmpty(currentPipelineHeaderView.getTemporaryPipelineNumber()));
    pipelineHeaderMap.put("canShowFootnote",
        currentPipelineHeaderView.getFootnote() != null || previousPipelineHeaderView.getFootnote() != null);

    pipelineDiffMap.put("pipelineHeader", pipelineHeaderMap);
    pipelineDiffMap.put("pipelineIdents",
        diffService.diffComplexLists(
            currentSummaryView.getIdentViews(),
            previousSummaryView.getIdentViews(),
            // Simple mapping of idents. If the ident is in the same position, it's considered to be the same ident.
            IdentDiffableView::getIdentNumber,
            IdentDiffableView::getIdentNumber
        )
    );
    pipelineDiffMap.put("drawingSummaryView", currentSummaryView.getDrawingSummaryView());
    return pipelineDiffMap;
  }


  @VisibleForTesting
  List<Map<String, ?>> getDiffedPipelineSummaryList(List<PipelineDiffableSummary> applicationPipelines,
                                                    List<PipelineDiffableSummary> consentedPipelines) {

    List<Map<String, ?>> diffedPipelineSummaryList = new ArrayList<>();

    Map<PipelineId, PipelineDiffableSummary> consentedPipelinesMap = consentedPipelines.stream()
        .collect(toMap(PipelineDiffableSummary::getPipelineId, o -> o));

    // Cant remove pipelines from an application once they are in the consented model, can change their status only, e.g not laid.
    // Use a Pair where the left is the current version of the pipeline, and the right is the consented version.
    List<ImmutablePair<PipelineDiffableSummary, PipelineDiffableSummary>> pipelineSummaryPairList = applicationPipelines.stream()
        .sorted(Comparator.comparing(x -> x.getPipelineHeaderView().getPipelineName()))
        .map(applicationPipelineSummary -> new ImmutablePair<>(
                applicationPipelineSummary,
                consentedPipelinesMap.getOrDefault(applicationPipelineSummary.getPipelineId(), PipelineDiffableSummary.empty())
            )
        )
        .collect(Collectors.toList());

    pipelineSummaryPairList.forEach(pipelineSummaryPair -> {
      var currentPipelineDiffableSummary = pipelineSummaryPair.getLeft();
      var consentedPipelineDiffableSummary = pipelineSummaryPair.getRight();
      var pipelineDiffMap = produceDiffedPipelineModel(currentPipelineDiffableSummary, consentedPipelineDiffableSummary);
      diffedPipelineSummaryList.add(pipelineDiffMap);
    });

    return diffedPipelineSummaryList;
  }

}
