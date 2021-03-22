package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.fieldinformation.PadFieldService;

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
  public ApplicationTask getTaskDependentOn() {
    return ApplicationTask.FIELD_INFORMATION;
  }

  @Override
  public void write(PwaApplicationDetail pwaApplicationDetail,
                    PwaConsent pwaConsent) {

    boolean writeNewFieldDetails = false;
    var currentPwaDetail = masterPwaService.getCurrentDetailOrThrow(pwaApplicationDetail.getMasterPwa());

    // if first application for PWA, need to set the current master PWA detail to consented and write any fields to consented model
    if (pwaConsent.getVariationNumber() == 0) {

      masterPwaService.updateDetail(
          currentPwaDetail,
          MasterPwaDetailStatus.CONSENTED,
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

        currentPwaDetail = masterPwaService.createNewDetail(pwaApplicationDetail.getMasterPwa(),
            padFieldView.getLinkedToFields(), padFieldView.getPwaLinkedToDescription());

        writeNewFieldDetails = true;

      }

    }

    if (writeNewFieldDetails) {

      var padFields = padFieldService.getActiveFieldsForApplicationDetail(pwaApplicationDetail);

      masterPwaDetailFieldService.createMasterPwaFieldsFromPadFields(currentPwaDetail, padFields);

    }

  }

}