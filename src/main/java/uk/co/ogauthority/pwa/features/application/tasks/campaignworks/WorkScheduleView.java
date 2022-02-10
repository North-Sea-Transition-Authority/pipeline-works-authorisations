package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;

public class WorkScheduleView {
  private static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

  private final Integer padCampaignWorkScheduleId;
  private final LocalDate workStartDate;
  private final LocalDate workEndDate;

  private final String formattedWorkStartDate;
  private final String formattedWorkEndDate;

  private final List<CampaignWorkSchedulePipelineView> schedulePipelines;

  private WorkScheduleView(Integer padCampaignWorkScheduleId,
                           LocalDate workStartDate,
                           LocalDate workEndDate,
                           List<CampaignWorkSchedulePipelineView> linkedPipelines) {
    this.padCampaignWorkScheduleId = padCampaignWorkScheduleId;
    this.workStartDate = workStartDate;
    this.workEndDate = workEndDate;
    this.formattedWorkStartDate = workStartDate.format(DATETIME_FORMATTER);
    this.formattedWorkEndDate = workEndDate.format(DATETIME_FORMATTER);
    this.schedulePipelines = linkedPipelines;
  }

  public WorkScheduleView(PadCampaignWorkSchedule padCampaignWorkSchedule,
                          List<PipelineOverview> linkedPipelineOverviews) {
    this(padCampaignWorkSchedule.getId(),
        padCampaignWorkSchedule.getWorkFromDate(),
        padCampaignWorkSchedule.getWorkToDate(),
        linkedPipelineOverviews.stream()
            .map(CampaignWorkSchedulePipelineView::fromPipelineOverview)
            .collect(Collectors.toList()));
  }

  public LocalDate getWorkStartDate() {
    return workStartDate;
  }

  public LocalDate getWorkEndDate() {
    return workEndDate;
  }

  public String getFormattedWorkStartDate() {
    return formattedWorkStartDate;
  }

  public String getFormattedWorkEndDate() {
    return formattedWorkEndDate;
  }

  public Integer getPadCampaignWorkScheduleId() {
    return padCampaignWorkScheduleId;
  }

  public List<CampaignWorkSchedulePipelineView> getSchedulePipelines() {
    return schedulePipelines;
  }
}
