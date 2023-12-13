package uk.co.ogauthority.pwa.service.pwaconsents.consentwriters;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadAreaService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailAreaService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.consentwriters.pipelines.ConsentWriterDto;

@Service
public class AreaWriter implements ConsentWriter {

  private final MasterPwaService masterPwaService;
  private final MasterPwaDetailAreaService masterPwaDetailAreaService;
  private final PadAreaService padAreaService;

  @Autowired
  public AreaWriter(MasterPwaService masterPwaService,
                    MasterPwaDetailAreaService masterPwaDetailAreaService,
                    PadAreaService padAreaService) {
    this.masterPwaService = masterPwaService;
    this.masterPwaDetailAreaService = masterPwaDetailAreaService;
    this.padAreaService = padAreaService;
  }

  @Override
  public int getExecutionOrder() {
    return 5;
  }

  @Override
  public boolean writerIsApplicable(Collection<ApplicationTask> applicationTaskSet, PwaConsent pwaConsent) {
    var applicableTasks = Set.of(ApplicationTask.FIELD_INFORMATION, ApplicationTask.CARBON_STORAGE_INFORMATION);
    return applicationTaskSet.stream()
        .anyMatch(applicableTasks::contains);
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
          pwaApplicationDetail.getLinkedToArea(),
          pwaApplicationDetail.getNotLinkedDescription());

      writeNewFieldDetails = true;

    } else {

      // otherwise we need to see if there are differences between the application and
      // the current master PWA info before deciding to write new fields
      var masterPwaFieldView = masterPwaDetailAreaService
          .getCurrentMasterPwaDetailFieldLinksView(pwaApplicationDetail.getPwaApplication());

      var padFieldView = padAreaService.getApplicationFieldLinksView(pwaApplicationDetail);

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

      var padFields = padAreaService.getActiveFieldsForApplicationDetail(pwaApplicationDetail);

      masterPwaDetailAreaService.createMasterPwaFieldsFromPadFields(currentPwaDetail, padFields);

    }

    return consentWriterDto;

  }

}
