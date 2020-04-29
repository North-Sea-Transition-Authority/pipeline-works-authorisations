package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDetailServiceTest {

  @Mock
  private PwaApplicationDetailRepository applicationDetailRepository;

  private PwaApplicationDetailService pwaApplicationDetailService;
  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount webUserAccount;
  private AuthenticatedUserAccount user;
  private Clock clock = Clock.systemUTC();

  @Before
  public void setUp() {
    pwaApplicationDetailService = new PwaApplicationDetailService(applicationDetailRepository, clock);
    pwaApplicationDetail = new PwaApplicationDetail();
    webUserAccount = new WebUserAccount();
    user = new AuthenticatedUserAccount(webUserAccount, List.of());

    when(applicationDetailRepository.findByPwaApplicationIdAndStatusAndTipFlagIsTrue(1, PwaApplicationStatus.DRAFT))
        .thenReturn(Optional.of(pwaApplicationDetail));
  }

  @Test
  public void withDraftTipDetail() {
    AtomicBoolean functionApplied = new AtomicBoolean(false);
    pwaApplicationDetailService.withDraftTipDetail(1, user, detail -> {
      assertThat(detail).isEqualTo(pwaApplicationDetail);
      functionApplied.set(true);
      return null;
    });
    assertThat(functionApplied.get()).isEqualTo(true);
  }

  @Test
  public void getTipDetailWithStatus() {
    var detail = pwaApplicationDetailService.getTipDetailWithStatus(1, PwaApplicationStatus.DRAFT);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void setLinkedToFields() {

    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, true);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToField()).isTrue();
  }
}