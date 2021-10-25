package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;

@Service
public class InitialPwaMasterDetailWriter implements ConsentWriter {

  private final MasterPwaService masterPwaService;

  @Autowired
  public InitialPwaMasterDetailWriter(MasterPwaService masterPwaService) {
    this.masterPwaService = masterPwaService;
  }

  @Override
  public int getExecutionOrder() {
    return 1;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return pwaConsent.getConsentType().equals(PwaConsentType.INITIAL_PWA);
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail,
                                PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {

    var currentPwaDetail = masterPwaService.createNewDetailWithStatus(
        pwaApplicationDetail.getMasterPwa(),
        MasterPwaDetailStatus.CONSENTED
    );

    masterPwaService.updateDetailReference(currentPwaDetail, pwaConsent.getReference());

    return consentWriterDto;

  }

}