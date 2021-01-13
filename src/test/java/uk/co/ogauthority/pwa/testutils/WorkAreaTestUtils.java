package uk.co.ogauthority.pwa.testutils;

import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem;

public class WorkAreaTestUtils {

  private WorkAreaTestUtils() {
  }

  public static PageView<PwaApplicationWorkAreaItem> setUpFakeAppPageView(int page) {
    var fakePage = WorkAreaApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(),
        PageRequest.of(page, 10)
    );

    return PageView.fromPage(
        fakePage,
        "workAreaUri",
        searchItem -> new PwaApplicationWorkAreaItem(searchItem, applicationDetailSearchItem -> "Fake_View_Url")
    );

  }

  public static PageView<ConsultationRequestWorkAreaItem> setUpFakeConsultationPageView(int page) {
    var fakePage = new PageImpl<ConsultationRequestSearchItem>(
        List.of(),
        PageRequest.of(page, 10),
        0
    );

    return PageView.fromPage(
        fakePage,
        "workAreaUri",
        searchItem -> new ConsultationRequestWorkAreaItem(searchItem, consultationRequestSearchItem -> "Fake_View_Url")
    );

  }

}
