package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
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
  private final BlockCrossingService blockCrossingService;
  private final PadCableCrossingService padCableCrossingService;
  private final PadPipelineCrossingService padPipelineCrossingService;
  private final CrossingTypesService crossingTypesService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @Autowired
  public CrossingAgreementsService(
      PadMedianLineAgreementService padMedianLineAgreementService,
      BlockCrossingService blockCrossingService,
      PadCableCrossingService padCableCrossingService,
      PadPipelineCrossingService padPipelineCrossingService,
      CrossingTypesService crossingTypesService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService) {
    this.padMedianLineAgreementService = padMedianLineAgreementService;
    this.blockCrossingService = blockCrossingService;
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

    if (blockCrossingService.isComplete(detail)) {
      validSections.add(CrossingAgreementsSection.BLOCK_CROSSINGS);
    }

    if (padMedianLineAgreementService.isComplete(detail) || BooleanUtils.isFalse(detail.getMedianLineCrossed())) {
      validSections.add(CrossingAgreementsSection.MEDIAN_LINE);
    }

    if (padCableCrossingService.isComplete(detail) || BooleanUtils.isFalse(detail.getCablesCrossed())) {
      validSections.add(CrossingAgreementsSection.CABLE_CROSSINGS);
    }

    if (padPipelineCrossingService.isComplete(detail) || BooleanUtils.isFalse(detail.getPipelinesCrossed())) {
      validSections.add(CrossingAgreementsSection.PIPELINE_CROSSINGS);
    }

    return new CrossingAgreementsValidationResult(validSections);

  }

  public List<TaskListEntry> getTaskListItems(PwaApplicationDetail pwaApplicationDetail) {
    return CrossingAgreementTask.stream()
        .sorted(Comparator.comparing(CrossingAgreementTask::getDisplayOrder))
        .map(crossingAgreementTask -> createTaskListEntry(pwaApplicationDetail, crossingAgreementTask))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  @VisibleForTesting
  public Optional<TaskListEntry> createTaskListEntry(PwaApplicationDetail detail, CrossingAgreementTask task) {
    var service = crossingAgreementsTaskListService.getServiceBean(task);
    if (!service.canShowInTaskList(detail)) {
      return Optional.empty();
    }
    var taskListEntry = new TaskListEntry(
        task.getDisplayText(),
        crossingAgreementsTaskListService.getRoute(detail, task),
        service.isComplete(detail),
        service.getTaskInfoList(detail)
    );
    return Optional.of(taskListEntry);
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
