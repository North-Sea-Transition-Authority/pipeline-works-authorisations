package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appprocessinggeneration;

import java.util.Optional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewPaymentDecision;

/**
 * Wrapper class used to contain the properties needed for generating app processing task data
 * used by implementors of TestHarnessAppProcessingService.
 * Separate users/contexts are stored for easily accessing app processing service methods which require a certain context.
 */
public class TestHarnessAppProcessingProperties {

  private final AuthenticatedUserAccount applicantAua;
  private PwaAppProcessingContext applicantProcessingContext;
  private final AuthenticatedUserAccount caseOfficerAua;
  private PwaAppProcessingContext caseOfficerProcessingContext;
  private final AuthenticatedUserAccount pwaManagerAua;

  private final InitialReviewPaymentDecision initialReviewPaymentDecision;
  private final Integer pipelineQuantity;

  public TestHarnessAppProcessingProperties(AuthenticatedUserAccount applicantAua,
                                            PwaAppProcessingContext applicantProcessingContext,
                                            AuthenticatedUserAccount caseOfficerAua,
                                            AuthenticatedUserAccount pwaManagerAua,
                                            InitialReviewPaymentDecision initialReviewPaymentDecision,
                                            Integer pipelineQuantity) {
    this.applicantAua = applicantAua;
    this.applicantProcessingContext = applicantProcessingContext;
    this.caseOfficerAua = caseOfficerAua;
    this.pwaManagerAua = pwaManagerAua;
    this.caseOfficerProcessingContext = null;
    this.initialReviewPaymentDecision = initialReviewPaymentDecision;
    this.pipelineQuantity = pipelineQuantity;
  }


  AuthenticatedUserAccount getApplicantAua() {
    return applicantAua;
  }

  public PwaAppProcessingContext getApplicantProcessingContext() {
    return applicantProcessingContext;
  }

  void setApplicantProcessingContext(
      PwaAppProcessingContext applicantProcessingContext) {
    this.applicantProcessingContext = applicantProcessingContext;
  }

  public AuthenticatedUserAccount getCaseOfficerAua() {
    return caseOfficerAua;
  }

  public PwaAppProcessingContext getCaseOfficerProcessingContext() {

    return Optional.ofNullable(caseOfficerProcessingContext).orElseThrow(
        () -> new ActionNotAllowedException(String.format(
        "Case officer processing context cannot be accessed until the case officer is assigned to application with id %s ",
            applicantProcessingContext.getPwaApplication().getId())));
  }

  public void setCaseOfficerProcessingContext(PwaAppProcessingContext caseOfficerProcessingContext) {
    this.caseOfficerProcessingContext = caseOfficerProcessingContext;
  }

  public AuthenticatedUserAccount getPwaManagerAua() {
    return pwaManagerAua;
  }

  InitialReviewPaymentDecision getInitialReviewPaymentDecision() {
    return initialReviewPaymentDecision;
  }

  public Integer getPipelineQuantity() {
    return pipelineQuantity;
  }

  public PwaApplicationDetail getPwaApplicationDetail() {
    return applicantProcessingContext.getApplicationDetail();
  }

  public PwaApplication getPwaApplication() {
    return applicantProcessingContext.getPwaApplication();
  }


}
