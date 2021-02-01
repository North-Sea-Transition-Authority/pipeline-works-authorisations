package uk.co.ogauthority.pwa.service.pwaapplications.workflow;

import java.util.Optional;
import org.springframework.lang.Nullable;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationSubmitResult;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;

public interface ApplicationSubmissionService {

  /**
   * Optionally define a result to set on the workflow on submission.
   */
  Optional<PwaApplicationSubmitResult> getSubmissionWorkflowResult();


  /**
   * Provide the workflow task that should be completed on submission.
   */
  PwaApplicationWorkflowTask getTaskToComplete();


  /**
   * Provide the status that should be set on the submitted application detail.
   */
  PwaApplicationStatus getSubmittedApplicationDetailStatus(PwaApplicationDetail pwaApplicationDetail);

  /**
   * Each implementor is free to decide what code is required to run before standardised submission process.
   */
  void doBeforeSubmit(PwaApplicationDetail pwaApplicationDetail, Person submittedByPerson, @Nullable String submissionDescription);


  /**
   * Each implementor is free to decide what code is required to run after standardised submission process.
   */
  void doAfterSubmit(PwaApplicationDetail pwaApplicationDetail);


}
