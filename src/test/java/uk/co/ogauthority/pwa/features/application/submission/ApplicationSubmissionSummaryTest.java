package uk.co.ogauthority.pwa.features.application.submission;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionSummaryTest {

  private static final String SUBMITTED_BY = "A NAME";
  private ApplicationSubmissionSummary applicationSubmissionSummary;

  private PwaApplication pwaApplication;
  private LocalDateTime localDateTime = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0, 59);

  @BeforeEach
  void setup() {
    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL)
        .getPwaApplication();

  }


  @Test
  void applicationSubmissionSummary_propertiesSetAsExpected() {

    applicationSubmissionSummary = new ApplicationSubmissionSummary(
        pwaApplication,
        true,
        localDateTime,
        SUBMITTED_BY
    );

    assertThat(applicationSubmissionSummary.getApplicationReference()).isEqualTo(pwaApplication.getAppReference());
    assertThat(applicationSubmissionSummary.getIsFirstVersion()).isEqualTo(true);
    assertThat(applicationSubmissionSummary.getSubmissionDateTime()).isEqualTo(localDateTime);
    assertThat(applicationSubmissionSummary.getSubmittedBy()).isEqualTo(SUBMITTED_BY);
  }

  @Test
  void getFormattedSubmissionTime_formatIsExpected() {

    applicationSubmissionSummary = new ApplicationSubmissionSummary(
        pwaApplication,
        true,
        localDateTime,
        SUBMITTED_BY
    );

    assertThat(applicationSubmissionSummary.getFormattedSubmissionTime()).isEqualTo("01/01/2020 12:00:59");

  }
}