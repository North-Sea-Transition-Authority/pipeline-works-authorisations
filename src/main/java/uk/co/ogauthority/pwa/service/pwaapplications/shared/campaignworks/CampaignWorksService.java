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
import org.springframework.validation.BindingResult;
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
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorkScheduleRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorksPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
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
    // TODO PWA-372 do validation on all work schedules
    // then we need to check at least one schedule and that every schedule has at least one pad pipeline AND valid schedule dates
    return false;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    var projectStartDate = padProjectInformationService.getProposedStartDate(pwaApplicationDetail)
        .map(instant -> LocalDate.ofInstant(instant, ZoneId.systemDefault()));

    var campaignWorksHint = new CampaignWorkScheduleValidationHint(
        projectStartDate.orElse(null),
        pwaApplicationDetail.getPwaApplicationType());
    return validateForm((WorkScheduleForm) form, bindingResult, pwaApplicationDetail, campaignWorksHint);
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
            String.format("work schedule id: %s not found for app_detail_id: %s", pwaApplicationDetail.getId(), workScheduleId))
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

  public List<WorkScheduleView> getWorkScheduleViews(PwaApplicationDetail pwaApplicationDetail) {

    // is there a nicer way? potentially putting a link down to linked pipelines on the schedule entity would have made this much simpler.
    // 1. get all pipelines for work schedules on the application
    // 2. create a lookup from padPipelineId to pipelineOverview view
    // 3. Organise the list of all pipelines on a schedule into a map so we can go from a single schedule to all its pipelines
    // 4. for each schedule, create a schedule summary using the pipelineOverview lookup

    var allScheduledPipelines = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(
        pwaApplicationDetail);

    var padPipelineIds = allScheduledPipelines.stream()
        .map(PadCampaignWorksPipeline::getPadPipeline)
        .map(PadPipeline::getId)
        .collect(Collectors.toSet());

    var pipelineOverviewsMappedByPadPipelineId = padPipelineService.getPipelineOverviews(pwaApplicationDetail)
        .stream()
        .filter(po -> padPipelineIds.contains(po.getPadPipelineId()))
        .collect(Collectors.toMap(PipelineOverview::getPadPipelineId, po -> po));

    Map<PadCampaignWorkSchedule, List<PadPipeline>> scheduleToSchedulePipelineMap = allScheduledPipelines.stream()
        .collect(Collectors.groupingBy((PadCampaignWorksPipeline::getPadCampaignWorkSchedule),
            Collectors.mapping(PadCampaignWorksPipeline::getPadPipeline, Collectors.toList())
        ));

    var listOfWorkScheduleViews = new ArrayList<WorkScheduleView>();
    for (Map.Entry<PadCampaignWorkSchedule, List<PadPipeline>> entry : scheduleToSchedulePipelineMap.entrySet()) {
      var schedulePipelineOverviews = entry.getValue().stream()
          .map(padPipeline -> pipelineOverviewsMappedByPadPipelineId.get(padPipeline.getId()))
          .collect(Collectors.toList());
      listOfWorkScheduleViews.add(new WorkScheduleView(
          entry.getKey().getId(),
          entry.getKey().getWorkFromDate(),
          entry.getKey().getWorkToDate(),
          schedulePipelineOverviews
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

    var formPadPipelines = padPipelineService.getByIdList(padCampaignWorkSchedule.getPwaApplicationDetail(), form.getPadPipelineIds());
    var oldSchedulePipeline = padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(padCampaignWorkSchedule);
    padCampaignWorksPipelineRepository.deleteAll(oldSchedulePipeline);

    var updatedSchedule = setCampaignWorkScheduleValues(
        form.getWorkStart().createDateOrNull(),
        form.getWorkEnd().createDateOrNull(),
        formPadPipelines,
        padCampaignWorkSchedule);

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
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)
        && padPipelineService.totalPipelineContainedInApplication(pwaApplicationDetail) > 0L;
  }

}
