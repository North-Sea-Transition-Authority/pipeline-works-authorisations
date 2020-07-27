package uk.co.ogauthority.pwa.service.consultations;

import static org.junit.Assert.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
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


  private ConsultationRequest createValidEntity() {
    var entity = new ConsultationRequest();
    entity.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    entity.setDeadlineDate(Instant.now());
    entity.setConsulteeGroup(new ConsulteeGroup());
    entity.setStartedByPersonId(authenticatedUserAccount.getLinkedPerson().getId().asInt());
    entity.setOtherGroupSelected(false);

    return entity;
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
  }

  @Test
  public void saveEntitiesUsingForm_otherSelected() {
    var form = new ConsultationRequestForm();
    form.setOtherGroupSelected(true);
    form.setOtherGroupLogin("my login");
    form.setDaysToRespond(22);

    var expectedEntity =  createValidEntity();
    expectedEntity.setConsulteeGroup(null);
    expectedEntity.setOtherGroupSelected(true);
    expectedEntity.setOtherGroupLogin("my login");

    consultationRequestService.saveEntitiesAndStartWorkflow(form, pwaApplicationDetail, authenticatedUserAccount);
    verify(consultationRequestRepository, times(1)).save(expectedEntity);
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



}