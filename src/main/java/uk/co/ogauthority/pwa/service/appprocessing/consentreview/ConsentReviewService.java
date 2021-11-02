package uk.co.ogauthority.pwa.service.appprocessing.consentreview;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentReviewException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;
import uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent.ConsentReviewRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consentissue.ConsentIssueSchedulerBean;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.ConsentReviewDecision;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventService;
import uk.co.ogauthority.pwa.service.pwaapplications.events.PwaApplicationEventType;
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
  private final IssueConsentEmailsService issueConsentEmailsService;
  private final Scheduler scheduler;
  private final PwaApplicationEventService pwaApplicationEventService;

  @Autowired
  public ConsentReviewService(ConsentReviewRepository consentReviewRepository,
                              @Qualifier("utcClock") Clock clock,
                              PwaApplicationDetailService pwaApplicationDetailService,
                              WorkflowAssignmentService workflowAssignmentService,
                              CamundaWorkflowService camundaWorkflowService,
                              IssueConsentEmailsService issueConsentEmailsService,
                              Scheduler scheduler,
                              PwaApplicationEventService pwaApplicationEventService) {
    this.consentReviewRepository = consentReviewRepository;
    this.clock = clock;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.issueConsentEmailsService = issueConsentEmailsService;
    this.scheduler = scheduler;
    this.pwaApplicationEventService = pwaApplicationEventService;
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
  public void scheduleConsentIssue(PwaApplicationDetail pwaApplicationDetail, WebUserAccount issuingUser) {

    if (getOpenConsentReview(pwaApplicationDetail).isEmpty()) {
      throw new ConsentReviewException(
          String.format("No open consent review for PAD with id %s", pwaApplicationDetail.getId()));
    }

    // clear any previous consent issue failure events, we'll get a new one if we fail this time
    pwaApplicationEventService.clearEvents(pwaApplicationDetail.getPwaApplication(), PwaApplicationEventType.CONSENT_ISSUE_FAILED);

    pwaApplicationDetailService.updateStatus(pwaApplicationDetail, PwaApplicationStatus.ISSUING_CONSENT, issuingUser);
    completeWorkflowTaskWithDecision(pwaApplicationDetail, ConsentReviewDecision.APPROVE);

    // schedule consent issue
    try {

      // make the job id unique in case the job fails and we need to create another one later
      var jobId = pwaApplicationDetail.getId() + "-" + clock.instant().getEpochSecond();

      JobKey jobKey = jobKey(jobId, "PadConsentIssue");
      JobDetail jobDetail = newJob(ConsentIssueSchedulerBean.class)
          .withIdentity(jobKey)
          .usingJobData("issuingWuaId", issuingUser.getWuaId())
          .requestRecovery()
          .storeDurably()
          .build();

      jobDetail.getJobDataMap().put("approvalTime", clock.instant());

      scheduler.addJob(jobDetail, false);
      scheduler.triggerJob(jobKey);

    } catch (Exception e) {
      throw new RuntimeException("Error scheduling consent issue job", e);
    }

  }

  public boolean areThereAnyOpenReviews(PwaApplicationDetail pwaApplicationDetail) {
    return consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .anyMatch(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()));
  }

  public boolean isApplicationConsented(PwaApplicationDetail pwaApplicationDetail) {
    return consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .anyMatch(review -> ConsentReviewStatus.APPROVED.equals(review.getStatus()));
  }

  public ConsentReview approveConsentReview(PwaApplicationDetail pwaApplicationDetail,
                                            WebUserAccount approvingUser,
                                            Instant approvalTime) {

    // get open review if exists, error if it doesn't
    var openReview = consentReviewRepository.findAllByPwaApplicationDetail(pwaApplicationDetail).stream()
        .filter(review -> ConsentReviewStatus.OPEN.equals(review.getStatus()))
        .findFirst()
        .orElseThrow(() -> new ConsentReviewException(String.format(
            "Can't issue consent as there is no open consent review for PWA detail with id [%s]", pwaApplicationDetail.getId())));

    // end review
    openReview.setEndTimestamp(approvalTime);
    openReview.setStatus(ConsentReviewStatus.APPROVED);
    openReview.setEndedByPersonId(approvingUser.getLinkedPerson().getId());

    return consentReviewRepository.save(openReview);

  }

}
