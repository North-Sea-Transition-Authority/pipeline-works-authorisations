package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.Collection;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;

/**
 * Implementations of this interface are responsible for updating specific areas of the consented PWA datasets.
 */
public interface ConsentWriter {

  int getExecutionOrder();

  boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent);

  ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail, PwaConsent pwaConsent, ConsentWriterDto consentWriterDto);

}
