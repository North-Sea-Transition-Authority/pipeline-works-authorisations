package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.time.Instant;
import java.util.List;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning.AppProcessingTaskWarningTestUtil;

public class PreSendForApprovalChecksViewTestUtil {

  private PreSendForApprovalChecksViewTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static PreSendForApprovalChecksView createNoFailedChecksView() {
    return new PreSendForApprovalChecksView(List.of(), List.of(), AppProcessingTaskWarningTestUtil.createWithNoWarning());
  }

  public static PreSendForApprovalChecksView createFailedChecksView() {
    var failCheck = FailedSendForApprovalCheckTestUtil.create(SendConsentForApprovalRequirement.DOCUMENT_HAS_CLAUSES);
    return new PreSendForApprovalChecksView(List.of(failCheck), List.of(), AppProcessingTaskWarningTestUtil.createWithNoWarning());
  }

  public static PreSendForApprovalChecksView createParallelConsentsChecksView() {
    var failCheck = FailedSendForApprovalCheckTestUtil.create(SendConsentForApprovalRequirement.DOCUMENT_HAS_CLAUSES);
    return new PreSendForApprovalChecksView(
        List.of(),
        List.of(
            new ParallelConsentView(
                1,
                "CONSENT/REF",
                1,
                PwaApplicationType.INITIAL,
                "APP/REF",
                Instant.now(),
                "FormattedDate"
            )
        ),
        AppProcessingTaskWarningTestUtil.createWithNoWarning());
  }

}