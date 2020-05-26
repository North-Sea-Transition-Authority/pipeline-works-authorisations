package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

public class WorkScheduleView {
  private static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

  private final Integer padCampaignWorkScheduleId;
  private final LocalDate workStartDate;
  private final LocalDate workEndDate;

  private final String formattedWorkStartDate;
  private final String formattedWorkEndDate;

  private final List<PipelineOverview> schedulePipelines;

  public WorkScheduleView(Integer padCampaignWorkScheduleId,
                          LocalDate workStartDate,
                          LocalDate workEndDate,
                          List<PipelineOverview> linkedPipelines) {
    this.padCampaignWorkScheduleId = padCampaignWorkScheduleId;
    this.workStartDate = workStartDate;
    this.workEndDate = workEndDate;
    this.formattedWorkStartDate = workStartDate.format(DATETIME_FORMATTER);
    this.formattedWorkEndDate = workEndDate.format(DATETIME_FORMATTER);
    ;
    this.schedulePipelines = linkedPipelines;
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

  public List<PipelineOverview> getSchedulePipelines() {
    return schedulePipelines;
  }
}
