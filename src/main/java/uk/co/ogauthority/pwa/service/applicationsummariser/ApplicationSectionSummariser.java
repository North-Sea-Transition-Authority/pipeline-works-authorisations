package uk.co.ogauthority.pwa.service.applicationsummariser;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface ApplicationSectionSummariser {

  /**
   * Return true when the summary section is appropriate for an application detail.
   */
  boolean canSummarise(PwaApplicationDetail pwaApplicationDetail);

  /**
   * Generate a summary an application where the intended rendering template is known.
   */
  ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                             String templateName);


}
