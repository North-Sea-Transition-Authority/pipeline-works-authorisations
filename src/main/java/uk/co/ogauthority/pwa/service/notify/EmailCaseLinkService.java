package uk.co.ogauthority.pwa.service.notify;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@Service
public class EmailCaseLinkService {

  @Value("${pwa.url.base}")
  private String pwaUrlBase;

  @Value("${context-path}")
  private String contextPath;

  public String generateCaseManagementLink(PwaApplication application) {
    return pwaUrlBase + contextPath + CaseManagementUtils.routeCaseManagement(application);
  }

}