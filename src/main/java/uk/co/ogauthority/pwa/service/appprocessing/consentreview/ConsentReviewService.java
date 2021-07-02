package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentReviewException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ConsentReviewRepository;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.ConsentReviewDecision;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.ConsentWriterService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@Service
public class ConsentReviewService {

  private final ConsentReviewRepository consentReviewRepository;
  private final Clock clock;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final PwaConsentService pwaConsentService;
  private final ConsentWriterService consentWriterService;
  private final IssueConsentEmailsService issueConsentEmailsService;
  private final DocgenService docgenService;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  public ConsentReviewService(ConsentReviewRepository consentReviewRepository,
                              @Qualifier("utcClock") Clock clock,
                              PwaApplicationDetailService pwaApplicationDetailService,
                              WorkflowAssignmentService workflowAssignmentService,
                              CamundaWorkflowService camundaWorkflowService,
                              PwaConsentService pwaConsentService,
                              ConsentWriterService consentWriterService,
                              IssueConsentEmailsService issueConsentEmailsService,
                              DocgenService docgenService,
                              DocumentInstanceService documentInstanceService) {
    this.consentReviewRepository = consentReviewRepository;
    this.clock = clock;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.pwaConsentService = pwaConsentService;
    this.consentWriterService = consentWriterService;
    this.issueConsentEmailsService = issueConsentEmailsService;
    this.docgenService = docgenService;
    this.documentInstanceService = documentInstanceService;
  }

  @Transactional
  public ConsentReview startConsentReview(PwaApplicationDetail pwaApplicationDetail,
                                          String coverLetterText,
                                          Person startingPerson) {
    if (areThereAnyOpenReviews(pwaApplicationDetail)) {
      throw new RuntimeException(String.format(
          "Can't start a new consent review as there is already an open one for PWA detail with id [%s]", pwaApplicationDetail.getId()));
    }
    var consentReview = new ConsentReview(pwaApplicationDetail, coverLetterText, startingPerson.getId(), clock.instant());
    return consentReviewRepository.save(consentReview);
  }

  public Optional<ConsentReview> getOpenConsentReview(PwaApplicationDetail pwaApplicationDetail) {
    return consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .filter(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()))
        .findFirst();
  }

  @Transactional
  public void returnToCaseOfficer(PwaApplicationDetail pwaApplicationDetail,
                                  String returnReason,
                                  Person caseOfficerPerson,
                                  WebUserAccount returningUser) {


    // check there's an open review, error if not
    var openReview = consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .filter(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()))
        .findFirst()
        .orElseThrow(() -> new ConsentReviewException(String.format(
            "Can't return to case officer as there is no open consent review for PWA detail with id [%s]", pwaApplicationDetail.getId())));

    // end review, store details
    openReview.setStatus(ConsentReviewStatus.RETURNED);
    openReview.setEndTimestamp(clock.instant());
    openReview.setEndedByPersonId(returningUser.getLinkedPerson().getId());
    openReview.setEndedReason(returnReason);
    consentReviewRepository.save(openReview);

    // update app status
    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.CASE_OFFICER_REVIEW, returningUser);

    // transition workflow back to CO
    completeWorkflowTaskWithDecision(pwaApplicationDetail, ConsentReviewDecision.RETURN);

    workflowAssignmentService.assign(
        pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficerPerson,
        returningUser.getLinkedPerson());

    // email CO
    issueConsentEmailsService.sendConsentReviewReturnedEmail(
        pwaApplicationDetail, caseOfficerPerson, returningUser.getLinkedPerson().getFullName(), returnReason);

  }

  private void completeWorkflowTaskWithDecision(PwaApplicationDetail pwaApplicationDetail, ConsentReviewDecision decision) {

    camundaWorkflowService.setWorkflowProperty(pwaApplicationDetail.getPwaApplication(), decision);

    var workflowTaskInstance = new WorkflowTaskInstance(pwaApplicationDetail.getPwaApplication(),
        PwaApplicationWorkflowTask.CONSENT_REVIEW);

    camundaWorkflowService.completeTask(workflowTaskInstance);

  }

  public List<ConsentReview> findByPwaApplicationDetails(Collection<PwaApplicationDetail> details) {
    return consentReviewRepository.findAllByPwaApplicationDetailIn(details);
  }

  @Transactional
  public IssuedConsentDto issueConsent(PwaApplicationDetail pwaApplicationDetail, WebUserAccount issuingUser) {

    // get open review if exists, error if it doesn't
    var openReview = consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .filter(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()))
        .findFirst()
        .orElseThrow(() -> new ConsentReviewException(String.format(
            "Can't issue consent as there is no open consent review for PWA detail with id [%s]", pwaApplicationDetail.getId())));

    var consent = pwaConsentService.createConsent(pwaApplicationDetail.getPwaApplication());
    consentWriterService.updateConsentedData(pwaApplicationDetail, consent);

    // end review
    openReview.setEndTimestamp(clock.instant());
    openReview.setStatus(ConsentReviewStatus.APPROVED);
    openReview.setEndedByPersonId(issuingUser.getLinkedPerson().getId());
    var approvedReview = consentReviewRepository.save(openReview);

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.COMPLETE, issuingUser);
    completeWorkflowTaskWithDecision(pwaApplicationDetail, ConsentReviewDecision.APPROVE);

    issueConsentEmailsService.sendConsentIssuedEmails(
        pwaApplicationDetail, approvedReview.getCoverLetterText(), issuingUser.getFullName());

    workflowAssignmentService.clearAssignments(pwaApplicationDetail.getPwaApplication());

    var docInstance = documentInstanceService
        .getDocumentInstanceOrError(pwaApplicationDetail.getPwaApplication(), DocumentTemplateMnem.PWA_CONSENT_DOCUMENT);

    var docgenRun = docgenService.scheduleDocumentGeneration(docInstance, DocGenType.FULL, issuingUser.getLinkedPerson());
    pwaConsentService.setDocgenRunId(consent, docgenRun);

    return new IssuedConsentDto(consent.getReference());

  }

  public boolean areThereAnyOpenReviews(PwaApplicationDetail pwaApplicationDetail) {
    return consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .anyMatch(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()));
  }

  public boolean isApplicationConsented(PwaApplicationDetail pwaApplicationDetail) {
    return consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .anyMatch(review -> ConsentReviewStatus.APPROVED.equals(review.getStatus()));
  }

}
