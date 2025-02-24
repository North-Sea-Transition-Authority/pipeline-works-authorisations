package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargePaymentAttempt;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargePaymentAttemptRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargePaymentAttemptTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestDetailRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestItemRepository;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequestRepository;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.PadInitialReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaAwaitPaymentResult;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.pwapay.CreateCardPaymentResultTestUtil;
import uk.co.ogauthority.pwa.features.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.features.pwapay.PwaPaymentRequest;
import uk.co.ogauthority.pwa.features.pwapay.PwaPaymentRequestTestUtil;
import uk.co.ogauthority.pwa.features.pwapay.PwaPaymentService;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationChargeRequestServiceTest {

  private static final String CANCEL_REASON = "CANCEL_REASON";

  @Mock
  private AppChargeEmailService appChargeEmailService;

  @Mock
  private PwaAppChargeRequestRepository pwaAppChargeRequestRepository;

  @Mock
  private PwaAppChargeRequestDetailRepository pwaAppChargeRequestDetailRepository;

  @Mock
  private PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository;

  @Mock
  private PwaAppChargePaymentAttemptRepository pwaAppChargePaymentAttemptRepository;

  @Mock
  private PwaPaymentService pwaPaymentService;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PersonService personService;

  @Mock
  private PadInitialReviewService padInitialReviewService;

  @Mock
  private ApplicationChargeRequestMetadataService applicationChargeRequestMetadataService;

  @Mock
  private AssignCaseOfficerService assignCaseOfficerService;

  @Captor
  private ArgumentCaptor<PwaAppChargeRequestDetail> requestDetailArgumentCaptor;

  @Captor
  private ArgumentCaptor<PwaAppChargePaymentAttempt> paymentAttemptArgumentCaptor;

  @Captor
  private ArgumentCaptor<List<PwaAppChargePaymentAttempt>> activePaymentAttemptArgumentCaptor;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationChargeRequestService applicationChargeRequestService;

  private Person pwaManagerPerson;
  private WebUserAccount pwaManagerWua;

  private Person caseOfficerPerson;

  private Person paymentAttemptPerson;
  private WebUserAccount paymentAttemptWua;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;

  private PwaAppChargeRequestDetail chargeRequestDetail;
  private PwaAppChargeRequest chargeRequest;

  private final Map<String, String> metadataMap = Map.of(
      "Applicant organisation", "SHELL U.K. LIMITED",
      "Project name", "New field development"
  );

  @BeforeEach
  void setUp() throws Exception {

    pwaManagerPerson = PersonTestUtil.createPersonFrom(new PersonId(10));
    pwaManagerWua = new WebUserAccount(10, pwaManagerPerson);
    caseOfficerPerson = PersonTestUtil.createPersonFrom(new PersonId(20));
    paymentAttemptPerson = PersonTestUtil.createPersonFrom(new PersonId(30));
    paymentAttemptWua = new WebUserAccount(31, paymentAttemptPerson);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    when(pwaApplicationDetailService.getTipDetailByApplication(pwaApplication)).thenReturn(pwaApplicationDetail);
    when(pwaApplicationDetailService.getLatestSubmittedDetail(pwaApplication)).thenReturn(Optional.of(pwaApplicationDetail));

    chargeRequestDetail = PwaAppChargeRequestTestUtil.createDefaultChargeRequest(
        pwaApplication, pwaManagerPerson, PwaAppChargeRequestStatus.OPEN);
    chargeRequest = chargeRequestDetail.getPwaAppChargeRequest();

    when(pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(pwaApplication, PwaAppChargeRequestStatus.OPEN))
        .thenReturn(Optional.of(chargeRequestDetail));

    when(assignCaseOfficerService.autoAssignCaseOfficer(any(),any(), any()))
        .thenReturn(WorkflowAssignmentService.AssignTaskResult.SUCCESS);

    applicationChargeRequestService = new ApplicationChargeRequestService(
        appChargeEmailService,
        pwaAppChargeRequestRepository,
        pwaAppChargeRequestDetailRepository,
        pwaAppChargeRequestItemRepository,
        pwaAppChargePaymentAttemptRepository,
        pwaPaymentService,
        pwaApplicationDetailService,
        camundaWorkflowService,
        personService,
        clock,
        padInitialReviewService,
        applicationChargeRequestMetadataService,
        assignCaseOfficerService);

    when(pwaAppChargeRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(pwaAppChargeRequestDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    when(applicationChargeRequestMetadataService.getMetadataMapForDetail(any())).thenReturn(metadataMap);

  }

  @Test
  void createPwaAppChargeRequest_withValidSpec_createsExpectedData() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.OPEN)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25)
        .addChargeItem("CHARGE_2", 75);

    applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec);

    ArgumentCaptor<List<PwaAppChargeRequestItem>> chargeItemCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<PwaAppChargeRequest> chargeRequestCaptor = ArgumentCaptor.forClass(PwaAppChargeRequest.class);
    ArgumentCaptor<PwaAppChargeRequestDetail> chargeRequestDetailCaptor = ArgumentCaptor.forClass(
        PwaAppChargeRequestDetail.class);

    InOrder inOrder = Mockito.inOrder(
        pwaAppChargeRequestRepository,
        pwaAppChargeRequestDetailRepository,
        pwaAppChargeRequestItemRepository);

    inOrder.verify(pwaAppChargeRequestRepository, times(1)).save(chargeRequestCaptor.capture());
    inOrder.verify(pwaAppChargeRequestDetailRepository, times(1)).save(chargeRequestDetailCaptor.capture());
    inOrder.verify(pwaAppChargeRequestItemRepository, times(1)).saveAll(chargeItemCaptor.capture());
    inOrder.verifyNoMoreInteractions();

    assertThat(chargeRequestCaptor.getValue()).satisfies(pwaAppChargeRequest -> {
      assertThat(pwaAppChargeRequest.getPwaApplication()).isEqualTo(pwaApplication);
      assertThat(pwaAppChargeRequest.getRequestedByTimestamp()).isEqualTo(clock.instant());
      assertThat(pwaAppChargeRequest.getRequestedByPersonId()).isEqualTo(pwaManagerPerson.getId());
    });

    assertThat(chargeRequestDetailCaptor.getValue()).satisfies(pwaAppChargeRequestDetail -> {
      assertThat(pwaAppChargeRequestDetail.getPwaAppChargeRequest()).isEqualTo(chargeRequestCaptor.getValue());
      assertThat(pwaAppChargeRequestDetail.getStartedTimestamp()).isEqualTo(clock.instant());
      assertThat(pwaAppChargeRequestDetail.getStartedByPersonId()).isEqualTo(pwaManagerPerson.getId());
      assertThat(pwaAppChargeRequestDetail.getEndedTimestamp()).isNull();
      assertThat(pwaAppChargeRequestDetail.getEndedByPersonId()).isNull();
      assertThat(pwaAppChargeRequestDetail.getTipFlag()).isTrue();
      assertThat(pwaAppChargeRequestDetail.getTotalPennies()).isEqualTo(spec.getTotalPennies());
      assertThat(pwaAppChargeRequestDetail.getChargeSummary()).isEqualTo(spec.getChargeSummary());
      assertThat(pwaAppChargeRequestDetail.getAutoCaseOfficerPersonId()).isEqualTo(
          spec.getOnPaymentCompleteCaseOfficerPersonId());
      assertThat(pwaAppChargeRequestDetail.getPwaAppChargeRequestStatus()).isEqualTo(
          spec.getPwaAppChargeRequestStatus());
      assertThat(pwaAppChargeRequestDetail.getChargeWaivedReason()).isNull();
    });

    assertThat(chargeItemCaptor.getValue())
        .isNotEmpty()
        .anySatisfy(pwaAppChargeRequestItem -> {
          assertThat(pwaAppChargeRequestItem.getPwaAppChargeRequest()).isEqualTo(chargeRequestCaptor.getValue());
          assertThat(pwaAppChargeRequestItem.getPennyAmount()).isEqualTo(25);
          assertThat(pwaAppChargeRequestItem.getDescription()).isEqualTo("CHARGE_1");
        })
        .anySatisfy(pwaAppChargeRequestItem -> {
          assertThat(pwaAppChargeRequestItem.getPwaAppChargeRequest()).isEqualTo(chargeRequestCaptor.getValue());
          assertThat(pwaAppChargeRequestItem.getPennyAmount()).isEqualTo(75);
          assertThat(pwaAppChargeRequestItem.getDescription()).isEqualTo("CHARGE_2");
        });
  }

  @Test
  void createPwaAppChargeRequest_withValidSpec_emailsAppContacts() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.OPEN)
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(100)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25)
        .addChargeItem("CHARGE_2", 75);

    applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec);

    verify(appChargeEmailService, times(1)).sendChargeRequestIssuedEmail(pwaApplication);
  }

  @Test
  void createPwaAppChargeRequest_withValidSpec_andWaivedStatus_setsExpectedData() {

    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
        .setChargeWaivedReason("WAIVED_REASON")
        .setChargeSummary("CHARGE_SUMMARY")
        .setTotalPennies(0)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
        .addChargeItem("CHARGE_1", 25)
        .addChargeItem("CHARGE_2", 75);

    applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec);

    ArgumentCaptor<PwaAppChargeRequest> chargeRequestCaptor = ArgumentCaptor.forClass(PwaAppChargeRequest.class);
    ArgumentCaptor<PwaAppChargeRequestDetail> chargeRequestDetailCaptor = ArgumentCaptor.forClass(
        PwaAppChargeRequestDetail.class);

    verify(pwaAppChargeRequestDetailRepository, times(1)).save(chargeRequestDetailCaptor.capture());

    assertThat(chargeRequestDetailCaptor.getValue()).satisfies(pwaAppChargeRequestDetail -> {

      assertThat(pwaAppChargeRequestDetail.getTotalPennies()).isEqualTo(spec.getTotalPennies());
      assertThat(pwaAppChargeRequestDetail.getPwaAppChargeRequestStatus()).isEqualTo(
          spec.getPwaAppChargeRequestStatus());
      assertThat(pwaAppChargeRequestDetail.getChargeWaivedReason()).isEqualTo(spec.getChargeWaivedReason());
    });

    verify(appChargeEmailService, never()).sendChargeRequestIssuedEmail(pwaApplication);
  }

  @Test
  void createPwaAppChargeRequest_whenNotWaived_andWaivedReasonProvided() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.OPEN)
          .setChargeWaivedReason("WAIVED_REASON")
          .setChargeSummary("CHARGE_SUMMARY")
          .setTotalPennies(100)
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
          .addChargeItem("CHARGE_1", 25);
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void createPwaAppChargeRequest_whenWaived_andWaivedNotReasonProvided() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
          .setChargeSummary("CHARGE_SUMMARY")
          .setTotalPennies(100)
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
          .addChargeItem("CHARGE_1", 25);
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void createPwaAppChargeRequest_whenNoChargeItems() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
          .setChargeSummary("CHARGE_SUMMARY")
          .setTotalPennies(100)
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId());
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void createPwaAppChargeRequest_whenChargeItems_negativePennyAmount() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
          .setChargeSummary("CHARGE_SUMMARY")
          .setTotalPennies(100)
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
          .addChargeItem("CHARGE_1", -1);
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void createPwaAppChargeRequest_whenMissingChargeSummary() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
          .setTotalPennies(100)
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
          .addChargeItem("CHARGE_1", 100);
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void createPwaAppChargeRequest_whenMissingTotalPennies() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
          .setChargeSummary("CHARGE_SUMMARY")
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
          .addChargeItem("CHARGE_1", 100);
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void createPwaAppChargeRequest_whenNegativeTotalPennies() {
    var spec = new ApplicationChargeRequestSpecification(pwaApplication, PwaAppChargeRequestStatus.WAIVED)
          .setChargeSummary("CHARGE_SUMMARY")
          .setTotalPennies(-10)
          .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPerson.getId())
          .addChargeItem("CHARGE_1", 100);
    assertThrows(UnsupportedOperationException.class, () ->

      applicationChargeRequestService.createPwaAppChargeRequest(pwaManagerPerson, spec));

  }

  @Test
  void getOpenRequestAsApplicationChargeRequestReport_whenNoOpenChargeRequestFound() {
    when(pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(any(), any()))
        .thenReturn(Optional.empty());
    assertThat(applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(pwaApplication)).isEmpty();

  }

  @Test
  void getOpenRequestAsApplicationChargeRequestReport_whenOpenChargeRequestFound_andChargeItems() {

    var chargeItem = new PwaAppChargeRequestItem(null, "Item 1", 150);
    when(
        pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
            any(), any()))
        .thenReturn(Optional.of(chargeRequestDetail));
    when(pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequestOrderByDescriptionAsc(any()))
        .thenReturn(List.of(chargeItem));

    var report = applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(pwaApplication)
        .orElseThrow(() -> new RuntimeException("Expected report!"));

    assertThat(report.getPwaAppChargeRequestStatus()).isEqualTo(chargeRequestDetail.getPwaAppChargeRequestStatus());
    assertThat(report.getSummary()).isEqualTo(chargeRequestDetail.getChargeSummary());
    assertThat(report.getTotalPennies()).isEqualTo(chargeRequestDetail.getTotalPennies());
    assertThat(report.getWaivedReason()).isNull();
    assertThat(report.getPaymentItems()).containsExactly(new ApplicationChargeItem(
        chargeItem.getDescription(),
        chargeItem.getPennyAmount()
    ));
  }

  @Test
  void startChargeRequestPaymentAttempt_noTipRequestDetail() {
    when(pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
          pwaApplication, PwaAppChargeRequestStatus.OPEN
      )).thenReturn(Optional.empty());
    assertThrows(ApplicationChargeException.class, () ->

      applicationChargeRequestService.startChargeRequestPaymentAttempt(
          pwaApplication, paymentAttemptWua
      ));
  }

  @Test
  void startChargeRequestPaymentAttempt_noActiveAttempts_paymentServiceSuccess() {

    var newPaymentRequest = PwaPaymentRequestTestUtil.createFrom(UUID.randomUUID(), PaymentRequestStatus.PENDING,
        "someId");
    var createCardPaymentResult = CreateCardPaymentResultTestUtil.createWithUrl(newPaymentRequest);

    when(pwaPaymentService.createCardPayment(any(), any(), any(), any(), any()))
        .thenReturn(createCardPaymentResult);

    var createPaymentAttemptResult = applicationChargeRequestService.startChargeRequestPaymentAttempt(
        pwaApplication, paymentAttemptWua
    );

    verify(pwaPaymentService, times(0)).refreshPwaPaymentRequestData(any());
    verify(pwaAppChargePaymentAttemptRepository, times(0)).saveAll(any());

    verify(pwaPaymentService, times(1)).createCardPayment(
        eq(chargeRequestDetail.getTotalPennies()),
        eq(pwaApplication.getAppReference()),
        eq(chargeRequestDetail.getChargeSummary()),
        eq(metadataMap),
        any()
    );

    verify(pwaAppChargePaymentAttemptRepository, times(1)).save(paymentAttemptArgumentCaptor.capture());

    assertThat(paymentAttemptArgumentCaptor.getValue()).satisfies(paymentAttempt -> {
      assertThat(paymentAttempt.getAssociatedPaymentRequestStatus()).isEqualTo(newPaymentRequest.getRequestStatus());
      assertThat(paymentAttempt.getActiveFlag()).isTrue();
      assertThat(paymentAttempt.getCreatedTimestamp()).isEqualTo(clock.instant());
      assertThat(paymentAttempt.getCreatedByPersonId()).isEqualTo(paymentAttemptPerson.getId());
      assertThat(paymentAttempt.getPwaPaymentRequest()).isEqualTo(newPaymentRequest);
    });

    assertThat(createPaymentAttemptResult.getStartExternalJourneyUrl())
        .isEqualTo(createCardPaymentResult.getStartExternalJourneyUrl().get());
    assertThat(createPaymentAttemptResult.getPaymentAttemptOutcome())
        .isEqualTo(CreatePaymentAttemptResult.AttemptOutcome.PAYMENT_CREATED);
  }

  @Test
  void startChargeRequestPaymentAttempt_noActiveAttempts_paymentServiceFailure() {
    var paymentRequest = PwaPaymentRequestTestUtil
          .createFrom(UUID.randomUUID(), PaymentRequestStatus.FAILED_TO_CREATE, null);
    var createCardPaymentResult = CreateCardPaymentResultTestUtil.createWithoutUrl(paymentRequest);
    when(pwaPaymentService.createCardPayment(any(), any(), any(), any(), any()))
          .thenReturn(createCardPaymentResult);
    assertThrows(ApplicationChargeException.class, () ->

      applicationChargeRequestService.startChargeRequestPaymentAttempt(
          pwaApplication, paymentAttemptWua
      ));

  }

  @Test
  void startChargeRequestPaymentAttempt_inProgressActiveAttempt_notCompleted() {
    /*
     * Ensure existing active attempts get refreshed and cancelled if possible
     */
    var newAttemptPaymentRequest = PwaPaymentRequestTestUtil
        .createFrom(UUID.randomUUID(), PaymentRequestStatus.IN_PROGRESS, "someId");
    var createCardPaymentResult = CreateCardPaymentResultTestUtil.createWithUrl(newAttemptPaymentRequest);
    when(pwaPaymentService.createCardPayment(any(), any(), any(), any(), any()))
        .thenReturn(createCardPaymentResult);

    var activeAttempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(
        chargeRequestDetail.getPwaAppChargeRequest(), PaymentRequestStatus.IN_PROGRESS, paymentAttemptPerson
    );
    when(pwaAppChargePaymentAttemptRepository.findAllByPwaAppChargeRequestAndActiveFlagIsTrue(
        chargeRequestDetail.getPwaAppChargeRequest()))
        .thenReturn(List.of(activeAttempt));

    // mock update of payment request on cancel
    doAnswer(invocation -> {
      var activePaymentRequest = (PwaPaymentRequest) invocation.getArgument(0);
      activePaymentRequest.setRequestStatus(PaymentRequestStatus.CANCELLED);
      return invocation;
    }).when(pwaPaymentService).cancelPayment(any());

   var createPaymentAttemptResult = applicationChargeRequestService.startChargeRequestPaymentAttempt(
        pwaApplication, paymentAttemptWua
    );

    verify(pwaPaymentService, times(1)).cancelPayment(activeAttempt.getPwaPaymentRequest());
    verify(pwaAppChargePaymentAttemptRepository, times(1)).saveAll(activePaymentAttemptArgumentCaptor.capture());

    assertThat(activePaymentAttemptArgumentCaptor.getValue()).hasOnlyOneElementSatisfying(
        pwaAppChargePaymentAttempt -> {
          assertThat(pwaAppChargePaymentAttempt.getActiveFlag()).isFalse();
          assertThat(pwaAppChargePaymentAttempt.getEndedByPersonId()).isEqualTo(paymentAttemptPerson.getId());
        });

    assertThat(createPaymentAttemptResult.getPaymentAttemptOutcome()).isEqualTo(CreatePaymentAttemptResult.AttemptOutcome.PAYMENT_CREATED);
  }

  @Test
  void startChargeRequestPaymentAttempt_inProgressActiveAttempt_completedAndPaid() {
    /*
     * Ensure existing active attempts get refreshed and if paid we dont create a new attempt.
     */
    var activeAttempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(
        chargeRequestDetail.getPwaAppChargeRequest(), PaymentRequestStatus.IN_PROGRESS, paymentAttemptPerson
    );
    when(pwaAppChargePaymentAttemptRepository.findAllByPwaAppChargeRequestAndActiveFlagIsTrue(
        chargeRequestDetail.getPwaAppChargeRequest()))
        .thenReturn(List.of(activeAttempt));

    // mock update of payment request on cancel where payment request has now been paid
    doAnswer(invocation -> {
      var activePaymentRequest = (PwaPaymentRequest) invocation.getArgument(0);
      activePaymentRequest.setRequestStatus(PaymentRequestStatus.PAYMENT_COMPLETE);
      return invocation;
    }).when(pwaPaymentService).cancelPayment(any());

    var createPaymentAttemptResult = applicationChargeRequestService.startChargeRequestPaymentAttempt(
        pwaApplication, paymentAttemptWua
    );

    verify(pwaPaymentService, times(1)).cancelPayment(activeAttempt.getPwaPaymentRequest());
    verify(pwaAppChargePaymentAttemptRepository, times(1)).saveAll(activePaymentAttemptArgumentCaptor.capture());

    assertThat(activePaymentAttemptArgumentCaptor.getValue()).hasOnlyOneElementSatisfying(
        pwaAppChargePaymentAttempt -> {
          assertThat(pwaAppChargePaymentAttempt.getActiveFlag()).isTrue();
          assertThat(pwaAppChargePaymentAttempt.getEndedByPersonId()).isNull();
        });

    assertThat(createPaymentAttemptResult.getPaymentAttemptOutcome()).isEqualTo(CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS);
  }

  @Test
  void startChargeRequestPaymentAttempt_chargeRequestAlreadyPaid() {

    chargeRequestDetail.setPwaAppChargeRequestStatus(PwaAppChargeRequestStatus.PAID);

    var createPaymentAttemptResult = applicationChargeRequestService.startChargeRequestPaymentAttempt(
        pwaApplication, paymentAttemptWua
    );
    assertThat(createPaymentAttemptResult.getPaymentAttemptOutcome()).isEqualTo(CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS);
    assertThat(createPaymentAttemptResult.getStartExternalJourneyUrl()).isNull();

  }

  @Test
  void startChargeRequestPaymentAttempt_chargeRequestWaived() {
    chargeRequestDetail.setPwaAppChargeRequestStatus(PwaAppChargeRequestStatus.WAIVED);
    assertThrows(ApplicationChargeException.class, () ->

      applicationChargeRequestService.startChargeRequestPaymentAttempt(
          pwaApplication, paymentAttemptWua
      ));


  }


  @Test
  void processPaymentAttempt_paymentAttemptStillInProgress() {
    var attempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest, PaymentRequestStatus.PENDING, paymentAttemptPerson);

    var processPaymentAttemptResult = applicationChargeRequestService.processPaymentAttempt(attempt, paymentAttemptWua);

    assertThat(processPaymentAttemptResult).isEqualTo(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_UNCHANGED);
    verify(pwaAppChargeRequestDetailRepository, times(0)).save(any());
    verifyNoInteractions(assignCaseOfficerService);

  }

  @Test
  void processPaymentAttempt_paymentRequestNotOpen_butPaymentAttemptUpdatesAsPaid() {
    chargeRequestDetail.setPwaAppChargeRequestStatus(PwaAppChargeRequestStatus.PAID);

    var attempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest, PaymentRequestStatus.PENDING, paymentAttemptPerson);

    doAnswer(invocation -> {
      var request = (PwaPaymentRequest) invocation.getArgument(0);
      request.setRequestStatus(PaymentRequestStatus.PAYMENT_COMPLETE);
      return invocation;
    }).when(pwaPaymentService).refreshPwaPaymentRequestData(any());

    var processPaymentAttemptResult = applicationChargeRequestService.processPaymentAttempt(attempt, paymentAttemptWua);

    assertThat(processPaymentAttemptResult).isEqualTo(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_UNCHANGED);

    verify(pwaAppChargeRequestDetailRepository, times(0)).save(any());
    verifyNoInteractions(assignCaseOfficerService);

  }

  @Test
  void processPaymentAttempt_paymentRequestOpen_paymentAttemptUpdatesAsPaid() {
    chargeRequestDetail.setPwaAppChargeRequestStatus(PwaAppChargeRequestStatus.OPEN);
    chargeRequestDetail.setAutoCaseOfficerPersonId(caseOfficerPerson.getId());

    when(personService.getPersonById(caseOfficerPerson.getId())).thenReturn(caseOfficerPerson);
    when(padInitialReviewService.getLatestInitialReviewer(pwaApplicationDetail)).thenReturn(Optional.of(pwaManagerPerson));

    var attempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest, PaymentRequestStatus.PENDING, paymentAttemptPerson);

    doAnswer(invocation -> {
      var request = (PwaPaymentRequest) invocation.getArgument(0);
      request.setRequestStatus(PaymentRequestStatus.PAYMENT_COMPLETE);
      return invocation;
    }).when(pwaPaymentService).refreshPwaPaymentRequestData(any());

    var processPaymentAttemptResult = applicationChargeRequestService.processPaymentAttempt(attempt, paymentAttemptWua);

    assertThat(processPaymentAttemptResult).isEqualTo(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_PAID);

    var verifyOrder = Mockito.inOrder(
        pwaApplicationDetailService, camundaWorkflowService, assignCaseOfficerService, pwaAppChargeRequestDetailRepository
    );

    verifyOrder.verify(pwaApplicationDetailService, times(1)).getTipDetailByApplication(pwaApplication);
    verifyOrder.verify(pwaAppChargeRequestDetailRepository, times(2)).save(requestDetailArgumentCaptor.capture());
    verifyOrder.verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        pwaApplication,
        PwaAwaitPaymentResult.PAID
    );
    verifyOrder.verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(
        pwaApplication,
        PwaApplicationWorkflowTask.AWAIT_APPLICATION_PAYMENT
    ));
    verifyOrder.verify(assignCaseOfficerService, times(1))
        .autoAssignCaseOfficer(pwaApplicationDetail, caseOfficerPerson, pwaManagerPerson);

    verifyOrder.verify(pwaApplicationDetailService, times(1)).updateStatus(pwaApplicationDetail, PwaApplicationStatus.CASE_OFFICER_REVIEW, paymentAttemptWua);
    verifyOrder.verifyNoMoreInteractions();

    // verify we end the old request detail and set new one as tip.
    assertThat(requestDetailArgumentCaptor.getAllValues().get(0).getTipFlag()).isFalse();
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getTipFlag()).isTrue();

    verifyNoInteractions(appChargeEmailService);

  }

  @Test
  void processPaymentAttempt_paymentRequestOpen_paymentAttemptUpdatesAsPaid_caseOfficerFailsToAssign() {
    chargeRequestDetail.setPwaAppChargeRequestStatus(PwaAppChargeRequestStatus.OPEN);
    chargeRequestDetail.setAutoCaseOfficerPersonId(caseOfficerPerson.getId());

    when(personService.getPersonById(caseOfficerPerson.getId())).thenReturn(caseOfficerPerson);
    when(padInitialReviewService.getLatestInitialReviewer(pwaApplicationDetail)).thenReturn(Optional.of(pwaManagerPerson));

    when(assignCaseOfficerService.autoAssignCaseOfficer(any(),any(),any()))
        .thenReturn(WorkflowAssignmentService.AssignTaskResult.ASSIGNMENT_CANDIDATE_INVALID);

    var attempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest, PaymentRequestStatus.PENDING, paymentAttemptPerson);

    doAnswer(invocation -> {
      var request = (PwaPaymentRequest) invocation.getArgument(0);
      request.setRequestStatus(PaymentRequestStatus.PAYMENT_COMPLETE);
      return invocation;
    }).when(pwaPaymentService).refreshPwaPaymentRequestData(any());

    applicationChargeRequestService.processPaymentAttempt(attempt, paymentAttemptWua);

    verify(appChargeEmailService, times(1)).sendFailedToAssignCaseOfficerEmail(pwaApplication);

  }

  @Test
  void applicationHasOpenChargeRequest_whenNoOpenRequest() {

    when(pwaAppChargeRequestDetailRepository.countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication, PwaAppChargeRequestStatus.OPEN
    )).thenReturn(0L);

    assertThat(applicationChargeRequestService.applicationHasOpenChargeRequest(pwaApplication)).isFalse();

  }

  @Test
  void applicationHasOpenChargeRequest_whenOpenRequest() {

    when(pwaAppChargeRequestDetailRepository.countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication, PwaAppChargeRequestStatus.OPEN
    )).thenReturn(1L);

    assertThat(applicationChargeRequestService.applicationHasOpenChargeRequest(pwaApplication)).isTrue();

  }

  @Test
  void applicationChargeRequestCompleteAndPaid_whenNoPaidRequest() {

    when(pwaAppChargeRequestDetailRepository.countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication, PwaAppChargeRequestStatus.PAID
    )).thenReturn(0L);

    assertThat(applicationChargeRequestService.applicationChargeRequestCompleteAndPaid(pwaApplication)).isFalse();

  }

  @Test
  void applicationChargeRequestCompleteAndPaid_whenPaidRequest() {

    when(pwaAppChargeRequestDetailRepository.countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication, PwaAppChargeRequestStatus.PAID
    )).thenReturn(1L);

    assertThat(applicationChargeRequestService.applicationChargeRequestCompleteAndPaid(pwaApplication)).isTrue();

  }


  @Test
  void cancelAppPaymentOutcome_whenChargeRequestOpen_emailSent(){

    var cancelOutcome = applicationChargeRequestService.cancelPaymentRequest(
        pwaApplication, pwaManagerWua, CANCEL_REASON);

    assertThat(cancelOutcome).isEqualTo(CancelAppPaymentOutcome.CANCELLED);

    verify(appChargeEmailService, times(1)).sendChargeRequestCancelledEmail(pwaApplication);

  }

  @Test
  void cancelAppPaymentOutcome_whenChargeRequestOpen_workflowUpdated(){

    var cancelOutcome = applicationChargeRequestService.cancelPaymentRequest(
        pwaApplication, pwaManagerWua, CANCEL_REASON);

    assertThat(cancelOutcome).isEqualTo(CancelAppPaymentOutcome.CANCELLED);

    verify(camundaWorkflowService, times(1)).setWorkflowProperty(
        pwaApplication,
        PwaAwaitPaymentResult.CANCELLED
    );
    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(
        pwaApplication,
        PwaApplicationWorkflowTask.AWAIT_APPLICATION_PAYMENT
    ));

  }

  @Test
  void cancelAppPaymentOutcome_whenChargeRequestOpen_chargeRequestStatusUpdated(){

    var cancelOutcome = applicationChargeRequestService.cancelPaymentRequest(
        pwaApplication, pwaManagerWua, CANCEL_REASON);

    assertThat(cancelOutcome).isEqualTo(CancelAppPaymentOutcome.CANCELLED);

    verify(pwaAppChargeRequestDetailRepository, times(2)).save(requestDetailArgumentCaptor.capture());

    // verify we end the old request detail and set new one as tip.
    assertThat(requestDetailArgumentCaptor.getAllValues().get(0).getTipFlag()).isFalse();
    assertThat(requestDetailArgumentCaptor.getAllValues().get(0).getEndedTimestamp()).isEqualTo(clock.instant());
    assertThat(requestDetailArgumentCaptor.getAllValues().get(0).getEndedByPersonId()).isEqualTo(pwaManagerPerson.getId());
    assertThat(requestDetailArgumentCaptor.getAllValues().get(0).getPwaAppChargeRequestStatus()).isEqualTo(PwaAppChargeRequestStatus.OPEN);

    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getTipFlag()).isTrue();
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getChargeCancelledReason()).isEqualTo(CANCEL_REASON);
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getPwaAppChargeRequestStatus()).isEqualTo(PwaAppChargeRequestStatus.CANCELLED);
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getStartedTimestamp()).isEqualTo(clock.instant());
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getStartedByPersonId()).isEqualTo(pwaManagerPerson.getId());
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getEndedTimestamp()).isNull();
    assertThat(requestDetailArgumentCaptor.getAllValues().get(1).getEndedByPersonId()).isNull();

  }

  @Test
  void cancelAppPaymentOutcome_whenChargeRequestOpen_applicationDetailUpdated(){

    var cancelOutcome = applicationChargeRequestService.cancelPaymentRequest(
        pwaApplication, pwaManagerWua, CANCEL_REASON);

    assertThat(cancelOutcome).isEqualTo(CancelAppPaymentOutcome.CANCELLED);

    verify(padInitialReviewService).revokeLatestInitialReview(pwaApplicationDetail, pwaManagerWua);
    verify(pwaApplicationDetailService).updateStatus(pwaApplicationDetail, PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW, pwaManagerWua);
  }

  @Test
  void cancelAppPaymentOutcome_whenChargeRequestOpen_andIncompletePaymentAttempts_attemptsCancelled(){

    var attempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest, PaymentRequestStatus.PENDING, paymentAttemptPerson);
    when(pwaAppChargePaymentAttemptRepository.findAllByPwaAppChargeRequestAndActiveFlagIsTrue(chargeRequest))
        .thenReturn(List.of(attempt));

    doAnswer(invocation -> {
      var request = (PwaPaymentRequest) invocation.getArgument(0);
      request.setRequestStatus(PaymentRequestStatus.IN_PROGRESS);
      return invocation;
    }).when(pwaPaymentService).cancelPayment(
        any());

    var cancelOutcome = applicationChargeRequestService.cancelPaymentRequest(
        pwaApplication, pwaManagerWua, CANCEL_REASON);

    assertThat(cancelOutcome).isEqualTo(CancelAppPaymentOutcome.CANCELLED);

    verify(pwaPaymentService, times(1)).cancelPayment(attempt.getPwaPaymentRequest());
  }

  @Test
  void cancelAppPaymentOutcome_whenChargeRequestOpen_andCompletePaymentAttempt_doNotCancelRequest(){

    var attempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(chargeRequest, PaymentRequestStatus.PENDING, paymentAttemptPerson);
    when(pwaAppChargePaymentAttemptRepository.findAllByPwaAppChargeRequestAndActiveFlagIsTrue(chargeRequest))
        .thenReturn(List.of(attempt));

    doAnswer(invocation -> {
      var request = (PwaPaymentRequest) invocation.getArgument(0);
      request.setRequestStatus(PaymentRequestStatus.PAYMENT_COMPLETE);
      return invocation;
    }).when(pwaPaymentService).cancelPayment(any());

    var cancelOutcome = applicationChargeRequestService.cancelPaymentRequest(
        pwaApplication, pwaManagerWua, CANCEL_REASON);

    assertThat(cancelOutcome).isEqualTo(CancelAppPaymentOutcome.NOT_CANCELLED_ALREADY_PAID);

    verify(pwaPaymentService, times(1)).cancelPayment(attempt.getPwaPaymentRequest());
  }

}
