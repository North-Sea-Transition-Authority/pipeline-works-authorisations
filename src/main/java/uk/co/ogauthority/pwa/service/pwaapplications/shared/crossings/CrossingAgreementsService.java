package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;

@Service
public class CrossingAgreementsService implements ApplicationFormSectionService {

  private final PadMedianLineAgreementService padMedianLineAgreementService;
  private final BlockCrossingFileService blockCrossingFileService;
  private final PadCableCrossingService padCableCrossingService;
  private final PadPipelineCrossingService padPipelineCrossingService;
  private final CrossingTypesService crossingTypesService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @Autowired
  public CrossingAgreementsService(
      PadMedianLineAgreementService padMedianLineAgreementService,
      BlockCrossingFileService blockCrossingFileService,
      PadCableCrossingService padCableCrossingService,
      PadPipelineCrossingService padPipelineCrossingService,
      CrossingTypesService crossingTypesService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.blockCrossingFileService = blockCrossingFileService;
    this.padCableCrossingService = padCableCrossingService;
    this.padPipelineCrossingService = padPipelineCrossingService;
    this.crossingTypesService = crossingTypesService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
  }

  public CrossingAgreementsValidationResult getValidationResult(PwaApplicationDetail detail) {
    var validSections = EnumSet.noneOf(CrossingAgreementsSection.class);

    if (crossingTypesService.isComplete(detail)) {
      validSections.add(CrossingAgreementsSection.CROSSING_TYPES);
    }

    if (blockCrossingFileService.isComplete(detail)) {
      validSections.add(CrossingAgreementsSection.BLOCK_CROSSINGS);
    }

    if (padMedianLineAgreementService.isComplete(detail) || !BooleanUtils.isTrue(detail.getMedianLineCrossed())) {
      validSections.add(CrossingAgreementsSection.MEDIAN_LINE);
    }

    if (padCableCrossingService.isComplete(detail) || !BooleanUtils.isTrue(detail.getCablesCrossed())) {
      validSections.add(CrossingAgreementsSection.CABLE_CROSSINGS);
    }

    if (padPipelineCrossingService.isComplete(detail) || !BooleanUtils.isTrue(detail.getPipelinesCrossed())) {
      validSections.add(CrossingAgreementsSection.PIPELINE_CROSSINGS);
    }

    return new CrossingAgreementsValidationResult(validSections);

  }

  public List<TaskListEntry> getTaskListItems(PwaApplicationDetail pwaApplicationDetail) {
    var list = new ArrayList<TaskListEntry>();

    var sortedTasks = CrossingAgreementTask.stream()
        .sorted(Comparator.comparing(CrossingAgreementTask::getDisplayOrder))
        .collect(Collectors.toList());

    for (var task : sortedTasks) {
      var service = crossingAgreementsTaskListService.getServiceBean(task);
      if (service.getCanShowInTaskList(pwaApplicationDetail)) {
        list.add(new TaskListEntry(
            task.getDisplayText(),
            crossingAgreementsTaskListService.getRoute(pwaApplicationDetail, task),
            service.isTaskListEntryCompleted(pwaApplicationDetail),
            service.getTaskListLabels(pwaApplicationDetail)
        ));
      }
    }

    return list;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return getValidationResult(detail).isCrossingAgreementsValid();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }
}
