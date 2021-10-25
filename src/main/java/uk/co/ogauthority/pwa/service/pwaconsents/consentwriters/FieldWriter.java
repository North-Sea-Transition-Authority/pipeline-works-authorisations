package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.Collection;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.fieldinformation.PadFieldService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;

@Service
public class FieldWriter implements ConsentWriter {

  private final MasterPwaService masterPwaService;
  private final MasterPwaDetailFieldService masterPwaDetailFieldService;
  private final PadFieldService padFieldService;

  @Autowired
  public FieldWriter(MasterPwaService masterPwaService,
                     MasterPwaDetailFieldService masterPwaDetailFieldService,
                     PadFieldService padFieldService) {
    this.masterPwaService = masterPwaService;
    this.masterPwaDetailFieldService = masterPwaDetailFieldService;
    this.padFieldService = padFieldService;
  }

  @Override
  public int getExecutionOrder() {
    return 5;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    return applicationTaskSet.contains(ApplicationTask.FIELD_INFORMATION);
  }

  @Override
  public ConsentWriterDto write(PwaApplicationDetail pwaApplicationDetail,
                                PwaConsent pwaConsent,
                                ConsentWriterDto consentWriterDto) {

    boolean writeNewFieldDetails = false;
    var currentPwaDetail = masterPwaService.getCurrentDetailOrThrow(pwaApplicationDetail.getMasterPwa());

    // if first application for PWA, need to set the current master PWA detail to consented and write any fields to consented model
    if (pwaConsent.getVariationNumber() != null && pwaConsent.getVariationNumber() == 0) {

      masterPwaService.updateDetailFieldInfo(
          currentPwaDetail,
          pwaApplicationDetail.getLinkedToField(),
          pwaApplicationDetail.getNotLinkedDescription());

      writeNewFieldDetails = true;

    } else {

      // otherwise we need to see if there are differences between the application and
      // the current master PWA info before deciding to write new fields
      var masterPwaFieldView = masterPwaDetailFieldService
          .getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication());

      var padFieldView = padFieldService.getApplicationFieldLinksView(pwaApplicationDetail);

      if (!Objects.equals(masterPwaFieldView, padFieldView)) {

        currentPwaDetail = masterPwaService.createDuplicateNewDetail(pwaApplicationDetail.getMasterPwa());
        masterPwaService.updateDetailFieldInfo(
            currentPwaDetail,
            padFieldView.getLinkedToFields(),
            padFieldView.getPwaLinkedToDescription());

        writeNewFieldDetails = true;

      }

    }

    if (writeNewFieldDetails) {

      var padFields = padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail);

      masterPwaDetailFieldService.createMasterPwaFieldsFromPadFields(currentPwaDetail, padFields);

    }

    return consentWriterDto;

  }

}