package uk.co.ogauthority.pwa.model.view.appprocessing.options;


import java.time.LocalDate;
import java.time.ZoneId;

public class OptionsApprovalDeadlineViewTestUtil {

  private OptionsApprovalDeadlineViewTestUtil() {
    // no instantiation
  }

  public static OptionsApprovalDeadlineView createWithDeadline(LocalDate date) {
    return new OptionsApprovalDeadlineView(
        null,
        null,
        null,
        null,
        date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        null
    );
  }


}