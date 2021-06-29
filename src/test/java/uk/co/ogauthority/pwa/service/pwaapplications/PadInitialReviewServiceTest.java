package uk.co.ogauthority.pwa.service.pwaapplications;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PadInitialReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PadInitialReviewRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadInitialReviewServiceTest {

  @Mock
  private PadInitialReviewRepository padInitialReviewRepository;

  @Captor
  private ArgumentCaptor<PadInitialReview> padInitialReviewArgumentCaptor;

  private PadInitialReviewService padInitialReviewService;

  private static final int WUA_ID_1 = 1;
  private static final PersonId WUA_1_PERSON_ID = new PersonId(10);
  private static final Instant YESTERDAY = Instant.now();

  private Clock clock;
  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplication pwaApplication;
  private AuthenticatedUserAccount user;
  private Person wua1Person = new Person(WUA_1_PERSON_ID.asInt(), "Industry", "Person", "industry@pwa.co.uk", null);


  @Before
  public void setUp() {

    clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(WUA_ID_1, wua1Person), List.of());

    padInitialReviewService = new PadInitialReviewService(padInitialReviewRepository, clock);
  }

  @Test
  public void addApprovedInitialReview_verifyRepoInteraction() {

    padInitialReviewService.addApprovedInitialReview(pwaApplicationDetail, user);
    verify(padInitialReviewRepository).save(
        new PadInitialReview(pwaApplicationDetail, user.getWuaId(), clock.instant()));
  }

  @Test
  public void revokeLatestInitialReview_latestReviewSetAsRevoked() {

    var initialReview = new PadInitialReview(pwaApplicationDetail, user.getWuaId(), clock.instant());

    when(padInitialReviewRepository.findFirstByPwaApplicationDetailOrderByInitialReviewApprovedTimestampDesc(
        pwaApplicationDetail)).thenReturn(Optional.of(initialReview));

    padInitialReviewService.revokeLatestInitialReview(pwaApplicationDetail, user);

    var revokedInitialReview = new PadInitialReview(pwaApplicationDetail, user.getWuaId(), clock.instant());
    revokedInitialReview.setApprovalRevokedTimestamp(clock.instant());
    revokedInitialReview.setApprovalRevokedByWuaId(user.getWuaId());

    verify(padInitialReviewRepository).save(padInitialReviewArgumentCaptor.capture());
    var actualPadInitialReview = padInitialReviewArgumentCaptor.getValue();
    assertThat(actualPadInitialReview.getApprovalRevokedByWuaId()).isEqualTo(revokedInitialReview.getApprovalRevokedByWuaId());
    assertThat(actualPadInitialReview.getApprovalRevokedTimestamp()).isEqualTo(revokedInitialReview.getApprovalRevokedTimestamp());
  }

  @Test
  public void isInitialReviewComplete_latestInitialReviewIsRevokedOrDoesNotExist_notComplete() {

    when(padInitialReviewRepository.findByPwaApplicationDetail_pwaApplicationAndApprovalRevokedTimestampIsNull(pwaApplication)).thenReturn(List.of());
    assertThat(padInitialReviewService.isInitialReviewComplete(pwaApplication)).isFalse();
  }

  @Test
  public void isInitialReviewComplete_latestInitialReviewExistsAndIsNotRevoked_complete() {

    when(padInitialReviewRepository.findByPwaApplicationDetail_pwaApplicationAndApprovalRevokedTimestampIsNull(pwaApplication)).thenReturn(List.of(new PadInitialReview()));
    assertThat(padInitialReviewService.isInitialReviewComplete(pwaApplication)).isTrue();
  }


}
