package uk.co.ogauthority.pwa.service.pwaapplications.search;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Util to simplify tests interfacing with application search code.
 */
public class ApplicationSearchTestUtil {

  public static ApplicationDetailSearchItem getSearchDetailItem(PwaApplicationStatus status) {
    var applicationDetailSearchItem = new ApplicationDetailSearchItem();

    applicationDetailSearchItem.setPadStatus(status);

    //defaults
    applicationDetailSearchItem.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailSearchItem.setPwaApplicationId(100);
    applicationDetailSearchItem.setPadFields(List.of("FIELD2", "FIELD1"));
    applicationDetailSearchItem.setPadProjectName("PROJECT_NAME");
    applicationDetailSearchItem.setPadProposedStart(
        LocalDateTime.of(2020, 1, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    applicationDetailSearchItem.setPadStatusTimestamp(
        LocalDateTime.of(2020, 2, 3, 4, 5, 6)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    applicationDetailSearchItem.setPwaReference("PWA_REF");
    applicationDetailSearchItem.setPadReference("PAD_REF");
    applicationDetailSearchItem.setTipFlag(true);
    applicationDetailSearchItem.setSubmittedAsFastTrackFlag(false);
    return applicationDetailSearchItem;
  }

  public static Page<ApplicationDetailSearchItem> setupFakeApplicationSearchResultPage(
      List<ApplicationDetailSearchItem> results,
      Pageable pageable) {

    return new PageImpl<ApplicationDetailSearchItem>(
        results,
        pageable,
        results.size());
  }
}
