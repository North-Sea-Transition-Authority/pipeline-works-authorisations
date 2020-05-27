package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.lang.Nullable;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrAfterDateHint;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.OnOrBeforeDateHint;

/* contain earliest and latest work dates as determined by application type. Could be inlined into validator */
public final class CampaignWorkScheduleValidationHint {

  public static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
  private static final long OPTIONS_VARIATION_MAX_MONTH = 6L;
  private static final long DEFAULT_MAX_MONTH = 12L;
  private static final String PROJECT_INFO_PROP_START_DATE_LABEL = ApplicationTask.PROJECT_INFORMATION.getDisplayName() +
      " proposed start date";

  private final LocalDate earliestDate;
  private final OnOrAfterDateHint earliestWorkStartDateHint;
  private final OnOrBeforeDateHint latestWorkEndDateHint;

  public CampaignWorkScheduleValidationHint(@Nullable LocalDate projectInfoProposedStartDate, PwaApplicationType pwaApplicationType) {
    this.earliestDate = projectInfoProposedStartDate != null ? projectInfoProposedStartDate : LocalDate.now();
    var formattedEarliestDate = "(" + this.earliestDate.format(DATETIME_FORMATTER) + ")";

    var earliestDateLabel = projectInfoProposedStartDate != null
        ? PROJECT_INFO_PROP_START_DATE_LABEL + " " + formattedEarliestDate : "today's date";

    var plusMonths = pwaApplicationType.equals(PwaApplicationType.OPTIONS_VARIATION)
        ? OPTIONS_VARIATION_MAX_MONTH : DEFAULT_MAX_MONTH;

    var beforeDate = this.earliestDate.plusMonths(plusMonths);
    var beforeDateFormatted = beforeDate.format(DATETIME_FORMATTER);

    this.latestWorkEndDateHint = new OnOrBeforeDateHint(beforeDate, beforeDateFormatted);
    this.earliestWorkStartDateHint = new OnOrAfterDateHint(this.earliestDate, earliestDateLabel);

  }

  public LocalDate getEarliestDate() {
    return earliestDate;
  }

  public OnOrAfterDateHint getEarliestWorkStartDateHint() {
    return earliestWorkStartDateHint;
  }

  public OnOrBeforeDateHint getLatestWorkEndDateHint() {
    return latestWorkEndDateHint;
  }
}
