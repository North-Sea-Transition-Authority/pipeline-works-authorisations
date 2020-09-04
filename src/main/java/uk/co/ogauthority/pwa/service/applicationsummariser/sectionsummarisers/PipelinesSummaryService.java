package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import static java.util.stream.Collectors.toMap;

import com.google.common.annotations.VisibleForTesting;
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
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.measurements.UnitMeasurement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentDiffableView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummary;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory;

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

    var applicationPipelineSummaryList = pipelineDiffableSummaryService.getApplicationDetailPipelines(
        pwaApplicationDetail);


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

    pipelineSummaryPairList.forEach((pipelineSummaryPair) -> {
      var pipelineDiffMap = new HashMap<String, Object>();
      diffedPipelineSummaryList.add(pipelineDiffMap);
      // we need to ignore the nested complex list of idents so we can do this diff separately
      // the diff service does not handle nested complex properties natively.
      pipelineDiffMap.put("pipelineHeader", diffService.diff(
          pipelineSummaryPair.getLeft().getPipelineHeaderView(), pipelineSummaryPair.getRight().getPipelineHeaderView(),
          Set.of("identViews")
      ));
      pipelineDiffMap.put("pipelineIdents",
          diffService.diffComplexLists(
              pipelineSummaryPair.getLeft().getIdentViews(),
              pipelineSummaryPair.getRight().getIdentViews(),
              // Simple mapping of idents. If the ident is in the same position, its considered to be the same ident.
              IdentDiffableView::getIdentNumber,
              IdentDiffableView::getIdentNumber
          )
      );
      pipelineDiffMap.put("drawingSummaryView", pipelineSummaryPair.getLeft().getDrawingSummaryView());

    });

    return diffedPipelineSummaryList;
  }

}
