package uk.co.ogauthority.pwa.service.pwaapplications.generic.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.users.UserAccountService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSummaryFactoryTest {

  private static final String NAME = "A NAME";
  private static final int SUBMITTER_WUA_ID = 10;

  @Mock
  private UserAccountService userAccountService;

  private ApplicationSummaryFactory applicationSummaryFactory;

  @Mock
  private WebUserAccount webUserAccount;

  private PwaApplicationDetail detail;

  private LocalDateTime submitDateTime;
  private Instant submitInstant;

  @Before
  public void setup() {
    applicationSummaryFactory = new ApplicationSummaryFactory(userAccountService);
    when(webUserAccount.getFullName()).thenReturn(NAME);
    when(userAccountService.getWebUserAccount(anyInt())).thenReturn(webUserAccount);

    submitDateTime = LocalDateTime.of(2020, Month.JANUARY, 1, 11, 59, 0);
    submitInstant = submitDateTime.toInstant(ZoneOffset.UTC);
    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setSubmittedTimestamp(submitInstant);
    detail.setSubmittedByWuaId(SUBMITTER_WUA_ID);


  }


  @Test
  public void createSubmissionSummary_verifyServiceInteractions_() {
    var summary = applicationSummaryFactory.createSubmissionSummary(detail);
    verify(userAccountService, times(1)).getWebUserAccount(SUBMITTER_WUA_ID);

  }

  @Test
  public void createSubmissionSummary_summaryCreatedAsExpected() {
    var summary = applicationSummaryFactory.createSubmissionSummary(detail);
    assertThat(summary.getApplicationReference()).isEqualTo(detail.getPwaApplicationRef());
    assertThat(summary.getIsFirstVersion()).isEqualTo(true);
    assertThat(summary.getSubmissionDateTime()).isEqualTo(submitDateTime);
    assertThat(summary.getSubmittedBy()).isEqualTo(NAME);

  }
}