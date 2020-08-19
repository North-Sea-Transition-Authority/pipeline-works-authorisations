package uk.co.ogauthority.pwa.service.applicationsummariser;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface ApplicationSectionSummariser {

  /**
   * When summarising, if the current version of the application no long has a summarised section,
   * but the previous version did, or vice versa, you want that diff summary to be generated.
   */
  boolean canSummarise(PwaApplicationDetail pwaApplicationDetail);

  /**
   * Generate a summary of differences between two versions of an application where the intended rendering template is known.
   */
  ApplicationSectionSummary summariseDifferences(PwaApplicationDetail pwaApplicationDetail,
                                                 String templateName);



}
