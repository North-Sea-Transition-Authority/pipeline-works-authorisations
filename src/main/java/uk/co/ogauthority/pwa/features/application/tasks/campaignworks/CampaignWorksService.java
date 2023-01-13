package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.appdetailreconciliation.PadPipelineReconcilerService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.util.CleanupUtils;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@Service
public class CampaignWorksService implements ApplicationFormSectionService {

  private final PadProjectInformationService padProjectInformationService;
  private final PadPipelineService padPipelineService;
  private final WorkScheduleFormValidator workScheduleFormValidator;
  private final PadCampaignWorkScheduleRepository padCampaignWorkScheduleRepository;
  private final PadCampaignWorksPipelineRepository padCampaignWorksPipelineRepository;
  private final EntityCopyingService entityCopyingService;
  private final PadPipelineReconcilerService padPipelineReconcilerService;

  @Autowired
  public CampaignWorksService(
      PadProjectInformationService padProjectInformationService,
      PadPipelineService padPipelineService,
      WorkScheduleFormValidator workScheduleFormValidator,
      PadCampaignWorkScheduleRepository padCampaignWorkScheduleRepository,
      PadCampaignWorksPipelineRepository padCampaignWorksPipelineRepository,
      EntityCopyingService entityCopyingService,
      PadPipelineReconcilerService padPipelineReconcilerService) {
    this.padProjectInformationService = padProjectInformationService;
    this.padPipelineService = padPipelineService;
    this.workScheduleFormValidator = workScheduleFormValidator;
    this.padCampaignWorkScheduleRepository = padCampaignWorkScheduleRepository;
    this.padCampaignWorksPipelineRepository = padCampaignWorksPipelineRepository;
    this.entityCopyingService = entityCopyingService;
    this.padPipelineReconcilerService = padPipelineReconcilerService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return getCampaignWorksValidationResult(detail).isSectionComplete();
  }

  public SummaryScreenValidationResult getCampaignWorksValidationResult(
      PwaApplicationDetail pwaApplicationDetail) {

    // create this once to avoid hitting the db for every form when the data will not have changed
    var campaignWorksHint = createCampaignWorksValidationHint(pwaApplicationDetail);
    var allCampaignWorkSchedules = padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail);

    var invalidWorkScheduleToErrorMap = new HashMap<String, String>();
    allCampaignWorkSchedules.forEach(workSchedule -> {
      if (getFormErrorsForCampaignWorkSchedule(workSchedule, campaignWorksHint).hasErrors()) {
        invalidWorkScheduleToErrorMap.put(
            String.valueOf(workSchedule.getId()), String.format("Work schedule %s to %s",
                DateUtils.formatDate(workSchedule.getWorkFromDate()), DateUtils.formatDate(workSchedule.getWorkToDate())));
      }
    });

    var sectionComplete = invalidWorkScheduleToErrorMap.isEmpty() &&  allApplicationPipelinesCoveredByWorkSchedules(pwaApplicationDetail);
    String sectionIncompleteError = !sectionComplete
        ? "All application pipelines must be covered by a work schedule and all work schedules must be valid" : null;

