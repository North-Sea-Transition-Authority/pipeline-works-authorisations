package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.AdmiraltyChartDocumentsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

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
    return ReverseRouter.route(on(AdmiraltyChartDocumentsController.class)
        .handleDownload(applicationType, applicationId, null, null));
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
