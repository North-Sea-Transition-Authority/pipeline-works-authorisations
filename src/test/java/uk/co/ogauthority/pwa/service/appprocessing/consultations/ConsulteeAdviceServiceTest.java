package uk.co.ogauthority.pwa.service.appprocessing.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.consultations.ConsultationFileService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseDataService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseDataView;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationViewService;
import uk.co.ogauthority.pwa.service.consultations.ConsulteeAdviceService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
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

  @Mock
  private ConsultationResponseDataService consultationResponseDataService;

  @Mock
  private ConsultationFileService consultationFileService;

  private ConsulteeAdviceService consulteeAdviceService;

  private PwaApplicationDetail detail;
  private WebUserAccount user;

  private ConsulteeGroupDetail consulteeGroupDetail;

  private UploadedFileView fileView;

  private final String DOWNLOAD_URL = "/file/download";

  @Before
  public void setUp() {

    consulteeAdviceService = new ConsulteeAdviceService(consultationResponseService, consultationViewService, consultationResponseDataService,
        consultationFileService);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    user = new WebUserAccount(1);
    consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "ab");

    fileView = new UploadedFileView("id", "name", 1L, "desc", Instant.now(), "#id");

  }

  @Test
  public void canShowInTaskList_appEnded_hidden() {

    detail.setStatus(PwaApplicationStatus.COMPLETE);
    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        detail.getPwaApplication(), consultationInvolvement);

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE), null, appInvolvement,
        Set.of());

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_appNotEnded_noConsulteeAdvicePermission_hidden() {

    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        detail.getPwaApplication(), consultationInvolvement);

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE), null, appInvolvement,
        Set.of());

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_appNotEnded_consulteeAdvicePermission_shown() {

    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(new ConsultationRequest()), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        detail.getPwaApplication(), consultationInvolvement);

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CONSULTEE_ADVICE), null, appInvolvement,
        Set.of());

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_oga_hidden() {

    var appInvolvement = PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication());

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA), null, appInvolvement,
        Set.of());

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_industry_hidden() {

    var appInvolvement = PwaAppProcessingContextDtoTestUtils.emptyAppInvolvement(detail.getPwaApplication());

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY), null, appInvolvement,
        Set.of());

    boolean canShow = consulteeAdviceService.canShowInTaskList(context);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry_default() {


    var context = new PwaAppProcessingContext(detail, user, Set.of(), null, null, Set.of());
    var taskListEntry = consulteeAdviceService.getTaskListEntry(PwaAppProcessingTask.CONSULTEE_ADVICE, context);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTEE_ADVICE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTEE_ADVICE.getRoute(context));
    assertThat(taskListEntry.getTaskTag()).isNull();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CONSULTEE_ADVICE.getDisplayOrder());

  }

  @Test
  public void getConsulteeAdviceView_noActiveRequest() {

    var historicalRequest = new ConsultationRequest();
    historicalRequest.setId(1);
    historicalRequest.setStatus(ConsultationRequestStatus.RESPONDED);

    var historicalResponse = new ConsultationResponse();
    historicalResponse.setId(1);
    historicalResponse.setConsultationRequest(historicalRequest);

    when(consultationResponseService.getResponsesByConsultationRequests(any())).thenReturn(List.of(historicalResponse));

    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), null, List.of(historicalRequest), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil
        .generateConsulteeInvolvement(detail.getPwaApplication(), consultationInvolvement);

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CONSULTEE_ADVICE), null, appInvolvement, Set.of());

    var requestView = new ConsultationRequestView(
        null,
        consulteeGroupDetail.getName(),
        Instant.now(),
        null,
        "",
        List.of(),
        false,
        null,
        null,
        ConsultationResponseDocumentType.DEFAULT);

    when(consultationViewService.mapConsultationRequestToView(eq(historicalRequest), eq(historicalResponse), any(), eq(consulteeGroupDetail), eq(List.of(fileView)),
        eq(DOWNLOAD_URL)))
        .thenReturn(requestView);

    when(consultationFileService.getConsultationResponseIdToFileViewsMap(appInvolvement.getPwaApplication(), Set.of(historicalResponse)))
        .thenReturn(Map.of(historicalResponse.getId(), List.of(fileView)));
    when(consultationFileService.getConsultationFileViewUrl(historicalRequest))
        .thenReturn(DOWNLOAD_URL);

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
    historicalResponse.setId(10);
    historicalResponse.setConsultationRequest(historicalRequest);

    var activeRequest = new ConsultationRequest();
    activeRequest.setId(11);
    activeRequest.setStatus(ConsultationRequestStatus.ALLOCATION);

    when(consultationResponseService.getResponsesByConsultationRequests(any())).thenReturn(List.of(historicalResponse));

    var consultationInvolvement = new ConsultationInvolvementDto(consulteeGroupDetail, Set.of(), activeRequest, List.of(historicalRequest), false);
    var appInvolvement = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        detail.getPwaApplication(), consultationInvolvement);

    var context = new PwaAppProcessingContext(detail, user, Set.of(PwaAppProcessingPermission.CONSULTEE_ADVICE), null, appInvolvement,
        Set.of());

    var data = new ConsultationResponseData(historicalResponse);
    data.setResponseGroup(ConsultationResponseOptionGroup.CONTENT);
    data.setResponseType(ConsultationResponseOption.CONFIRMED);
    data.setResponseText("ttt");

    var dataView = ConsultationResponseDataView.from(data);

    var historicRequestView = new ConsultationRequestView(
        null,
        null,
        Instant.now(),
        null,
        "",
        Instant.now(),
        List.of(dataView),
        null,
        null,
        List.of(fileView),
        DOWNLOAD_URL,
        ConsultationResponseDocumentType.DEFAULT);

    var activeRequestView = new ConsultationRequestView(
        null,
        null,
        Instant.now().minusSeconds(60),
        null,
        "",
        Instant.now(),
        List.of(),
        false,
        null,
        List.of(fileView),
        DOWNLOAD_URL,
        ConsultationResponseDocumentType.DEFAULT);

    when(consultationViewService.mapConsultationRequestToView(activeRequest, null, List.of(), consulteeGroupDetail, List.of(),
        DOWNLOAD_URL))
        .thenReturn(activeRequestView);

    when(consultationViewService.mapConsultationRequestToView(eq(historicalRequest), eq(historicalResponse), any(), eq(consulteeGroupDetail),
        eq(List.of(fileView)), eq(DOWNLOAD_URL)))
        .thenReturn(historicRequestView);

    when(consultationFileService.getConsultationResponseIdToFileViewsMap(eq(appInvolvement.getPwaApplication()), any()))
        .thenReturn(Map.of(historicalResponse.getId(), List.of(fileView)));
    when(consultationFileService.getConsultationFileViewUrl(any()))
        .thenReturn(DOWNLOAD_URL);

    var consulteeAdviceView = consulteeAdviceService.getConsulteeAdviceView(context);

    assertThat(consulteeAdviceView.getConsulteeGroupName()).isEqualTo(consulteeGroupDetail.getName());
    assertThat(consulteeAdviceView.getActiveRequestView()).isEqualTo(activeRequestView);
    assertThat(consulteeAdviceView.getHistoricRequestViews()).hasSize(1);
    assertThat(consulteeAdviceView.getHistoricRequestViews().get(0)).isEqualTo(historicRequestView);
    assertThat(consulteeAdviceView.getHistoricRequestViews().get(0))
        .satisfies(view -> assertThat(view.getDataList()).containsExactly(dataView));

  }

}
