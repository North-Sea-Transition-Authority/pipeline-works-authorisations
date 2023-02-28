package uk.co.ogauthority.pwa.features.application.tasks.campaignworks;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.lang.Nullable;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.MaxCompletionPeriod;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.BeforeDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;

/* contain earliest and latest work dates as determined by application type. Could be inlined into validator */
public final class CampaignWorkScheduleValidationHint {

  public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
  private static final String PROJECT_INFO_PROP_START_DATE_LABEL = ApplicationTask.PROJECT_INFORMATION.getDisplayName() +
      " proposed start of works date";

  private final LocalDate earliestDate;
  private final OnOrAfterDateHint earliestWorkStartDateHint;
  private final BeforeDateHint latestWorkEndDateHint;

  public CampaignWorkScheduleValidationHint(@Nullable LocalDate projectInfoProposedStartDate,
                                            @Nullable LocalDate projectInfoProposedEndDate,
                                            PwaApplicationType pwaApplicationType) {
    this.earliestDate = projectInfoProposedStartDate != null ? projectInfoProposedStartDate : LocalDate.now();
    var formattedEarliestDate = "(" + this.earliestDate.format(DATETIME_FORMATTER) + ")";

    var earliestDateLabel = projectInfoProposedStartDate != null
        ? PROJECT_INFO_PROP_START_DATE_LABEL + " " + formattedEarliestDate : "today's date";

    var plusMonths = MaxCompletionPeriod.valueOf(pwaApplicationType.name()).getMaxMonthsCompletion();

    var beforeDate = projectInfoProposedEndDate != null
        ? projectInfoProposedEndDate : this.earliestDate.plusMonths(plusMonths);

    var beforeDateFormatted = beforeDate.format(DATETIME_FORMATTER);

    this.latestWorkEndDateHint = new BeforeDateHint(beforeDate, beforeDateFormatted);
    this.earliestWorkStartDateHint = new OnOrAfterDateHint(this.earliestDate, earliestDateLabel);

  }

  public LocalDate getEarliestDate() {
    return earliestDate;
  }

  public OnOrAfterDateHint getEarliestWorkStartDateHint() {
    return earliestWorkStartDateHint;
  }

  public BeforeDateHint getLatestWorkEndDateHint() {
    return latestWorkEndDateHint;
  }
}
