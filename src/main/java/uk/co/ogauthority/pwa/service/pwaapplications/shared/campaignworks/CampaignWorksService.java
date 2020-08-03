package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.CampaignWorkScheduleValidationHint;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleFormValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorkScheduleRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorksPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.CleanupUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@Service
public class CampaignWorksService implements ApplicationFormSectionService {

  private final PadProjectInformationService padProjectInformationService;
  private final PadPipelineService padPipelineService;
  private final WorkScheduleFormValidator workScheduleFormValidator;
  private final PadCampaignWorkScheduleRepository padCampaignWorkScheduleRepository;
  private final PadCampaignWorksPipelineRepository padCampaignWorksPipelineRepository;

  @Autowired
  public CampaignWorksService(
      PadProjectInformationService padProjectInformationService,
      PadPipelineService padPipelineService,
      WorkScheduleFormValidator workScheduleFormValidator,
      PadCampaignWorkScheduleRepository padCampaignWorkScheduleRepository,
      PadCampaignWorksPipelineRepository padCampaignWorksPipelineRepository) {
    this.padProjectInformationService = padProjectInformationService;
    this.padPipelineService = padPipelineService;
    this.workScheduleFormValidator = workScheduleFormValidator;
    this.padCampaignWorkScheduleRepository = padCampaignWorkScheduleRepository;
    this.padCampaignWorksPipelineRepository = padCampaignWorksPipelineRepository;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return getCampaignWorksValidationResult(detail).isComplete();
  }

  public CampaignWorksSummaryValidationResult getCampaignWorksValidationResult(
      PwaApplicationDetail pwaApplicationDetail) {

    // create this once to avoid hitting the db for every form when the data will not have changed
    var campaignWorksHint = createCampaignWorksValidationHint(pwaApplicationDetail);
    var allCampaignWorkSchedules = padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail);

    return new CampaignWorksSummaryValidationResult(pwaApplicationDetail,
        allCampaignWorkSchedules,
        padCampaignWorkSchedule -> getFormErrorsForCampaignWorkSchedule(padCampaignWorkSchedule, campaignWorksHint),
        this::allApplicationPipelinesCoveredByWorkSchedules
    );
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

    return new CampaignWorkScheduleValidationHint(
        projectStartDate.orElse(null),
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
        schedulePadPipelines
    );
  }

  public List<WorkScheduleView> getWorkScheduleViews(PwaApplicationDetail pwaApplicationDetail) {

    var allScheduledPipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(
        pwaApplicationDetail);

    var allSchedules = padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail);

    Map<PadCampaignWorkSchedule, List<PadPipeline>> scheduleToSchedulePipelineMap = allScheduledPipelines.stream()
        .collect(Collectors.groupingBy((PadCampaignWorksPipeline::getPadCampaignWorkSchedule),
            Collectors.mapping(PadCampaignWorksPipeline::getPadPipeline, Collectors.toList())
        ));

    // need to add in any schedule with no pipeline so that removing the last pipeline from a schedule at the application level
    // will still keep showing the schedule
    allSchedules.forEach(
        padCampaignWorkSchedule -> scheduleToSchedulePipelineMap.putIfAbsent(padCampaignWorkSchedule, List.of()));

    var listOfWorkScheduleViews = new ArrayList<WorkScheduleView>();
    for (Map.Entry<PadCampaignWorkSchedule, List<PadPipeline>> entry : scheduleToSchedulePipelineMap.entrySet()) {
      listOfWorkScheduleViews.add(new WorkScheduleView(
          entry.getKey(),
          entry.getValue()
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

  @Transactional
  public void removePipelineFromAllSchedules(PwaApplicationDetail pwaApplicationDetail, PadPipeline padPipeline) {
    var pipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_PwaApplicationDetailAndAndPadPipeline(
        pwaApplicationDetail, padPipeline);
    padCampaignWorksPipelineRepository.deleteAll(pipelines);
  }

  public void cleanupUnlinkedSchedules(PwaApplicationDetail pwaApplicationDetail) {

    var schedules = padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail);

    Map<PadCampaignWorkSchedule, List<PadCampaignWorksPipeline>> campaignMap =
        padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail)
            .stream()
            .collect(Collectors.groupingBy(PadCampaignWorksPipeline::getPadCampaignWorkSchedule));

    var schedulesToRemove = CleanupUtils.getUnlinkedKeys(schedules, campaignMap,
        (key, value) -> key.getId().equals(value.getId()));

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

}
