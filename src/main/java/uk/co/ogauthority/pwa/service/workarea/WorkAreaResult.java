package uk.co.ogauthority.pwa.service.workarea;

import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem;

public class WorkAreaResult {

  private final PageView<PwaApplicationWorkAreaItem> applicationsTabPages;

  private final PageView<ConsultationRequestWorkAreaItem> consultationsTabPages;

  private final PageView<AsBuiltNotificationWorkAreaItem> asBuiltNotificationTabPages;

  public WorkAreaResult(PageView<PwaApplicationWorkAreaItem> applicationsTabPages,
                        PageView<ConsultationRequestWorkAreaItem> consultationsTabPages,
                        PageView<AsBuiltNotificationWorkAreaItem> asBuiltNotificationTabPages) {
    this.applicationsTabPages = applicationsTabPages;
    this.consultationsTabPages = consultationsTabPages;
    this.asBuiltNotificationTabPages = asBuiltNotificationTabPages;
  }

  public PageView<PwaApplicationWorkAreaItem> getApplicationsTabPages() {
    return applicationsTabPages;
  }

  public PageView<ConsultationRequestWorkAreaItem> getConsultationsTabPages() {
    return consultationsTabPages;
  }

  public PageView<AsBuiltNotificationWorkAreaItem> getAsBuiltNotificationTabPages() {
    return asBuiltNotificationTabPages;
  }

}
