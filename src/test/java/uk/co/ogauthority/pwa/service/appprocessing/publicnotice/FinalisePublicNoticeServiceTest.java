package uk.co.ogauthority.pwa.service.appprocessing.publicnotice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticePublicationEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.publicnotices.PublicNoticePublicationUpdateEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNotice;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.model.form.publicnotice.FinalisePublicNoticeForm;
import uk.co.ogauthority.pwa.repository.publicnotice.PublicNoticeDatesRepository;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PublicNoticeCaseOfficerReviewResult;
import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeWorkflowTask;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.publicnotice.FinalisePublicNoticeValidator;

@ExtendWith(MockitoExtension.class)
class FinalisePublicNoticeServiceTest {

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
  private CaseLinkService caseLinkService;

  @Mock
  private Clock clock;

  @Mock
  private EmailService emailService;

  @Value("${service.full-name}")
  private String serviceName;

  @Captor
  private ArgumentCaptor<PublicNotice> publicNoticeArgumentCaptor;

  @Captor
  private ArgumentCaptor<PublicNoticeDate> publicNoticeDateArgumentCaptor;


  private PwaApplication pwaApplication;
  private AuthenticatedUserAccount user;


  @BeforeEach
  void setUp() {

    finalisePublicNoticeService = new FinalisePublicNoticeService(publicNoticeService, validator,
        camundaWorkflowService, publicNoticeDatesRepository, serviceName, pwaContactService,
        caseLinkService, clock, emailService);

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();
    user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of());
  }


  @Test
  void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedExistsWithApp() {
    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedExistsWithDifferentApp() {
    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(new PwaApplication());
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of(publicNotice));
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }

  @Test
  void publicNoticeCanBeFinalised_publicNoticeThatCanBeFinalisedDoesNotExist() {
    when(publicNoticeService.getPublicNoticesByStatus(PublicNoticeStatus.CASE_OFFICER_REVIEW)).thenReturn(List.of());
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeCanBeFinalised(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }


  @Test
  void publicNoticeDatesCanBeUpdated_updatablePublicNoticeDatesExistsWithApp() {
    var pwaApplication = new PwaApplication();
    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeDatesCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isTrue();
  }

  @Test
  void publicNoticeDatesCanBeUpdated_updatablePublicNoticeDatesDoesNotExist() {
    var publicNoticeExists = finalisePublicNoticeService.publicNoticeDatesCanBeUpdated(pwaApplication);
    assertThat(publicNoticeExists).isFalse();
  }


  @Test
  void validate_verifyServiceInteractions() {

    var form = new FinalisePublicNoticeForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    finalisePublicNoticeService.validate(form, bindingResult);
    verify(validator, times(1)).validate(form, bindingResult);
  }


  @Test
  void finalisePublicNotice_publicNoticeDateEntitySavedFromForm() {

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
    assertThat(actualPublicNoticeDate.getCreatedTimestamp()).isEqualTo(clock.instant());
  }

  @Test
  void finalisePublicNotice_startDateBeforeToday_workflowTransitionsToPublishStage() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = PublicNoticeTestUtil.createStartBeforeTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.finalisePublicNotice(pwaApplication, form, user);

    //Expected twice, once to set to waiting, once to set to published
    verify(publicNoticeService, times(2)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.WAITING));
  }

  @Test
  void finalisePublicNotice_startDateAfterToday_workflowTransitionsToWaitingStage() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    var form = PublicNoticeTestUtil.createAfterTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.finalisePublicNotice(pwaApplication, form, user);

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.WAITING);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        publicNotice, PublicNoticeCaseOfficerReviewResult.WAIT_FOR_START_DATE);
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.CASE_OFFICER_REVIEW));
  }

  @Test
  void finalisePublicNotice_emailsSent() {

    var publicNotice = PublicNoticeTestUtil.createCaseOfficerReviewPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication))
        .thenReturn(publicNotice);

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
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

      verify(emailService, times(1)).sendEmail(expectedEmailProps, recipient, pwaApplication.getAppReference());
    });
  }


  @Test
  void updatePublicNoticeDate_existingDateEntityEnded_newDateEntitySavedFromForm() {

    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publicNotice);
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice))
        .thenReturn(Optional.of(publicNoticeDate));

    var form = PublicNoticeTestUtil.createAfterTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.updatePublicNoticeDate(pwaApplication, form, user);


    verify(publicNoticeDatesRepository, times(2)).save(publicNoticeDateArgumentCaptor.capture());
    var actualExistingPublicNoticeDate = publicNoticeDateArgumentCaptor.getAllValues().get(0);
    assertThat(actualExistingPublicNoticeDate.getEndedByPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
    assertThat(actualExistingPublicNoticeDate.getEndedTimestamp()).isEqualTo(clock.instant());

    var expectedStartDate = LocalDate.of(form.getStartYear(), form.getStartMonth(), form.getStartDay());
    var actualNewPublicNoticeDate = publicNoticeDateArgumentCaptor.getAllValues().get(1);
    var actualNewPublicationStartDate = DateUtils.instantToLocalDate(actualNewPublicNoticeDate.getPublicationStartTimestamp());
    assertThat(actualNewPublicationStartDate).isEqualTo(expectedStartDate);
    assertThat(actualNewPublicNoticeDate.getCreatedTimestamp()).isEqualTo(clock.instant());

    var expectedEndDate = expectedStartDate.plusDays(form.getDaysToBePublishedFor());
    var actualNewPublicationEndDate = DateUtils.instantToLocalDate(actualNewPublicNoticeDate.getPublicationEndTimestamp());
    assertThat(actualNewPublicationEndDate).isEqualTo(expectedEndDate);

    assertThat(actualNewPublicNoticeDate.getPublicationDaysLength()).isEqualTo(Long.valueOf(form.getDaysToBePublishedFor()));
    assertThat(actualNewPublicNoticeDate.getPublicNotice()).isEqualTo(publicNotice);
    assertThat(actualNewPublicNoticeDate.getCreatedByPersonId()).isEqualTo(user.getLinkedPerson().getId().asInt());
  }

  @Test
  void updatePublicNoticeDate_startDateAfterToday_workflowRemainsAtWaitingStage() {

    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publicNotice);
    publicNoticeDate.setPublicationStartTimestamp(publicNoticeDate.getPublicationStartTimestamp().plus(60, ChronoUnit.DAYS));
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice))
        .thenReturn(Optional.of(publicNoticeDate));

    var form = PublicNoticeTestUtil.createAfterTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.updatePublicNoticeDate(pwaApplication, form, user);

    verifyNoInteractions(camundaWorkflowService);
    verify(publicNoticeService, never()).savePublicNotice(publicNotice);
  }

  @Test
  void updatePublicNoticeDate_startDateBeforeToday_workflowTransitionsToEndStage() {

    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publicNotice);
    publicNoticeDate.setPublicationStartTimestamp(publicNoticeDate.getPublicationStartTimestamp().plus(60, ChronoUnit.DAYS));
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice))
        .thenReturn(Optional.of(publicNoticeDate));

    var form = PublicNoticeTestUtil.createStartBeforeTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.updatePublicNoticeDate(pwaApplication, form, user);

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.WAITING));
  }

  @Test
  void updatePublicNoticeDate_emailsSent() {

    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publicNotice);
    publicNoticeDate.setPublicationStartTimestamp(publicNoticeDate.getPublicationStartTimestamp().plus(60, ChronoUnit.DAYS));
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice))
        .thenReturn(Optional.of(publicNoticeDate));

    String caseManagementLink = "case management link url";
    when(caseLinkService.generateCaseManagementLink(pwaApplication)).thenReturn(caseManagementLink);
    var emailRecipients = List.of(PersonTestUtil.createDefaultPerson());
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplication, PwaContactRole.PREPARER))
        .thenReturn(emailRecipients);

    var form = PublicNoticeTestUtil.createStartBeforeTodayFinalisePublicNoticeForm();
    finalisePublicNoticeService.updatePublicNoticeDate(pwaApplication, form, user);

    emailRecipients.forEach(recipient -> {
      var expectedEmailProps = new PublicNoticePublicationUpdateEmailProps(
          recipient.getFullName(),
          pwaApplication.getAppReference(),
          caseManagementLink,
          DateUtils.formatDate(LocalDate.of(form.getStartYear(), form.getStartMonth(), form.getStartDay())));

      verify(emailService, times(1)).sendEmail(expectedEmailProps, recipient, pwaApplication.getAppReference());
    });
  }

  @Test
  void publishPublicNotice_verifyServiceInteractions_statusUpdatedToPublished() {

    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    finalisePublicNoticeService.publishPublicNotice(publicNotice);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(publicNotice,
        PwaApplicationPublicNoticeWorkflowTask.WAITING));

    verify(publicNoticeService, times(1)).savePublicNotice(publicNoticeArgumentCaptor.capture());
    var actualPublicNotice = publicNoticeArgumentCaptor.getValue();
    assertThat(actualPublicNotice.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);
  }

  @Test
  void mapUnpublishedPublicNoticeDateToForm_unpublishedPublicNoticeDateExists_dataMappedToForm() {

    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);

    var publicNoticeDate = PublicNoticeTestUtil.createLatestPublicNoticeDate(publicNotice);
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice))
        .thenReturn(Optional.of(publicNoticeDate));

    var form = new FinalisePublicNoticeForm();
    finalisePublicNoticeService.mapUnpublishedPublicNoticeDateToForm(pwaApplication, form);

    var startDate = DateUtils.instantToLocalDate(publicNoticeDate.getPublicationStartTimestamp());
    assertThat(form.getStartDay()).isEqualTo(startDate.getDayOfMonth());
    assertThat(form.getStartMonth()).isEqualTo(startDate.getMonthValue());
    assertThat(form.getStartYear()).isEqualTo(startDate.getYear());
    assertThat(form.getDaysToBePublishedFor()).isEqualTo((int) publicNoticeDate.getPublicationDaysLength());
  }

  @Test
  void mapUnpublishedPublicNoticeDateToForm_unpublishedPublicNoticeDateDoesNotExist() {
    var publicNotice = PublicNoticeTestUtil.createWaitingPublicNotice(pwaApplication);
    when(publicNoticeService.getLatestPublicNotice(pwaApplication)).thenReturn(publicNotice);
    when(publicNoticeDatesRepository.getByPublicNoticeAndEndedByPersonIdIsNull(publicNotice))
          .thenReturn(Optional.empty());
    assertThrows(EntityLatestVersionNotFoundException.class, () ->

      finalisePublicNoticeService.mapUnpublishedPublicNoticeDateToForm(pwaApplication, new FinalisePublicNoticeForm()));
  }


}