    return new SummaryScreenValidationResult(invalidWorkScheduleToErrorMap, "work-schedule", "has errors",
        sectionComplete,
        sectionIncompleteError);
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {

    var campaignWorksHint = createCampaignWorksValidationHint(pwaApplicationDetail);

    return validateForm((WorkScheduleForm) form, bindingResult, pwaApplicationDetail, campaignWorksHint);
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)
        && padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail) > 0L;
  }

  private boolean allApplicationPipelinesCoveredByWorkSchedules(PwaApplicationDetail pwaApplicationDetail) {

    var distinctSchedulePadPipeline = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(
        pwaApplicationDetail)
        .stream()
        .map(PadCampaignWorksPipeline::getPadPipeline)
        .collect(Collectors.toSet());

    var totalApplicationPipelines = padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail);

    return Long.valueOf(distinctSchedulePadPipeline.size()).equals(totalApplicationPipelines);

  }

  private Errors getFormErrorsForCampaignWorkSchedule(PadCampaignWorkSchedule padCampaignWorkSchedule,
                                                      CampaignWorkScheduleValidationHint campaignWorkScheduleValidationHint) {
    var form = new WorkScheduleForm();
    mapWorkScheduleToForm(form, padCampaignWorkSchedule);

    var validationBindingResult = validateForm(
        form,
        new BeanPropertyBindingResult(form, "form"),
        padCampaignWorkSchedule.getPwaApplicationDetail(),
        campaignWorkScheduleValidationHint
    );

    return validationBindingResult;

  }

  private CampaignWorkScheduleValidationHint createCampaignWorksValidationHint(
      PwaApplicationDetail pwaApplicationDetail) {
    var projectStartDate = padProjectInformationService.getProposedStartDate(pwaApplicationDetail)
        .map(instant -> LocalDate.ofInstant(instant, ZoneId.systemDefault()));

    var projectEndDate = padProjectInformationService.getLatestProjectCompletionDate(pwaApplicationDetail)
        .map(instant -> LocalDate.ofInstant(instant, ZoneId.systemDefault()));

    return new CampaignWorkScheduleValidationHint(
        projectStartDate.orElse(null),
        projectEndDate.orElse(null),
        pwaApplicationDetail.getPwaApplicationType());
  }

  private BindingResult validateForm(WorkScheduleForm form,
                                     BindingResult bindingResult,
                                     PwaApplicationDetail pwaApplicationDetail,
                                     CampaignWorkScheduleValidationHint campaignWorkScheduleValidationHint) {

    var validationHints = new Object[]{pwaApplicationDetail, campaignWorkScheduleValidationHint};
    ValidationUtils.invokeValidator(workScheduleFormValidator, form, bindingResult, validationHints);
    return bindingResult;

  }

  public PadCampaignWorkSchedule getWorkScheduleOrError(PwaApplicationDetail pwaApplicationDetail,
                                                        int workScheduleId) {
    return padCampaignWorkScheduleRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, workScheduleId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("work schedule id: %s not found for app_detail_id: %s", workScheduleId,
                pwaApplicationDetail.getId()))
        );
  }

  public void mapWorkScheduleToForm(WorkScheduleForm form, PadCampaignWorkSchedule padCampaignWorkSchedule) {
    var pipelineLinks = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(padCampaignWorkSchedule);
    form.setPadPipelineIds(
        pipelineLinks
            .stream()
            .map(p -> p.getPadPipeline().getId())
            .collect(Collectors.toList())
    );

    form.setWorkStart(new TwoFieldDateInput(padCampaignWorkSchedule.getWorkFromDate()));
    form.setWorkEnd(new TwoFieldDateInput(padCampaignWorkSchedule.getWorkToDate()));
  }

  public WorkScheduleView createWorkScheduleView(PadCampaignWorkSchedule padCampaignWorkSchedule) {
    var schedulePadPipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(
        padCampaignWorkSchedule)
        .stream()
        .map(PadCampaignWorksPipeline::getPadPipeline)
        .collect(Collectors.toList());

    return new WorkScheduleView(
        padCampaignWorkSchedule,
        schedulePadPipelines.stream()
            .map(padPipeline -> padPipelineService.getPipelineOverview(padPipeline))
            .collect(Collectors.toList())
    );
  }

  public List<WorkScheduleView> getWorkScheduleViews(PwaApplicationDetail pwaApplicationDetail) {

    var allScheduledPipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(
        pwaApplicationDetail);

    var allSchedules = padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail);

    Map<PadCampaignWorkSchedule, List<Integer>> scheduleToSchedulePipelineIdMap = allScheduledPipelines.stream()
        .collect(Collectors.groupingBy((PadCampaignWorksPipeline::getPadCampaignWorkSchedule),
            Collectors.mapping(e -> e.getPadPipeline().getId(), Collectors.toList())
        ));

    // need to add in any schedule with no pipeline so that removing the last pipeline from a schedule at the application level
    // will still keep showing the schedule
    allSchedules.forEach(
        padCampaignWorkSchedule -> scheduleToSchedulePipelineIdMap.putIfAbsent(padCampaignWorkSchedule, List.of()));

    Set<Integer> padPipelineIds = scheduleToSchedulePipelineIdMap.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toSet());

    var pipelineOverviewsIdMap = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail).stream()
        .filter(pipelineOverview -> padPipelineIds.contains(pipelineOverview.getPadPipelineId()))
        .collect(Collectors.toMap(PipelineOverview::getPadPipelineId, Function.identity()));


    var listOfWorkScheduleViews = new ArrayList<WorkScheduleView>();
    for (Map.Entry<PadCampaignWorkSchedule, List<Integer>> entry : scheduleToSchedulePipelineIdMap.entrySet()) {
      listOfWorkScheduleViews.add(new WorkScheduleView(
          entry.getKey(),
          entry.getValue().stream()
              .map(padPipelineId -> pipelineOverviewsIdMap.get(padPipelineId))
              .sorted(Comparator.comparing(e -> e.getPipelineName()))
              .collect(Collectors.toList())
      ));
    }

    return listOfWorkScheduleViews;
  }


  @Transactional
  public PadCampaignWorkSchedule addCampaignWorkScheduleFromForm(WorkScheduleForm form,
                                                                 PwaApplicationDetail pwaApplicationDetail) {

    var padPipelines = padPipelineService.getByIdList(pwaApplicationDetail, form.getPadPipelineIds());
    return addCampaignWorksSchedule(
        form.getWorkStart().createDateOrNull(),
        form.getWorkEnd().createDateOrNull(),
        padPipelines,
        pwaApplicationDetail
    );
  }

  @Transactional
  public void updateCampaignWorksScheduleFromForm(WorkScheduleForm form,
                                                  PadCampaignWorkSchedule padCampaignWorkSchedule) {

    var formPadPipelines = padPipelineService.getByIdList(padCampaignWorkSchedule.getPwaApplicationDetail(),
        form.getPadPipelineIds());
    var oldSchedulePipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(
        padCampaignWorkSchedule);
    padCampaignWorksPipelineRepository.deleteAll(oldSchedulePipelines);

    var updatedSchedule = setCampaignWorkScheduleValues(
        form.getWorkStart().createDateOrNull(),
        form.getWorkEnd().createDateOrNull(),
        formPadPipelines,
        padCampaignWorkSchedule);
  }

  @Transactional
  public void removeCampaignWorksSchedule(PadCampaignWorkSchedule padCampaignWorkSchedule) {

    var schedulePipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(
        padCampaignWorkSchedule);
    padCampaignWorksPipelineRepository.deleteAll(schedulePipelines);
    padCampaignWorkScheduleRepository.delete(padCampaignWorkSchedule);
  }

  @VisibleForTesting
  public void removePipelineFromAllSchedules(PadPipeline padPipeline) {
    var pipelines = padCampaignWorksPipelineRepository.findAllByPadPipeline(padPipeline);
    padCampaignWorksPipelineRepository.deleteAll(pipelines);
  }

  @Transactional
  public void removePadPipelineFromCampaignWorks(PadPipeline padPipeline) {

    var pwaApplicationDetail = padPipeline.getPwaApplicationDetail();

    this.removePipelineFromAllSchedules(padPipeline);

    var schedules = padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail);
    Map<PadCampaignWorkSchedule, List<PadCampaignWorksPipeline>> campaignMap =
        padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail)
            .stream()
            .collect(Collectors.groupingBy(PadCampaignWorksPipeline::getPadCampaignWorkSchedule));

    var schedulesToRemove = CleanupUtils.getUnlinkedKeys(schedules, campaignMap,
        (schedule1, schedule2) -> schedule1.getId().equals(schedule2.getId()));

    if (!schedulesToRemove.isEmpty()) {
      padCampaignWorkScheduleRepository.deleteAll(schedulesToRemove);
    }
  }

  private PadCampaignWorkSchedule setCampaignWorkScheduleValues(LocalDate workStart, LocalDate workEnd,
                                                                List<PadPipeline> associatedPipelines,
                                                                PadCampaignWorkSchedule schedule) {
    schedule.setWorkFromDate(workStart);
    schedule.setWorkToDate(workEnd);
    schedule = padCampaignWorkScheduleRepository.save(schedule);
    List<PadCampaignWorksPipeline> workSchedulePipelines = new ArrayList<>();

    for (PadPipeline padPipeline : associatedPipelines) {
      workSchedulePipelines.add(createCampaignWorksPipeline(schedule, padPipeline));
    }

    padCampaignWorksPipelineRepository.saveAll(workSchedulePipelines);
    return schedule;

  }

  private PadCampaignWorkSchedule addCampaignWorksSchedule(LocalDate workStart, LocalDate workEnd,
                                                           List<PadPipeline> associatedPipelines,
                                                           PwaApplicationDetail pwaApplicationDetail) {
    var schedule = new PadCampaignWorkSchedule();
    schedule.setPwaApplicationDetail(pwaApplicationDetail);
    return setCampaignWorkScheduleValues(workStart, workEnd, associatedPipelines, schedule);

  }

  private PadCampaignWorksPipeline createCampaignWorksPipeline(PadCampaignWorkSchedule padCampaignWorkSchedule,
                                                               PadPipeline padPipeline) {
    var padCampaignWorksPipeline = new PadCampaignWorksPipeline();
    padCampaignWorksPipeline.setPadCampaignWorkSchedule(padCampaignWorkSchedule);
    padCampaignWorksPipeline.setPadPipeline(padPipeline);
    return padCampaignWorksPipeline;

  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    // 1. duplicate work schedules
    var copiedPadCampaignWorkScheduleEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padCampaignWorkScheduleRepository.findByPwaApplicationDetail(fromDetail),
        toDetail,
        PadCampaignWorkSchedule.class
    );

    // 2. duplicate schedule pipelines
    var copiedWorkSchedulePipelineEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(fromDetail),
        copiedPadCampaignWorkScheduleEntityIds,
        PadCampaignWorksPipeline.class
    );

    // 3. re-point duplicated schedule pipelines at "toDetail" padPipelines
    var reconciledPadPipelines = padPipelineReconcilerService.reconcileApplicationDetailPadPipelines(
        fromDetail,
        toDetail);

    var duplicatedWorkSchedulePipelines = padCampaignWorksPipelineRepository
        .findAllByPadCampaignWorkSchedule_pwaApplicationDetail(toDetail);

    duplicatedWorkSchedulePipelines.forEach(padCampaignWorksPipeline -> {
      padCampaignWorksPipeline.setPadPipeline(
          reconciledPadPipelines.findByPipelineIdOrError(
              padCampaignWorksPipeline.getPadPipeline().getPipelineId()
          ).getReconciledPadPipeline()
      );
    });

    padCampaignWorksPipelineRepository.saveAll(duplicatedWorkSchedulePipelines);
  }

}
