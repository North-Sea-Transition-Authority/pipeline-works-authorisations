package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class DepositDrawingUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  @Autowired
  public DepositDrawingUrlFactory(PwaApplicationType applicationType, Integer applicationId) {
    this.applicationType = applicationType;
    this.applicationId = applicationId;
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
