package uk.co.ogauthority.pwa.service.workarea;

import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem;

public class WorkAreaResult {

  private final PageView<PwaApplicationWorkAreaItem> applicationsTabPages;

  private final PageView<ConsultationRequestWorkAreaItem> consultationsTabPages;

  public WorkAreaResult(PageView<PwaApplicationWorkAreaItem> applicationsTabPages,
                        PageView<ConsultationRequestWorkAreaItem> consultationsTabPages) {
    this.applicationsTabPages = applicationsTabPages;
    this.consultationsTabPages = consultationsTabPages;
  }

  public PageView<PwaApplicationWorkAreaItem> getApplicationsTabPages() {
    return applicationsTabPages;
  }

  public PageView<ConsultationRequestWorkAreaItem> getConsultationsTabPages() {
    return consultationsTabPages;
  }

}
