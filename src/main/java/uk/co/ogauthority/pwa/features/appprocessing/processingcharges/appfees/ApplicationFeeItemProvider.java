package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees;

import java.util.List;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.feeproviders.ApplicationFeeItem;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal.FeePeriodDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Provides Fee items based on applications details specific conditions and specific fee period details.
 */
public interface ApplicationFeeItemProvider {

  /**
   * If canProvideFeeItems returns true, define when the fee items should be provided in relation to other fee providers.
   */
  int getProvisionOrdering();

  /**
   * Should the fee items that could be provided by this service be provided.
   */
  boolean canProvideFeeItems(PwaApplicationDetail pwaApplicationDetail);

  /**
   * Get fee items appropriate for the fee detail and application.
   */
  List<ApplicationFeeItem> provideFees(FeePeriodDetail feePeriodDetail, PwaApplicationDetail pwaApplicationDetail);

  PwaApplicationFeeType getApplicationFeeType();

}
