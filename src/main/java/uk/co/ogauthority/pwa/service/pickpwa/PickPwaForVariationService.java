package uk.co.ogauthority.pwa.service.pickpwa;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;

/**
 * Coordinate migration of legacy PWAs into PWAs usable by the current in order to start a variation.
 * Use the new migrated/existing MasterPwa as the basis for the new application.
 */
@Service
public class PickPwaForVariationService {

  private final PickedPwaRetrievalService pickedPwaRetrievalService;
  private final PwaApplicationCreationService pwaApplicationCreationService;

  @Autowired
  public PickPwaForVariationService(
      PickedPwaRetrievalService pickedPwaRetrievalService,
      PwaApplicationCreationService pwaApplicationCreationService) {
    this.pickedPwaRetrievalService = pickedPwaRetrievalService;
    this.pwaApplicationCreationService = pwaApplicationCreationService;
  }

  @Transactional
  public PwaApplication createPwaVariationApplicationForPickedPwa(PickablePwa pickedPwa,
                                                                  PwaApplicationType pwaApplicationType,
                                                                  WebUserAccount user) {
    var masterPwa = pickedPwaRetrievalService.getOrMigratePickedPwa(pickedPwa, user);
    return pwaApplicationCreationService.createVariationPwaApplication(user, masterPwa, pwaApplicationType)
        .getPwaApplication();
  }
}
