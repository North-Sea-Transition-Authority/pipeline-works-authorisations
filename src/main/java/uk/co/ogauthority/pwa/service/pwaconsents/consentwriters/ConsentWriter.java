package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

/**
 * Implementations of this interface are responsible for updating specific areas of the consented PWA datasets.
 */
public interface ConsentWriter {

  int getExecutionOrder();

  ApplicationTask getTaskDependentOn();

  void write(PwaApplicationDetail pwaApplicationDetail, PwaConsent pwaConsent);

}
