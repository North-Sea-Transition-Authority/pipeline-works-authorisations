package uk.co.ogauthority.pwa.service.consultations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;


@RunWith(MockitoJUnitRunner.class)
public class ConsultationRequestServiceTest {

  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsultationRequestRepository consultationRequestRepository;
  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;
  @Mock
  CamundaWorkflowService camundaWorkflowService;


  private ConsultationRequestValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;


  @Before
  public void setUp() {
    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());
    validator = new ConsultationRequestValidator();
    consultationRequestService = new ConsultationRequestService(consulteeGroupTeamService, consultationRequestRepository, validator, camundaWorkflowService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  private ConsultationRequest createValidEntity() {
    var entity = new ConsultationRequest();
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setDeadlineDate(Instant.now());
    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    entity.setConsulteeGroupDetail(groupDetail);
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
    when(consulteeGroupTeamService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    consultationRequestService.saveEntitiesAndStartWorkflow(form, pwaApplicationDetail, authenticatedUserAccount);
    verify(consultationRequestRepository, times(1)).save(createValidEntity());
  }

  @Test
  public void saveEntitiesUsingForm_otherSelected() {
    var form = new ConsultationRequestForm();
    form.setOtherGroupSelected(true);
    form.setOtherGroupLogin("my login");
    form.setDaysToRespond(22);

    var expectedEntity =  createValidEntity();
    expectedEntity.setConsulteeGroupDetail(null);
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

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    consultationRequestService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = new ConsultationRequestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    consultationRequestService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }



}