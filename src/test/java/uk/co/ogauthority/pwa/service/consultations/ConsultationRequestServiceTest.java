package uk.co.ogauthority.pwa.service.consultations;

import static org.junit.Assert.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;


@RunWith(MockitoJUnitRunner.class)
public class ConsultationRequestServiceTest {

  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsultationRequestRepository consultationRequestRepository;
  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;
  @Mock
  CamundaWorkflowService camundaWorkflowService;


  @Captor
  private ArgumentCaptor<ConsultationRequest> consultationRequestArgumentCaptor;


  private ConsultationRequestValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;


  @Before
  public void setUp() {
    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());
    validator = new ConsultationRequestValidator();
    consultationRequestService = new ConsultationRequestService(consulteeGroupDetailService, consultationRequestRepository, validator, camundaWorkflowService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  @Test
  public void saveEntitiesUsingForm_consulteeGroupSelected() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(22);

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    groupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    consultationRequestService.saveEntitiesAndStartWorkflow(form, pwaApplicationDetail, authenticatedUserAccount);
    verify(consultationRequestRepository, times(1)).save(consultationRequestArgumentCaptor.capture());

    assertThat(consultationRequestArgumentCaptor.getValue().getConsulteeGroup().getId()).isEqualTo(1);
    var expectedDeadline = Instant.now().plus(Period.ofDays(form.getDaysToRespond()));
    assertThat(consultationRequestArgumentCaptor.getValue().getDeadlineDate().atZone(ZoneOffset.UTC).getDayOfYear()).isEqualTo(expectedDeadline.atZone(ZoneOffset.UTC).getDayOfYear());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(
        ConsultationRequestStatus.ALLOCATION);
  }
  

  @Test
  public void validate_valid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(22);

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    groupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    consultationRequestService.validate(form, bindingResult, pwaApplicationDetail.getPwaApplication());
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = new ConsultationRequestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    consultationRequestService.validate(form, bindingResult, pwaApplicationDetail.getPwaApplication());
    assertTrue(bindingResult.hasErrors());
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
        .withDayOfMonth(5).withMonth(2).withYear(2020).withNano(0).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest1.setDeadlineDate(Instant.now());
    consultationRequest1.setStatus(ConsultationRequestStatus.ALLOCATION);

    var consultationRequest2 = new ConsultationRequest();
    consultationRequest2.setConsulteeGroup(consulteeGroup2);
    consultationRequest2.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(8).withMonth(2).withYear(2020).withNano(0).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest2.setDeadlineDate(Instant.now());
    consultationRequest2.setStatus(ConsultationRequestStatus.ALLOCATION);

    var consultationRequest3 = new ConsultationRequest();
    consultationRequest3.setConsulteeGroup(consulteeGroup1);
    consultationRequest3.setStartTimestamp(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(4).withMonth(2).withYear(2020).withNano(0).toInstant().truncatedTo(ChronoUnit.SECONDS));
    consultationRequest3.setDeadlineDate(Instant.now());
    consultationRequest3.setStatus(ConsultationRequestStatus.ALLOCATION);

    //consultationRequest1: name - nameB, startDate - 5/02/2020
    //consultationRequest2: name - nameA, startDate 8/02/2020
    //consultationRequest3: name - nameB, startDate 4/02/2020
    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    when(consultationRequestRepository.findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(pwaApplication))
        .thenReturn(List.of(consultationRequest1, consultationRequest3, consultationRequest2));

    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroup(consulteeGroup1)).thenReturn(consulteeGroupDetail1);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroup(consulteeGroup2)).thenReturn(consulteeGroupDetail2);

    List<ConsulteeGroupRequestsView> consultationRequestViews = consultationRequestService.getConsultationRequestViews(pwaApplication);

    assertThat(consultationRequestViews.get(0).getCurrentRequest().getConsulteeGroupName()).isEqualTo("nameA");
    assertThat(consultationRequestViews.get(0).getCurrentRequest().getRequestDate()).isEqualTo(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(8).withMonth(2).withYear(2020).toInstant().truncatedTo(ChronoUnit.SECONDS));

    assertThat(consultationRequestViews.get(1).getCurrentRequest().getConsulteeGroupName()).isEqualTo("nameB");
    assertThat(consultationRequestViews.get(1).getCurrentRequest().getRequestDate()).isEqualTo(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(5).withMonth(2).withYear(2020).toInstant().truncatedTo(ChronoUnit.SECONDS));

    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getConsulteeGroupName()).isEqualTo("nameB");
    assertThat(consultationRequestViews.get(1).getHistoricalRequests().get(0).getRequestDate()).isEqualTo(instantTime.atZone(ZoneOffset.UTC)
        .withDayOfMonth(4).withMonth(2).withYear(2020).toInstant().truncatedTo(ChronoUnit.SECONDS));
  }





}