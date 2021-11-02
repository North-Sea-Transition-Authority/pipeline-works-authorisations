package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus.WAIVED;
import static uk.co.ogauthority.pwa.pwapay.PaymentRequestStatus.PAYMENT_COMPLETE;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.controller.appprocessing.processingcharges.IndustryPaymentCallbackController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargePaymentAttempt;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestItem;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.pwapay.PwaPaymentRequest;
import uk.co.ogauthority.pwa.pwapay.PwaPaymentService;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargePaymentAttemptRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestDetailRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestItemRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.processingcharges.PwaAppChargeRequestRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaAwaitPaymentResult;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PadInitialReviewService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * Creates and reports on charges demanded for applications.
 * Whats the difference between charges and fees? "They have been charged the application fee" e.g
 * -> Fee is what an item will cost
 * -> charge is when that fee has been actually demanded from a person/org.
 */
@Service
public class ApplicationChargeRequestService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationChargeRequestService.class);

  private final AppChargeEmailService appChargeEmailService;
  private final PwaAppChargeRequestRepository pwaAppChargeRequestRepository;
  private final PwaAppChargeRequestDetailRepository pwaAppChargeRequestDetailRepository;
  private final PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository;
  private final PwaAppChargePaymentAttemptRepository pwaAppChargePaymentAttemptRepository;
  private final PwaPaymentService pwaPaymentService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PersonService personService;
  private final Clock clock;
  private final PadInitialReviewService padInitialReviewService;

  @Autowired
  public ApplicationChargeRequestService(AppChargeEmailService appChargeEmailService,
                                         PwaAppChargeRequestRepository pwaAppChargeRequestRepository,
                                         PwaAppChargeRequestDetailRepository pwaAppChargeRequestDetailRepository,
                                         PwaAppChargeRequestItemRepository pwaAppChargeRequestItemRepository,
                                         PwaAppChargePaymentAttemptRepository pwaAppChargePaymentAttemptRepository,
                                         PwaPaymentService pwaPaymentService,
                                         PwaApplicationDetailService pwaApplicationDetailService,
                                         WorkflowAssignmentService workflowAssignmentService,
                                         CamundaWorkflowService camundaWorkflowService,
                                         PersonService personService,
                                         @Qualifier("utcClock") Clock clock,
                                         PadInitialReviewService padInitialReviewService) {
    this.appChargeEmailService = appChargeEmailService;
    this.pwaAppChargeRequestRepository = pwaAppChargeRequestRepository;
    this.pwaAppChargeRequestDetailRepository = pwaAppChargeRequestDetailRepository;
    this.pwaAppChargeRequestItemRepository = pwaAppChargeRequestItemRepository;
    this.pwaAppChargePaymentAttemptRepository = pwaAppChargePaymentAttemptRepository;
    this.pwaPaymentService = pwaPaymentService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.personService = personService;
    this.clock = clock;
    this.padInitialReviewService = padInitialReviewService;
  }

  @Transactional
  public void createPwaAppChargeRequest(Person requesterPerson,
                                        ApplicationChargeRequestSpecification applicationChargeRequestSpecification) {
    validateAppChargeSpec(applicationChargeRequestSpecification);

    var chargeRequest = createAndSavePwaAppChargeRequestFromSpec(requesterPerson,
        applicationChargeRequestSpecification);

    createAndSaveTipChargeRequestDetailFromSpec(chargeRequest, applicationChargeRequestSpecification);

    var chargeItems = applicationChargeRequestSpecification.getApplicationChargeItems().stream()
        .map(applicationChargeItem -> new PwaAppChargeRequestItem(
            chargeRequest,
            applicationChargeItem.getDescription(),
            applicationChargeItem.getPennyAmount()
        ))
        .collect(toList());
    pwaAppChargeRequestItemRepository.saveAll(chargeItems);

    if (applicationChargeRequestSpecification.getPwaAppChargeRequestStatus() != WAIVED) {
      appChargeEmailService.sendChargeRequestIssuedEmail(applicationChargeRequestSpecification.getPwaApplication());
    }
  }

  private void createAndSaveTipChargeRequestDetailFromSpec(PwaAppChargeRequest pwaAppChargeRequest,
                                                           ApplicationChargeRequestSpecification applicationChargeRequestSpecification) {
    var detail = new PwaAppChargeRequestDetail(pwaAppChargeRequest);
    detail.setTipFlag(true);
    detail.setChargeSummary(applicationChargeRequestSpecification.getChargeSummary());
    detail.setTotalPennies(applicationChargeRequestSpecification.getTotalPennies());
    detail.setPwaAppChargeRequestStatus(applicationChargeRequestSpecification.getPwaAppChargeRequestStatus());
    detail.setChargeWaivedReason(applicationChargeRequestSpecification.getChargeWaivedReason());
    detail.setAutoCaseOfficerPersonId(applicationChargeRequestSpecification.getOnPaymentCompleteCaseOfficerPersonId());

    pwaAppChargeRequestDetailRepository.save(detail);
  }

  private PwaAppChargeRequest createAndSavePwaAppChargeRequestFromSpec(Person requesterPerson,
                                                                       ApplicationChargeRequestSpecification chargeRequestSpecification) {
    var chargeRequest = new PwaAppChargeRequest();
    chargeRequest.setPwaApplication(chargeRequestSpecification.getPwaApplication());
    chargeRequest.setRequestedByPersonId(requesterPerson.getId());
    chargeRequest.setRequestedByTimestamp(clock.instant());
    return pwaAppChargeRequestRepository.save(chargeRequest);
  }

  private List<PwaAppChargePaymentAttempt> getActivePaymentAttemptsForChargeRequest(
      PwaAppChargeRequest pwaAppChargeRequest) {
    return pwaAppChargePaymentAttemptRepository.findAllByPwaAppChargeRequestAndActiveFlagIsTrue(pwaAppChargeRequest);

  }

  private void validateAppChargeSpec(ApplicationChargeRequestSpecification chargeRequestSpecification) {

    if (chargeRequestSpecification.getTotalPennies() == null || chargeRequestSpecification.getTotalPennies() < 0) {
      throw new UnsupportedOperationException("Cannot create charge request for value less than 0");
    }

    if (chargeRequestSpecification.getApplicationChargeItems()
        .stream()
        .anyMatch(applicationChargeItem -> applicationChargeItem.getPennyAmount() < 0)) {
      throw new UnsupportedOperationException("Cannot create charge request negative penny charge item");
    }

    if (StringUtils.isBlank(chargeRequestSpecification.getChargeSummary())) {
      throw new UnsupportedOperationException("Cannot create charge request with blank summary");
    }


    if (chargeRequestSpecification.getApplicationChargeItems().isEmpty()) {
      throw new UnsupportedOperationException("Cannot create charge request empty charge items");
    }

    if (WAIVED.equals(chargeRequestSpecification.getPwaAppChargeRequestStatus())
        && StringUtils.isBlank(chargeRequestSpecification.getChargeWaivedReason())) {
      throw new UnsupportedOperationException("Cannot create WAIVED charge request with no reason provided");
    }

    if (!WAIVED.equals(chargeRequestSpecification.getPwaAppChargeRequestStatus())
        && !StringUtils.isBlank(chargeRequestSpecification.getChargeWaivedReason())) {
      throw new UnsupportedOperationException("Cannot create non-waived charge request with waived reason provided");
    }

  }

  public Optional<ApplicationChargeRequestReport> getOpenRequestAsApplicationChargeRequestReport(PwaApplication pwaApplication) {
    return pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication,
        PwaAppChargeRequestStatus.OPEN
    ).map(this::convertRequestDetailToReport);
  }

  public Optional<ApplicationChargeRequestReport> getLatestRequestAsApplicationChargeRequestReport(PwaApplication pwaApplication) {
    // if multiple charge requests this is doing more work than necessary. unlikely to be more than 2 so perf ignored.
    return getAllApplicationChargeRequestReportsForApplication(pwaApplication)
        .stream()
        .max(Comparator.comparing(ApplicationChargeRequestReport::getRequestedInstant));
  }

  private PwaAppChargeRequestDetail getTipOpenRequestDetailForApplication(PwaApplication pwaApplication) {
    return pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication, PwaAppChargeRequestStatus.OPEN
    ).orElseThrow(() -> new ApplicationChargeException(
        "Expected to find OPEN tip charge request detail for app_id:" + pwaApplication.getId()));
  }

  private ApplicationChargeRequestReport convertRequestDetailToReport(PwaAppChargeRequestDetail pwaAppChargeRequestDetail) {
    var chargeItems = pwaAppChargeRequestItemRepository.findAllByPwaAppChargeRequestOrderByDescriptionAsc(
        pwaAppChargeRequestDetail.getPwaAppChargeRequest())
        .stream()
        .map(ApplicationChargeItem::from)
        .collect(Collectors.toUnmodifiableList());

    var successfullyPaidPaymentAttempt = getSuccessfullyPaidPaymentAttempt(
        pwaAppChargeRequestDetail.getPwaAppChargeRequest()
    );

    return new ApplicationChargeRequestReport(
        pwaAppChargeRequestDetail.getPwaAppChargeRequest().getRequestedByTimestamp(),
        pwaAppChargeRequestDetail.getPwaAppChargeRequest().getRequestedByPersonId(),
        pwaAppChargeRequestDetail.getStartedTimestamp(),
        pwaAppChargeRequestDetail.getStartedByPersonId(),
        successfullyPaidPaymentAttempt.map(o -> o.getPwaPaymentRequest().getRequestStatusTimestamp()).orElse(null),
        successfullyPaidPaymentAttempt.map(PwaAppChargePaymentAttempt::getCreatedByPersonId).orElse(null),
        pwaAppChargeRequestDetail.getTotalPennies(),
        pwaAppChargeRequestDetail.getChargeSummary(),
        chargeItems,
        pwaAppChargeRequestDetail.getPwaAppChargeRequestStatus(),
        pwaAppChargeRequestDetail.getChargeWaivedReason(),
        pwaAppChargeRequestDetail.getChargeCancelledReason()
    );
  }

  public List<ApplicationChargeRequestReport> getAllApplicationChargeRequestReportsForApplication(PwaApplication pwaApplication) {
    return pwaAppChargeRequestDetailRepository.findByPwaAppChargeRequest_PwaApplicationAndTipFlagIsTrue(pwaApplication)
        .stream()
        .map(this::convertRequestDetailToReport)
        .collect(Collectors.toList());
  }

  private Optional<PwaAppChargePaymentAttempt> getSuccessfullyPaidPaymentAttempt(PwaAppChargeRequest pwaAppChargeRequest) {
    var allPaidAttempts =  pwaAppChargePaymentAttemptRepository
        .findAllByPwaAppChargeRequestAndPwaPaymentRequest_RequestStatus(pwaAppChargeRequest, PAYMENT_COMPLETE);

    // if in the unlikely event there is some bug which means their are multiple complete payment requests,
    // log, then return the attempt which was paid first
    if (allPaidAttempts.size() > 1) {
      LOGGER.error("Detected multiple paid payment attempts for payment request! requestId:{}", pwaAppChargeRequest.getId());
    }

    return allPaidAttempts.stream()
        .min(Comparator.comparing(
            pwaAppChargePaymentAttempt -> pwaAppChargePaymentAttempt.getPwaPaymentRequest().getRequestStatusTimestamp())
        );

  }

  @Transactional
  public CreatePaymentAttemptResult startChargeRequestPaymentAttempt(PwaApplication pwaApplication,
                                                                     WebUserAccount webUserAccount) {

    var tipOpenRequestDetail = getTipOpenRequestDetailForApplication(pwaApplication);

    if (PwaAppChargeRequestStatus.OPEN.equals(tipOpenRequestDetail.getPwaAppChargeRequestStatus())) {

      var cancelActivePaymentAttemptOutcome = cancelActivePaymentAttempts(
          tipOpenRequestDetail.getPwaAppChargeRequest(),
          webUserAccount.getLinkedPerson()
      );

      if (cancelActivePaymentAttemptOutcome.equals(CancelActivePaymentAttemptsOutcome.SOME_ATTEMPT_ALREADY_PAID)) {
        return new CreatePaymentAttemptResult(null, CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS);
      }

      return createCardPaymentOrError(pwaApplication, tipOpenRequestDetail, webUserAccount.getLinkedPerson());

    } else if (PwaAppChargeRequestStatus.PAID.equals(tipOpenRequestDetail.getPwaAppChargeRequestStatus())) {
      // if we know payment request has already been paid, just return with appropriate result.
      return new CreatePaymentAttemptResult(null, CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS);
    }

    throw new ApplicationChargeException(
        String.format(
            "Tried to start a payment on app_id:%s where the current charge request status is not supported. request status:%s",
            pwaApplication.getId(),
            tipOpenRequestDetail.getPwaAppChargeRequestStatus()
        )
    );

  }

  private CancelActivePaymentAttemptsOutcome cancelActivePaymentAttempts(PwaAppChargeRequest pwaAppChargeRequest,
                                                                         Person person) {

    var activePaymentAttempts = getActivePaymentAttemptsForChargeRequest(pwaAppChargeRequest);
    var somePaymentAttemptCompletedSuccessfully = false;
    for (PwaAppChargePaymentAttempt activePaymentAttempt : activePaymentAttempts) {
      pwaPaymentService.refreshPwaPaymentRequestData(activePaymentAttempt.getPwaPaymentRequest());
      pwaPaymentService.cancelPayment(activePaymentAttempt.getPwaPaymentRequest());

      // Cancelling a request does a refresh of payment request data, so we can rely on this being up to date
      var activeAttemptPaymentRequestStatus = activePaymentAttempt.getPwaPaymentRequest().getRequestStatus();

      // make sure we only set active flag to false when payment journey finished without completed payment.
      updatePaymentAttemptFromRequest(activePaymentAttempt, person.getId());

      somePaymentAttemptCompletedSuccessfully = somePaymentAttemptCompletedSuccessfully
          || PAYMENT_COMPLETE.equals(activeAttemptPaymentRequestStatus);

    }

    if (!activePaymentAttempts.isEmpty()) {
      pwaAppChargePaymentAttemptRepository.saveAll(activePaymentAttempts);
    }

    return somePaymentAttemptCompletedSuccessfully
        ? CancelActivePaymentAttemptsOutcome.SOME_ATTEMPT_ALREADY_PAID
        : CancelActivePaymentAttemptsOutcome.NO_ATTEMPT_ALREADY_PAID;
  }

  private CreatePaymentAttemptResult createCardPaymentOrError(PwaApplication pwaApplication,
                                                              PwaAppChargeRequestDetail pwaAppChargeRequestDetail,
                                                              Person paymentPerson) {
    var createCardPaymentResult = pwaPaymentService.createCardPayment(
        pwaAppChargeRequestDetail.getTotalPennies(),
        pwaApplication.getAppReference(),
        pwaAppChargeRequestDetail.getChargeSummary(),
        uuid -> ReverseRouter.route(
            on(IndustryPaymentCallbackController.class).reconcilePaymentRequestAndRedirect(uuid, null, null))
    );
    var startExternalJourneyUrl = createCardPaymentResult.getStartExternalJourneyUrl()
        .orElseThrow(() -> new ApplicationChargeException(
            "Could not find expected external URL. payment request uuid:" + createCardPaymentResult.getPwaPaymentRequest().getUuid())
        );

    associatePaymentRequestWithPaymentAttempt(
        createCardPaymentResult.getPwaPaymentRequest(),
        pwaAppChargeRequestDetail.getPwaAppChargeRequest(),
        paymentPerson
    );

    return new CreatePaymentAttemptResult(
        startExternalJourneyUrl,
        CreatePaymentAttemptResult.AttemptOutcome.PAYMENT_CREATED
    );
  }

  @Transactional
  public PwaAppChargePaymentAttempt reconcilePaymentRequestCallbackUuidToPaymentAttempt(UUID uuid) {
    var paymentRequest = pwaPaymentService.getGovUkPaymentRequestOrError(uuid);
    return pwaAppChargePaymentAttemptRepository.findByPwaPaymentRequest(paymentRequest)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Cannot find app charge payment attempt link to payment request with uuid:" + uuid)
        );
  }

  private void associatePaymentRequestWithPaymentAttempt(PwaPaymentRequest paymentRequest,
                                                         PwaAppChargeRequest pwaAppChargeRequest,
                                                         Person person) {
    var attempt = new PwaAppChargePaymentAttempt(
        pwaAppChargeRequest,
        person.getId(),
        clock.instant(),
        true,
        paymentRequest
    );

    pwaAppChargePaymentAttemptRepository.save(attempt);

  }


  private void updatePaymentAttemptFromRequest(PwaAppChargePaymentAttempt pwaAppChargePaymentAttempt,
                                               PersonId endedByPersonId) {
    pwaPaymentService.refreshPwaPaymentRequestData(pwaAppChargePaymentAttempt.getPwaPaymentRequest());
    // set as not active if finished without completed payment.
    if (PaymentRequestStatus.JourneyState.FINISHED.equals(
        pwaAppChargePaymentAttempt.getPwaPaymentRequest().getRequestStatus().getJourneyState())
        && !PAYMENT_COMPLETE.equals(pwaAppChargePaymentAttempt.getPwaPaymentRequest().getRequestStatus())) {
      pwaAppChargePaymentAttempt.setActiveFlag(false);
      pwaAppChargePaymentAttempt.setEndedByPersonId(endedByPersonId);
      pwaAppChargePaymentAttempt.setEndedTimestamp(clock.instant());
    }

    pwaAppChargePaymentAttemptRepository.save(pwaAppChargePaymentAttempt);

  }

  private void setChargeRequestPaid(PwaAppChargeRequestDetail pwaAppChargeRequestDetail, Person person) {
    if (!pwaAppChargeRequestDetail.getTipFlag()) {
      throw new ApplicationChargeException("Expected tip detail to be provided");
    }

    pwaAppChargeRequestDetail = endAppChargeRequestDetail(pwaAppChargeRequestDetail, person);

    createNewTipChargeRequestDetailFrom(pwaAppChargeRequestDetail, PwaAppChargeRequestStatus.PAID, person, null, null);
  }

  private PwaAppChargeRequestDetail endAppChargeRequestDetail(PwaAppChargeRequestDetail pwaAppChargeRequestDetail,
                                                              Person person) {
    pwaAppChargeRequestDetail.setTipFlag(false);
    pwaAppChargeRequestDetail.setEndedByPersonId(person.getId());
    pwaAppChargeRequestDetail.setEndedTimestamp(clock.instant());
    return pwaAppChargeRequestDetailRepository.save(pwaAppChargeRequestDetail);
  }

  private void createNewTipChargeRequestDetailFrom(PwaAppChargeRequestDetail pwaAppChargeRequestDetail,
                                                   PwaAppChargeRequestStatus pwaAppChargeRequestStatus,
                                                   Person person,
                                                   String waivedReason,
                                                   String cancelledReason
  ) {
    var newTipDetail = new PwaAppChargeRequestDetail();
    newTipDetail.setPwaAppChargeRequest(pwaAppChargeRequestDetail.getPwaAppChargeRequest());
    newTipDetail.setStartedTimestamp(clock.instant());
    newTipDetail.setStartedByPersonId(person.getId());
    newTipDetail.setTipFlag(true);

    newTipDetail.setPwaAppChargeRequestStatus(pwaAppChargeRequestStatus);

    newTipDetail.setChargeSummary(pwaAppChargeRequestDetail.getChargeSummary());
    newTipDetail.setTotalPennies(pwaAppChargeRequestDetail.getTotalPennies());
    newTipDetail.setChargeWaivedReason(waivedReason);
    newTipDetail.setChargeCancelledReason(cancelledReason);
    newTipDetail.setAutoCaseOfficerPersonId(pwaAppChargeRequestDetail.getAutoCaseOfficerPersonId());

    pwaAppChargeRequestDetailRepository.save(newTipDetail);
  }

  @Transactional
  public ProcessPaymentAttemptOutcome processPaymentAttempt(PwaAppChargePaymentAttempt pwaAppChargePaymentAttempt,
                                                            WebUserAccount webUserAccount) {

    var tipChargeRequestDetail = getTipOpenRequestDetailForApplication(
        pwaAppChargePaymentAttempt.getPwaAppChargeRequest().getPwaApplication());

    var tipAppDetail = pwaApplicationDetailService.getTipDetail(
        pwaAppChargePaymentAttempt.getPwaAppChargeRequest().getPwaApplication());

    updatePaymentAttemptFromRequest(pwaAppChargePaymentAttempt, webUserAccount.getLinkedPerson().getId());

    var latestPaymentRequestStatus = pwaAppChargePaymentAttempt.getAssociatedPaymentRequestStatus();

    if (latestPaymentRequestStatus.equals(PAYMENT_COMPLETE)
        && !tipChargeRequestDetail.getPwaAppChargeRequestStatus().equals(PwaAppChargeRequestStatus.OPEN)) {
      LOGGER.error(
          "Attempted to handle PAYMENT_COMPLETE charge request attempt (id:{}) for charge request which is not not OPEN but is {}",
          pwaAppChargePaymentAttempt.getId(),
          tipChargeRequestDetail.getPwaAppChargeRequestStatus()
      );

      return ProcessPaymentAttemptOutcome.CHARGE_REQUEST_UNCHANGED;
    }

    if (!latestPaymentRequestStatus.equals(PAYMENT_COMPLETE)) {
      return ProcessPaymentAttemptOutcome.CHARGE_REQUEST_UNCHANGED;
    }

    // Continue processing successful paid attempt not error cases extracted out.
    setChargeRequestPaid(tipChargeRequestDetail, webUserAccount.getLinkedPerson());

    completeAwaitingPaymentTaskWithResult(
        tipChargeRequestDetail.getPwaAppChargeRequest().getPwaApplication(),
        PwaAwaitPaymentResult.PAID
    );

    var assignmentResult = workflowAssignmentService.assignTaskNoException(
        pwaAppChargePaymentAttempt.getPwaAppChargeRequest().getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        personService.getPersonById(tipChargeRequestDetail.getAutoCaseOfficerPersonId()),
        personService.getPersonById(tipChargeRequestDetail.getStartedByPersonId())
    );

    if (!assignmentResult.equals(WorkflowAssignmentService.AssignTaskResult.SUCCESS)) {
      LOGGER.info("Error on auto case officer assign. Failed  on personId:{} as case officer for appId:{}",
          tipChargeRequestDetail.getAutoCaseOfficerPersonId(),
          pwaAppChargePaymentAttempt.getPwaAppChargeRequest().getPwaApplication().getId()
      );

      appChargeEmailService.sendFailedToAssignCaseOfficerEmail(pwaAppChargePaymentAttempt.getPwaAppChargeRequest().getPwaApplication());
    }

    pwaApplicationDetailService.updateStatus(
        tipAppDetail,
        PwaApplicationStatus.CASE_OFFICER_REVIEW,
        webUserAccount
    );

    return ProcessPaymentAttemptOutcome.CHARGE_REQUEST_PAID;
  }

  private void completeAwaitingPaymentTaskWithResult(PwaApplication pwaApplication,
                                                     PwaAwaitPaymentResult pwaAwaitPaymentResult) {
    camundaWorkflowService.setWorkflowProperty(pwaApplication,
        pwaAwaitPaymentResult
    );

    camundaWorkflowService.completeTask(
        new WorkflowTaskInstance(
            pwaApplication,
            PwaApplicationWorkflowTask.AWAIT_APPLICATION_PAYMENT
        )
    );

  }

  @Transactional
  public CancelAppPaymentOutcome cancelPaymentRequest(PwaApplication pwaApplication,
                                                      WebUserAccount webUserAccount,
                                                      String cancellationReason) {
    var paymentRequestTipDetail = getTipOpenRequestDetailForApplication(pwaApplication);
    // return not cancelled value if detected invalid payment request status
    if (!paymentRequestTipDetail.getPwaAppChargeRequestStatus().equals(PwaAppChargeRequestStatus.OPEN)) {
      if (paymentRequestTipDetail.getPwaAppChargeRequestStatus().equals(PwaAppChargeRequestStatus.PAID)) {
        return CancelAppPaymentOutcome.NOT_CANCELLED_ALREADY_PAID;
      } else {
        throw new ApplicationChargeException(String.format(
            "Cannot cancel payment request with status:%s for paymentRequestDetailId:%s by wuaId:%s",
            paymentRequestTipDetail.getPwaAppChargeRequestStatus(),
            paymentRequestTipDetail.getId(),
            webUserAccount.getWuaId()
        ));
      }
    }

    var cancelOutcome = cancelActivePaymentAttempts(paymentRequestTipDetail.getPwaAppChargeRequest(), webUserAccount.getLinkedPerson());
    if (cancelOutcome.equals(CancelActivePaymentAttemptsOutcome.SOME_ATTEMPT_ALREADY_PAID)) {
      // do not do any additional clean up of payment attempt and charge request here so we do not
      // invalidate the user journey who has just completed the payment.
      // In the edge case where the user who paid does not return to the service immediately and progress the application,
      // then we just wait for the cleanup job to cleanup and progress the workflow.
      return CancelAppPaymentOutcome.NOT_CANCELLED_ALREADY_PAID;
    }

    paymentRequestTipDetail = endAppChargeRequestDetail(paymentRequestTipDetail, webUserAccount.getLinkedPerson());

    createNewTipChargeRequestDetailFrom(
        paymentRequestTipDetail,
        PwaAppChargeRequestStatus.CANCELLED,
        webUserAccount.getLinkedPerson(),
        null,
        cancellationReason
    );

    completeAwaitingPaymentTaskWithResult(
        paymentRequestTipDetail.getPwaAppChargeRequest().getPwaApplication(),
        PwaAwaitPaymentResult.CANCELLED
    );

    var pwaApplicationDetail = pwaApplicationDetailService.getTipDetail(
        paymentRequestTipDetail.getPwaAppChargeRequest().getPwaApplication()
    );


    padInitialReviewService.revokeLatestInitialReview(pwaApplicationDetail, webUserAccount);
    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW, webUserAccount);

    appChargeEmailService.sendChargeRequestCancelledEmail(pwaApplicationDetail.getPwaApplication());
    return CancelAppPaymentOutcome.CANCELLED;
  }

  public boolean applicationHasOpenChargeRequest(PwaApplication pwaApplication) {
    return pwaAppChargeRequestDetailRepository.countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication,
        PwaAppChargeRequestStatus.OPEN
    ) > 0L;
  }

  public boolean applicationChargeRequestCompleteAndPaid(PwaApplication pwaApplication) {
    return pwaAppChargeRequestDetailRepository.countByPwaAppChargeRequest_PwaApplicationAndPwaAppChargeRequestStatusAndTipFlagIsTrue(
        pwaApplication,
        PwaAppChargeRequestStatus.PAID
    ) > 0L;
  }

  private enum CancelActivePaymentAttemptsOutcome {
    SOME_ATTEMPT_ALREADY_PAID,
    NO_ATTEMPT_ALREADY_PAID
  }

  public List<PwaAppChargePaymentAttempt> getActiveAttemptsWhereStatusIsAndStartedBefore(PaymentRequestStatus paymentRequestStatus,
                                                                                         Instant attemptsCreatedBeforeInstant) {
    return pwaAppChargePaymentAttemptRepository.findAllByActiveFlagIsTrueAndPwaPaymentRequest_RequestStatusAndCreatedTimestampIsBefore(
        paymentRequestStatus,
        attemptsCreatedBeforeInstant
    );
  }


}

