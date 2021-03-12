package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.publicnotices.PublicNoticePublicationEmailProps;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeFinalisationResult;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.FinalisePublicNoticeValidator;

@RunWith(MockitoJUnitRunner.class)
public class FinalisePublicNoticeServiceTest {

  private FinalisePublicNoticeService finalisePublicNoticeService;

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private FinalisePublicNoticeValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PublicNoticeDatesRepository publicNoticeDatesRepository;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Value("${service.name}")
  private String serviceName;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDate> publicNoticeDateArgumentCaptor;
  

  private PwaApplication pwaApplication;
  private AuthenticatedUserAccount user;



  @Before
  public void setUp() {

    finalisePublicNoticeService = new FinalisePublicNoticeService(publicNoticeService, validator,
        camundaWorkflowService, publicNoticeDatesRepository, serviceName, pwaContactService, notifyService, emailCaseLinkService);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  public void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  public void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of());
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  public void validate_verifyServiceInteractions() {

    var form = new FinalisePublicNoticeForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    finalisePublicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }



  @Test
  public void finalisePublicNotice_publicNoticeDateEntitySavedFromForm() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = PublicNoticeTestUtil.createStartBeforeTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.finalisePublicNotice(pwaApplication, form, user);

    verify(publicNoticeDatesRepository, times(1)).save(publicNoticeDateArgumentCaptor.capture());
    var actualPublicNoticeDate = publicNoticeDateArgumentCaptor.getValue();

    var expectedStartDate = LocalDate.of(form.getStartYear(), form.getStartMonth(), form.getStartDay());
    var actualStartDate = DateUtils.instantToLocalDate(actualPublicNoticeDate.getPublicationStartTimestamp());
    assertThat(actualStartDate).isEqualTo(expectedStartDate);

    var expectedEndDate = expectedStartDate.plusDays(form.getDaysToBePublishedFor());
    var actualEndDate = DateUtils.instantToLocalDate(actualPublicNoticeDate.getPublicationEndTimestamp());
    assertThat(actualEndDate).isEqualTo(expectedEndDate);

    assertThat(actualPublicNoticeDate.getPublicationDaysLength()).isEqualTo(Long.valueOf(form.getDaysToBePublishedFor()));
    assertThat(actualPublicNoticeDate.getPublicNotice()).isEqualTo(publicNotice);
    assertThat(actualPublicNoticeDate.getCreatedByPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
  }

  @Test
  public void finalisePublicNotice_startDateBeforeToday_workflowTransitionsToEndStage() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = PublicNoticeTestUtil.createStartBeforeTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.finalisePublicNotice(pwaApplication, form, user);

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        publicNotice, PwaApplicationPublicNoticeFinalisationResult.PUBLICATION_STARTED);
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));
  }

  @Test
  public void finalisePublicNotice_startDateAfterToday_workflowTransitionsToWaitingStage() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = PublicNoticeTestUtil.createAfterTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.finalisePublicNotice(pwaApplication, form, user);

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.WAITING);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        publicNotice, PwaApplicationPublicNoticeFinalisationResult.WAIT_FOR_START_DATE);
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));
  }

  @Test
  public void finalisePublicNotice_emailsSent() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    String caseManagementLink = "case management link url";
    when(emailCaseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var emailRecipients = List.of(PersonTestUtil.createDefaultPerson());
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .thenReturn(emailRecipients);

    var form = PublicNoticeTestUtil.createAfterTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.finalisePublicNotice(pwaApplication, form, user);

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticePublicationEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          caseManagementLink,
          DateUtils.formatDate(LocalDate.of(form.getStartYear(), form.getStartMonth(), form.getStartDay())),
          serviceName);

      verify(notifyService, times(1)).sendEmail(expectedEmailProps, recipient.getEmailAddress());
    });
  }

}
