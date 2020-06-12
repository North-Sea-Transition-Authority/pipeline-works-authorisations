package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;

/**
 * Class which summarises validity of the Campaign works section of a PWA Application.
 */
public class CampaignWorksSummaryValidationResult {
  private final Set<Integer> invalidWorkScheduleIdSet;
  private final boolean everyApplicationPipelineScheduled;

  private final boolean isComplete;

  public CampaignWorksSummaryValidationResult(PwaApplicationDetail pwaApplicationDetail,
                                              List<PadCampaignWorkSchedule> padCampaignWorkScheduleList,
                                              Function<PadCampaignWorkSchedule, Errors> scheduleIsValid,
                                              Function<PwaApplicationDetail, Boolean> allApplicationPipelinesScheduledCheck
  ) {
    this.invalidWorkScheduleIdSet = new HashSet<>();
    padCampaignWorkScheduleList.forEach(
        padCampaignWorkSchedule -> processWorkScheduleForValidity(padCampaignWorkSchedule, scheduleIsValid));

    this.everyApplicationPipelineScheduled = allApplicationPipelinesScheduledCheck.apply(pwaApplicationDetail);

    this.isComplete = this.invalidWorkScheduleIdSet.isEmpty() && this.everyApplicationPipelineScheduled;

  }

  private void processWorkScheduleForValidity(PadCampaignWorkSchedule padCampaignWorkSchedule,
                                              Function<PadCampaignWorkSchedule, Errors> scheduleIsValid
  ) {
    if (scheduleIsValid.apply(padCampaignWorkSchedule).hasErrors()) {
      invalidWorkScheduleIdSet.add(padCampaignWorkSchedule.getId());
    }

  }

  public String getCompleteSectionErrorMessage() {
    if (this.isComplete) {
      return null;
    }

    if (this.invalidWorkScheduleIdSet.isEmpty() && !this.everyApplicationPipelineScheduled) {
      return "Every application pipeline must be covered by a work schedule";
    } else if (!this.invalidWorkScheduleIdSet.isEmpty() && this.everyApplicationPipelineScheduled) {
      return "There must be no work schedules with errors";
    } else {
      return "All application pipelines must be covered by a work schedule and all work schedules must be valid";

    }

  }

  public boolean isWorkScheduleInvalid(int padCampaignWorkScheduleId) {
    return this.invalidWorkScheduleIdSet.contains(padCampaignWorkScheduleId);
  }

  public boolean isComplete() {
    return isComplete;
  }

}
