package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.controller.AdmiraltyChartDocumentsController;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementRestController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

public class AdmiraltyChartUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  public AdmiraltyChartUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    applicationType = pwaApplicationDetail.getPwaApplicationType();
    applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
  }

  public String getAddDocumentsUrl() {
    return ReverseRouter.route(on(AdmiraltyChartDocumentsController.class)
        .renderEditAdmiraltyChartDocuments(applicationType, applicationId, null, null));
  }

  public String getDocumentsDownloadUrl() {
    return ReverseRouter.route(on(PadFileManagementRestController.class).download(applicationId, null));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdmiraltyChartUrlFactory that = (AdmiraltyChartUrlFactory) o;
    return applicationType == that.applicationType
        && Objects.equals(applicationId, that.applicationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationType, applicationId);
  }
}
