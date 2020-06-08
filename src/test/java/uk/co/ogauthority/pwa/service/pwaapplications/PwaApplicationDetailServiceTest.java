package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDetailServiceTest {

  @Mock
  private PwaApplicationDetailRepository applicationDetailRepository;

  @Mock
  private PadFastTrackService fastTrackService;

  private PwaApplicationDetailService pwaApplicationDetailService;
  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount webUserAccount;
  private AuthenticatedUserAccount user;

  private Clock clock;

  @Before
  public void setUp() {
    pwaApplicationDetail = new PwaApplicationDetail();
    webUserAccount = new WebUserAccount();
    user = new AuthenticatedUserAccount(webUserAccount, List.of());

    var fixedInstant = LocalDate
        .of(2020, 2, 6)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant();

    clock = Clock.fixed(fixedInstant, ZoneId.systemDefault());

    when(applicationDetailRepository.findByPwaApplicationIdAndStatusAndTipFlagIsTrue(1, PwaApplicationStatus.DRAFT))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(applicationDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    pwaApplicationDetailService = new PwaApplicationDetailService(applicationDetailRepository, clock, fastTrackService);
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
  public void setLinkedToFields_isLinked() {

    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, true);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToField()).isTrue();
    assertNull(detail.getNotLinkedDescription());
  }

  @Test
  public void setLinkedToFields_notLinked() {
    pwaApplicationDetail.setNotLinkedDescription("test description");
    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, false);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToField()).isFalse();
    assertEquals("test description", detail.getNotLinkedDescription());
  }

  @Test
  public void createFirstDetail_attributesSetAsExpected(){

    var master = new PwaApplication();
    var detail = pwaApplicationDetailService.createFirstDetail(master, user);
    assertThat(detail.getPwaApplication()).isEqualTo(master);
    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.DRAFT);
    assertThat(detail.isTipFlag()).isTrue();
    assertThat(detail.getVersionNo()).isEqualTo(1);
    assertThat(detail.getCreatedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(detail.getSubmittedByWuaId()).isNull();
    assertThat(detail.getSubmittedTimestamp()).isNull();
    assertThat(detail.getStatusLastModifiedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());

  }

  @Test
  public void updateStatus_statusModifiedDataSet(){
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var alternativeWua = new WebUserAccount(1000);
    var updatedDetail = pwaApplicationDetailService.updateStatus(
        detail,
        PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW,
        alternativeWua
    );

    assertThat(updatedDetail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());
    assertThat(updatedDetail.getStatusLastModifiedByWuaId()).isEqualTo(alternativeWua.getWuaId());
    assertThat(updatedDetail.getStatus()).isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

  }

  @Test
  public void setSubmitted_allStatusColumnsSetAsExpected(){

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var alternativeWua = new WebUserAccount(1000);

    when(fastTrackService.isFastTrackRequired(detail)).thenReturn(true);

    var submittedDetail = pwaApplicationDetailService.setSubmitted(
        detail,
        alternativeWua
    );

    assertThat(submittedDetail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());
    assertThat(submittedDetail.getStatusLastModifiedByWuaId()).isEqualTo(alternativeWua.getWuaId());
    assertThat(submittedDetail.getStatus()).isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    assertThat(submittedDetail.getSubmittedTimestamp()).isEqualTo(clock.instant());
    assertThat(submittedDetail.getSubmittedByWuaId()).isEqualTo(alternativeWua.getWuaId());

    assertThat(submittedDetail.getSubmittedAsFastTrackFlag()).isTrue();

  }

  @Test
  public void setInitialReviewApproved() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    pwaApplicationDetailService.setInitialReviewApproved(detail, user);

    verify(applicationDetailRepository, times(2)).save(detail);

    assertThat(detail.getInitialReviewApprovedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getInitialReviewApprovedTimestamp()).isEqualTo(clock.instant());
    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

  }

}