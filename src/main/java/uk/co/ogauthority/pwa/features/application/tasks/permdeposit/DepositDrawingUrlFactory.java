package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;


import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class DepositDrawingUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

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

  public String getEditDrawingUrl(int drawingId) {
    return ReverseRouter.route(on(PermanentDepositDrawingsController.class)
        .renderEditDepositDrawing(
            this.applicationType, this.applicationId,
            null, drawingId, null));
  }

  public String getRemoveDrawingUrl(int drawingId) {
    return ReverseRouter.route(on(PermanentDepositDrawingsController.class)
        .renderRemoveDepositDrawing(
            this.applicationType, this.applicationId,
            null, drawingId, null));
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DepositDrawingUrlFactory that = (DepositDrawingUrlFactory) o;
    return applicationType == that.applicationType
        && Objects.equals(applicationId, that.applicationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationType, applicationId);
  }
}
