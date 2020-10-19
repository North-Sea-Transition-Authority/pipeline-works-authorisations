package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestView;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;


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

  @Before
  public void setUp() {
    consultationViewService = new ConsultationViewService(consultationRequestService, consultationResponseService, consulteeGroupDetailService,
        teamManagementService);
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
    consultationRequest1.setId(1);
    consultationRequest1.setConsulteeGroup(consulteeGroup1);
    consultationRequest1.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest1.setDeadlineDate(Instant.now());
    consultationRequest1.setStatus(ConsultationRequestStatus.ALLOCATION);


    var consultationRequest2 = new ConsultationRequest();
    consultationRequest2.setId(2);
    consultationRequest2.setConsulteeGroup(consulteeGroup2);
    consultationRequest2.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(8).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest2.setDeadlineDate(Instant.now());
    consultationRequest2.setStatus(ConsultationRequestStatus.ALLOCATION);

    var consultationResponse = new ConsultationResponse();
    consultationResponse.setResponseType(ConsultationResponseOption.REJECTED);
    consultationResponse.setResponseText("my reason");
    consultationResponse.setResponseTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationResponse.setRespondingPersonId(1);
    consultationResponse.setConsultationRequest(consultationRequest2);
    when(teamManagementService.getPerson(1)).thenReturn(new Person(1, "Michael", "Scott", null, null));


    var consultationRequest3 = new ConsultationRequest();
    consultationRequest3.setId(3);
    consultationRequest3.setConsulteeGroup(consulteeGroup1);
    consultationRequest3.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(4).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest3.setDeadlineDate(Instant.now());
    consultationRequest3.setStatus(ConsultationRequestStatus.WITHDRAWN);
    consultationRequest3.setEndedByPersonId(2);
    consultationRequest3.setEndTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(9).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    when(teamManagementService.getPerson(2)).thenReturn(new Person(2, "David", "Henry", null, null));




    //consultationRequest1: name - nameB, startDate - 5/02/2020
    //consultationRequest2: name - nameA, startDate 8/02/2020
    //consultationRequest3: name - nameB, startDate 4/02/2020
    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    when(consultationRequestService.getAllRequestsByApplication(pwaApplication))
        .thenReturn(List.of(consultationRequest1, consultationRequest3, consultationRequest2));

    when(consultationResponseService.getResponsesByConsultationRequests(List.of(consultationRequest1, consultationRequest3, consultationRequest2))).thenReturn(List.of(consultationResponse));

    when(consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(Set.of(consulteeGroup1, consulteeGroup2)))
        .thenReturn(List.of(consulteeGroupDetail1, consulteeGroupDetail2));

    List<ConsulteeGroupRequestsView> consultationRequestViews = consultationViewService.getConsultationRequestViews(pwaApplication);

    assertThat(consultationRequestViews.get(0).getCurrentRequest().getConsulteeGroupName()).isEqualTo("nameA");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getRequestDateDisplay()).isEqualTo("08 February 2020 10:09");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getResponseType()).isEqualTo(ConsultationResponseOption.REJECTED);
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getResponseRejectionReason()).isEqualTo("my reason");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getResponseByPerson()).isEqualTo("Michael Scott");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getResponseDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getWithdrawnByUser()).isNull();

    assertThat(consultationRequestViews.get(1).getCurrentRequest().getConsulteeGroupName()).isEqualTo("nameB");
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getResponseType()).isNull();
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getResponseRejectionReason()).isNull();
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getResponseByPerson()).isNull();
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getResponseDateDisplay()).isNull();
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getWithdrawnByUser()).isNull();

    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getConsulteeGroupName()).isEqualTo("nameB");
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getRequestDateDisplay()).isEqualTo("04 February 2020 10:09");
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getResponseType()).isNull();
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getResponseRejectionReason()).isNull();
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getResponseByPerson()).isNull();
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getResponseDateDisplay()).isNull();
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getWithdrawnByUser()).isEqualTo("David Henry");
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getEndTimeStamp()).isEqualTo("09 February 2020 10:09");
  }


  //Single Request View Tests
  @Test
  public void getConsultationRequestView_allocation() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    var consulteeGroupDetail1 = new ConsulteeGroupDetail();
    consulteeGroupDetail1.setName("group name");
    consulteeGroupDetail1.setConsulteeGroup(consulteeGroup);

    var instantTime = Instant.now();
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest.setDeadlineDate(Instant.now());
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);

    when(consultationResponseService.getResponseByConsultationRequest(consultationRequest)).thenReturn(Optional.empty());
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup)).thenReturn(consulteeGroupDetail1);

    ConsultationRequestView requestView = consultationViewService.getConsultationRequestView(consultationRequest);

    assertThat(requestView.getConsulteeGroupName()).isEqualTo("group name");
    assertThat(requestView.getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(requestView.getResponseType()).isNull();
    assertThat(requestView.getResponseRejectionReason()).isNull();
    assertThat(requestView.getResponseByPerson()).isNull();
    assertThat(requestView.getResponseDateDisplay()).isNull();

  }


  @Test
  public void getConsultationRequestView_responseRejected() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    var consulteeGroupDetail1 = new ConsulteeGroupDetail();
    consulteeGroupDetail1.setName("group name");
    consulteeGroupDetail1.setConsulteeGroup(consulteeGroup);

    var instantTime = Instant.now();
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setId(1);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest.setDeadlineDate(Instant.now());
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);

    var consultationResponse = new ConsultationResponse();
    consultationResponse.setResponseType(ConsultationResponseOption.REJECTED);
    consultationResponse.setResponseText("my reason");
    consultationResponse.setResponseTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationResponse.setRespondingPersonId(1);
    consultationResponse.setConsultationRequest(consultationRequest);
    when(teamManagementService.getPerson(1)).thenReturn(new Person(1, "Michael", "Scott", null, null));

    when(consultationResponseService.getResponseByConsultationRequest(consultationRequest)).thenReturn(Optional.of(consultationResponse));
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup)).thenReturn(consulteeGroupDetail1);

    ConsultationRequestView requestView = consultationViewService.getConsultationRequestView(consultationRequest);

    assertThat(requestView.getConsulteeGroupName()).isEqualTo("group name");
    assertThat(requestView.getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(requestView.getResponseType()).isEqualTo(ConsultationResponseOption.REJECTED);
    assertThat(requestView.getResponseRejectionReason()).isEqualTo("my reason");
    assertThat(requestView.getResponseByPerson()).isEqualTo("Michael Scott");
    assertThat(requestView.getResponseDateDisplay()).isEqualTo("05 February 2020 10:09");
  }


  @Test
  public void getConsultationRequestViewsRespondedOnly() {
    //group and detail
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setName("nameA");
    consulteeGroupDetail.setConsulteeGroup(consulteeGroup);

    //Consultation Requests 1 and response
    var instantTime = Instant.now();
    var consultationRequest1 = new ConsultationRequest();
    consultationRequest1.setId(1);
    consultationRequest1.setConsulteeGroup(consulteeGroup);
    consultationRequest1.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest1.setDeadlineDate(Instant.now());
    consultationRequest1.setStatus(ConsultationRequestStatus.RESPONDED);

    var consultationResponse1 = new ConsultationResponse();
    consultationResponse1.setResponseType(ConsultationResponseOption.REJECTED);
    consultationResponse1.setResponseText("my reason");
    consultationResponse1.setResponseTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationResponse1.setRespondingPersonId(1);
    consultationResponse1.setConsultationRequest(consultationRequest1);
    when(teamManagementService.getPerson(1)).thenReturn(new Person(1, "fr1", "sr1", null, null));


    //Consultation Requests 2 and response
    var consultationRequest2 = new ConsultationRequest();
    consultationRequest2.setId(2);
    consultationRequest2.setConsulteeGroup(consulteeGroup);
    consultationRequest2.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(8).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest2.setDeadlineDate(Instant.now());
    consultationRequest2.setDeadlineDate(Instant.now());
    consultationRequest2.setStatus(ConsultationRequestStatus.RESPONDED);

    var consultationResponse2 = new ConsultationResponse();
    consultationResponse2.setResponseType(ConsultationResponseOption.CONFIRMED);
    consultationResponse2.setResponseText("confirm text");
    consultationResponse2.setResponseTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(11).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationResponse2.setRespondingPersonId(2);
    consultationResponse2.setConsultationRequest(consultationRequest2);
    when(teamManagementService.getPerson(2)).thenReturn(new Person(1, "fr2", "sr2", null, null));




    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    when(consultationRequestService.getAllRequestsByAppAndGroupRespondedOnly(pwaApplication, consulteeGroup))
        .thenReturn(List.of(consultationRequest2, consultationRequest1));

    when(consultationResponseService.getResponsesByConsultationRequests(List.of(consultationRequest2, consultationRequest1))).thenReturn(List.of(consultationResponse2, consultationResponse1));

    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup))
        .thenReturn(consulteeGroupDetail);

    var consultationRequestToRespondOn = new ConsultationRequest();
    consultationRequestToRespondOn.setConsulteeGroup(consulteeGroup);
    List<ConsultationRequestView> consultationRequestViews = consultationViewService.getConsultationRequestViewsRespondedOnly(pwaApplication, consultationRequestToRespondOn);


    assertThat(consultationRequestViews.get(0).getConsulteeGroupName()).isEqualTo("nameA");
    assertThat(consultationRequestViews.get(0).getRequestDateDisplay()).isEqualTo("08 February 2020 10:09");
    assertThat(consultationRequestViews.get(0).getResponseType()).isEqualTo(ConsultationResponseOption.CONFIRMED);
    assertThat(consultationRequestViews.get(0).getResponseConfirmReason()).isEqualTo("confirm text");
    assertThat(consultationRequestViews.get(0).getResponseRejectionReason()).isNull();
    assertThat(consultationRequestViews.get(0).getResponseByPerson()).isEqualTo("fr2 sr2");
    assertThat(consultationRequestViews.get(0).getResponseDateDisplay()).isEqualTo("11 February 2020 10:09");
    assertThat(consultationRequestViews.get(0).getWithdrawnByUser()).isNull();

    assertThat(consultationRequestViews.get(1).getConsulteeGroupName()).isEqualTo("nameA");
    assertThat(consultationRequestViews.get(1).getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(consultationRequestViews.get(1).getResponseType()).isEqualTo(ConsultationResponseOption.REJECTED);
    assertThat(consultationRequestViews.get(1).getResponseRejectionReason()).isEqualTo("my reason");
    assertThat(consultationRequestViews.get(1).getResponseByPerson()).isEqualTo("fr1 sr1");
    assertThat(consultationRequestViews.get(1).getResponseDateDisplay()).isEqualTo("05 February 2020 10:09");
    assertThat(consultationRequestViews.get(1).getWithdrawnByUser()).isNull();

  }



  @Test
  public void requestViewRequestDateDisplayCreation_noResponseDataConstructor() {
    var instantTime = Instant.now();
    var consulationRequest = new ConsultationRequestView(null, null,
        instantTime.atZone(ZoneOffset.UTC).withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS),
        null, null, null, null, null);

    assertThat(consulationRequest.getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
  }

  @Test
  public void requestViewRequestDateDisplayCreation_withResponseDataConstructor() {
    var instantTime = Instant.now();
    var consulationRequest = new ConsultationRequestView(null, null,
        instantTime.atZone(ZoneOffset.UTC).withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS),
        null, null,
        instantTime.atZone(ZoneOffset.UTC).withDayOfMonth(6).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS),
        null, null, null, null, null);

    assertThat(consulationRequest.getRequestDateDisplay()).isEqualTo("05 February 2020 10:09");
  }

  @Test
  public void requestViewResponseDateDisplayCreation_noResponseDataConstructor() {
    var instantTime = Instant.now();
    var consulationRequest = new ConsultationRequestView(null, null,
        instantTime.atZone(ZoneOffset.UTC).withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS),
        null, null, null, null, null);

    assertThat(consulationRequest.getResponseDateDisplay()).isNull();
  }

  @Test
  public void requestViewResponseDateDisplayCreation_withResponseDataConstructor() {
    var instantTime = Instant.now();
    var consulationRequest = new ConsultationRequestView(null, null,
        instantTime.atZone(ZoneOffset.UTC).withDayOfMonth(5).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS),
        null, null,
        instantTime.atZone(ZoneOffset.UTC).withDayOfMonth(6).withMonth(2).withYear(2020).withHour(10).withMinute(9).toInstant().truncatedTo(ChronoUnit.SECONDS),
        null, null, null, null, null);

    assertThat(consulationRequest.getResponseDateDisplay()).isEqualTo("06 February 2020 10:09");
  }



}