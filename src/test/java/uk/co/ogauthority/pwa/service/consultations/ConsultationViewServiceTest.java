package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;


@RunWith(MockitoJUnitRunner.class)
public class ConsultationViewServiceTest {

  private ConsultationViewService consultationViewService;

  @Mock
  private ConsultationRequestService consultationRequestService;
  @Mock
  private ConsultationResponseService consultationResponseService;
  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;
  @Mock
  private TeamManagementService teamManagementService;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    consultationViewService = new ConsultationViewService(consultationRequestService, consultationResponseService, consulteeGroupDetailService,
        teamManagementService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }



  //This tests that a list of consultation requests should result in a list of consultation request views grouped by their consultee group..
  // and ordered by the consultee group name.
  //Where multiple same group requests should be stored each as a requestView in a nested list within each parent requestView
  @Test
  public void getConsultationRequestViews() {
    var consulteeGroup1 = new ConsulteeGroup();
    consulteeGroup1.setId(1);
    var consulteeGroup2 = new ConsulteeGroup();
    consulteeGroup2.setId(2);

    //Create 2 Group Details - each assigned a different Consultee Group
    var consulteeGroupDetail1 = new ConsulteeGroupDetail();
    consulteeGroupDetail1.setName("nameB");
    consulteeGroupDetail1.setConsulteeGroup(consulteeGroup1);
    var consulteeGroupDetail2 = new ConsulteeGroupDetail();
    consulteeGroupDetail2.setName("nameA");
    consulteeGroupDetail2.setConsulteeGroup(consulteeGroup2);

    //Create 3 Consultation Requests - 2 requests of the same group with 1 older request. The other request from a separate Consultee Group
    var instantTime = Instant.now();
    var consultationRequest1 = new ConsultationRequest();
    consultationRequest1.setConsulteeGroup(consulteeGroup1);
    consultationRequest1.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest1.setDeadlineDate(Instant.now());
    consultationRequest1.setStatus(ConsultationRequestStatus.ALLOCATION);


    var consultationResponse = new ConsultationResponse();
    consultationResponse.setResponseType(ConsultationResponseOption.CONFIRMED);
    consultationResponse.setRespondingPersonId(1);
    when(teamManagementService.getPerson(1)).thenReturn(new Person(1, "Michael", "Scott", null, null));

    var consultationRequest2 = new ConsultationRequest();
    consultationRequest2.setConsulteeGroup(consulteeGroup2);
    consultationRequest2.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(8).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest2.setDeadlineDate(Instant.now());
    consultationRequest2.setStatus(ConsultationRequestStatus.ALLOCATION);
    when(consultationResponseService.getResponseByConsultationRequest(consultationRequest2)).thenReturn(Optional.of(consultationResponse));

    var consultationRequest3 = new ConsultationRequest();
    consultationRequest3.setConsulteeGroup(consulteeGroup1);
    consultationRequest3.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(4).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest3.setDeadlineDate(Instant.now());
    consultationRequest3.setStatus(ConsultationRequestStatus.ALLOCATION);

    //consultationRequest1: name - nameB, startDate - 5/02/2020
    //consultationRequest2: name - nameA, startDate 8/02/2020
    //consultationRequest3: name - nameB, startDate 4/02/2020
    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    when(consultationRequestService.getAllRequestsByApplication(pwaApplication))
        .thenReturn(List.of(consultationRequest1, consultationRequest3, consultationRequest2));

    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroup(consulteeGroup1)).thenReturn(consulteeGroupDetail1);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroup(consulteeGroup2)).thenReturn(consulteeGroupDetail2);

    List<ConsulteeGroupRequestsView> consultationRequestViews = consultationViewService.getConsultationRequestViews(pwaApplication);

    assertThat(consultationRequestViews.get(0).getCurrentRequest().getConsulteeGroupName()).isEqualTo("nameA");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getRequestDateDisplay()).isEqualTo("08 February 2020 10:09");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getResponseType()).isEqualTo(ConsultationResponseOption.CONFIRMED);
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getResponseByPerson()).isEqualTo("Michael Scott");

    assertThat(consultationRequestViews.get(1).getCurrentRequest().getConsulteeGroupName()).isEqualTo("nameB");
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getResponseType()).isNull();
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getResponseByPerson()).isNull();

    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getConsulteeGroupName()).isEqualTo("nameB");
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getRequestDateDisplay()).isEqualTo("04 February 2020 10:09");
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getResponseType()).isNull();
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getResponseByPerson()).isNull();
  }





}