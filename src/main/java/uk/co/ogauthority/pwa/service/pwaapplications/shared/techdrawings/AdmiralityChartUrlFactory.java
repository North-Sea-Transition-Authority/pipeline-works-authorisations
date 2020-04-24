package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.AdmiralityChartDocumentsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class AdmiralityChartUrlFactory {

  private final PwaApplicationType applicationType;
  private final Integer applicationId;

  public AdmiralityChartUrlFactory(PwaApplicationDetail pwaApplicationDetail) {
    applicationType = pwaApplicationDetail.getPwaApplicationType();
    applicationId = pwaApplicationDetail.getMasterPwaApplicationId();
  }

  public String getAddDocumentsUrl() {
    return ReverseRouter.route(on(AdmiralityChartDocumentsController.class)
        .renderEditCableCrossingDocuments(applicationType, applicationId, null, null));
  }

  public String getDocumentsDownloadUrl() {
    return ReverseRouter.route(on(AdmiralityChartDocumentsController.class)
        .renderEditCableCrossingDocuments(applicationType, applicationId, null, null));
  }


}
