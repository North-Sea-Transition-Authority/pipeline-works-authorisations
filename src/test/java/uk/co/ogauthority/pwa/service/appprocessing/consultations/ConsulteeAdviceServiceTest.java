package uk.co.ogauthority.pwa.service.appprocessing.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.consultations.ConsulteeAdviceService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsulteeAdviceServiceTest {

  @Mock
  private ConsultationResponseService consultationResponseService;

  @Mock
  private ConsultationViewService consultationViewService;

  private ConsulteeAdviceService consulteeAdviceService;

  private PwaApplicationDetail detail;
  private WebUserAccount user;

  private ConsulteeGroupDetail consulteeGroupDetail;

  @Before
  public void setUp() {

    consulteeAdviceService = new ConsulteeAdviceService(consultationResponseService, consultationViewService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    user = new WebUserAccount(1);
    consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "ab");

  }

  @Test
  public void canShowInTaskList_noConsulteeAdvicePermission_hidden() {

    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(), false);
    var appInvolvement = new ApplicationInvolvementDto(detail.getPwaApplication(), Set.of(), consultationInvolvement, false,
        false);
    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE), null, appInvolvement);

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_consulteeAdvicePermission_shown() {

    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(new ConsultationRequest()), false);
    var appInvolvement = new ApplicationInvolvementDto(detail.getPwaApplication(), Set.of(), consultationInvolvement, false,
        false);
    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CONSULTEE_ADVICE), null, appInvolvement);

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_oga_hidden() {

    var appInvolvement = PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication());

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA), null, appInvolvement);

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_industry_hidden() {

    var appInvolvement = PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication());

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, appInvolvement);

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_default() {


    var context = new PwaAppProcessingContext(detail, user, Set.of(), null, null);
    var taskListEntry = consulteeAdviceService.getTaskListEntry(PwaAppProcessingTask.CONSULTEE_ADVICE, context);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTEE_ADVICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTEE_ADVICE.getRoute(context));
    assertThat(taskListEntry.getTaskTag()).isNull();
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CONSULTEE_ADVICE.getDisplayOrder());

  }

  @Test
  public void getConsulteeAdviceView_noActiveRequest() {

    var historicalRequest = new ConsultationRequest();
    historicalRequest.setId(1);
    historicalRequest.setStatus(ConsultationRequestStatus.RESPONDED);

    var historicalResponse = new ConsultationResponse();
    historicalResponse.setConsultationRequest(historicalRequest);

    when(consultationResponseService.getResponsesByConsultationRequests(any())).thenReturn(List.of(historicalResponse));

    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(historicalRequest), false);
    var appInvolvement = new ApplicationInvolvementDto(detail.getPwaApplication(), Set.of(), consultationInvolvement, false,
        false);
    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CONSULTEE_ADVICE), null, appInvolvement);

    var requestView = new ConsultationRequestView(
        null,
        consulteeGroupDetail.getName(),
        Instant.now(),
        null,
        "",
        false,
        null,
        null
    );
    when(consultationViewService.mapConsultationRequestToView(eq(historicalRequest), eq(historicalResponse), eq(consulteeGroupDetail)))
        .thenReturn(requestView);

    var consulteeAdviceView = consulteeAdviceService.getConsulteeAdviceView(context);

    assertThat(consulteeAdviceView.getConsulteeGroupName()).isEqualTo(consulteeGroupDetail.getName());
    assertThat(consulteeAdviceView.getActiveRequestView()).isNull();
    assertThat(consulteeAdviceView.getHistoricRequestViews()).hasSize(1);
    assertThat(consulteeAdviceView.getHistoricRequestViews().get(0)).isEqualTo(requestView);

  }

  @Test
  public void getConsulteeAdviceView_activeRequest() {

    var historicalRequest = new ConsultationRequest();
    historicalRequest.setStatus(ConsultationRequestStatus.RESPONDED);

    var historicalResponse = new ConsultationResponse();
    historicalResponse.setConsultationRequest(historicalRequest);

    var activeRequest = new ConsultationRequest();
    activeRequest.setStatus(ConsultationRequestStatus.ALLOCATION);

    when(consultationResponseService.getResponsesByConsultationRequests(any())).thenReturn(List.of(historicalResponse));

    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), activeRequest, List.of(historicalRequest), false);
    var appInvolvement = new ApplicationInvolvementDto(detail.getPwaApplication(), Set.of(), consultationInvolvement, false,
        false);
    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CONSULTEE_ADVICE), null, appInvolvement);

    var historicRequestView = new ConsultationRequestView(
        null,
        null,
        Instant.now(),
        null,
        "",
        false,
        null,
        null
    );

    var activeRequestView = new ConsultationRequestView(
        null,
        null,
        Instant.now().minusSeconds(60),
        null,
        "",
        false,
        null,
        null
    );

    when(consultationViewService.mapConsultationRequestToView(activeRequest, null, consulteeGroupDetail))
        .thenReturn(activeRequestView);

    when(consultationViewService.mapConsultationRequestToView(historicalRequest, historicalResponse, consulteeGroupDetail))
        .thenReturn(historicRequestView);

    var consulteeAdviceView = consulteeAdviceService.getConsulteeAdviceView(context);

    assertThat(consulteeAdviceView.getConsulteeGroupName()).isEqualTo(consulteeGroupDetail.getName());
    assertThat(consulteeAdviceView.getActiveRequestView()).isEqualTo(activeRequestView);
    assertThat(consulteeAdviceView.getHistoricRequestViews()).hasSize(1);
    assertThat(consulteeAdviceView.getHistoricRequestViews().get(0)).isEqualTo(historicRequestView);

  }

}
