package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class DepositDrawingUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  public DepositDrawingUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    this.applicationType = pwaApplicationDetail.getPwaApplicationType();
    this.applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
  }

  public String getAddPipelineDrawingUrl() {
    return ReverseRouter.route(on(PermanentDepositDrawingsController.class)
        .renderAddDepositDrawing(applicationType, applicationId, null, null));
  }

  public String getPipelineDrawingDownloadUrl(String fileId) {
    return ReverseRouter.route(on(PermanentDepositDrawingsController.class)
        .handleDownload(applicationType, applicationId, fileId, null));
  }
}
