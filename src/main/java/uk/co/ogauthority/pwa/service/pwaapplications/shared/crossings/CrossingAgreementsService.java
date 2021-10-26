package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
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
    var validSections = EnumSet.allOf(CrossingAgreementsSection.class);
    var sectionsRequiredForAppType = getSectionsRequiredForAppType(detail.getPwaApplicationType());

    if (sectionsRequiredForAppType.contains(CrossingAgreementsSection.CROSSING_TYPES) && !crossingTypesService.isComplete(detail)) {
      validSections.remove(CrossingAgreementsSection.CROSSING_TYPES);
    }

    if (sectionsRequiredForAppType.contains(CrossingAgreementsSection.BLOCK_CROSSINGS) && !blockCrossingService.isComplete(detail)) {
      validSections.remove(CrossingAgreementsSection.BLOCK_CROSSINGS);
    }

    if (sectionsRequiredForAppType.contains(CrossingAgreementsSection.MEDIAN_LINE)
        && (!padMedianLineAgreementService.isComplete(detail) && BooleanUtils.isTrue(detail.getMedianLineCrossed()))) {
      validSections.remove(CrossingAgreementsSection.MEDIAN_LINE);
    }

    if (sectionsRequiredForAppType.contains(CrossingAgreementsSection.CABLE_CROSSINGS)
        && (!padCableCrossingService.isComplete(detail) && BooleanUtils.isTrue(detail.getCablesCrossed()))) {
      validSections.remove(CrossingAgreementsSection.CABLE_CROSSINGS);
    }

    if (sectionsRequiredForAppType.contains(CrossingAgreementsSection.PIPELINE_CROSSINGS)
        && (!padPipelineCrossingService.isComplete(detail) && BooleanUtils.isTrue(detail.getPipelinesCrossed()))) {
      validSections.remove(CrossingAgreementsSection.PIPELINE_CROSSINGS);
    }

    return new CrossingAgreementsValidationResult(validSections);

  }

  private EnumSet<CrossingAgreementsSection> getSectionsRequiredForAppType(PwaApplicationType pwaApplicationType) {
    if (pwaApplicationType.equals(PwaApplicationType.DEPOSIT_CONSENT)) {
      return EnumSet.of(CrossingAgreementsSection.BLOCK_CROSSINGS);
    }
    return EnumSet.allOf(CrossingAgreementsSection.class);
  }

  public List<TaskListEntry> getTaskListItems(PwaApplicationDetail pwaApplicationDetail) {

    var appType = pwaApplicationDetail.getPwaApplicationType();

    return CrossingAgreementTask.stream()
        .sorted(Comparator.comparing(CrossingAgreementTask::getDisplayOrder))
        .filter(crossingAgreementTask -> appType != PwaApplicationType.DEPOSIT_CONSENT
            || !crossingAgreementTask.equals(CrossingAgreementTask.CROSSING_TYPES))
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
        service.getTaskInfoList(detail),
        task.getDisplayOrder()
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

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    blockCrossingService.copySectionInformation(fromDetail, toDetail);
    if (BooleanUtils.isTrue(fromDetail.getCablesCrossed())) {
      padCableCrossingService.copySectionInformation(fromDetail, toDetail);
    }
    if (BooleanUtils.isTrue(fromDetail.getPipelinesCrossed())) {
      padPipelineCrossingService.copySectionInformation(fromDetail, toDetail);
    }
    if (BooleanUtils.isTrue(fromDetail.getMedianLineCrossed())) {
      padMedianLineAgreementService.copySectionInformation(fromDetail, toDetail);
    }
  }
}
