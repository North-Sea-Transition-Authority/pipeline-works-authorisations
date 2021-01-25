package uk.co.ogauthority.pwa.service.pwaapplications.search;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Util to simplify tests interfacing with work area  search code.
 */
public class WorkAreaApplicationSearchTestUtil {

  public static WorkAreaApplicationDetailSearchItem getSearchDetailItem(PwaApplicationStatus status) {
    return getSearchDetailItem(status, LocalDateTime.of(2020, 1, 2, 3, 4, 5)
        .toInstant(ZoneOffset.ofTotalSeconds(0)));
  }

  public static WorkAreaApplicationDetailSearchItem getSearchDetailItem(PwaApplicationDetail pwaApplicationDetail, Instant proposedStartDate) {

    var searchItem = getSearchDetailItem(pwaApplicationDetail.getStatus(), proposedStartDate);
    searchItem.setPwaApplicationDetailId(pwaApplicationDetail.getId());
    searchItem.setPwaApplicationId(pwaApplicationDetail.getMasterPwaApplicationId());
    return searchItem;

  }

  public static WorkAreaApplicationDetailSearchItem getSearchDetailItem(PwaApplicationStatus status, Instant proposedStartDate) {
    var applicationDetailSearchItem = new WorkAreaApplicationDetailSearchItem();

    applicationDetailSearchItem.setPadStatus(status);

    //defaults
    applicationDetailSearchItem.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailSearchItem.setPwaApplicationId(100);
    applicationDetailSearchItem.setPadFields(List.of("FIELD2", "FIELD1"));
    applicationDetailSearchItem.setPadHolderNameList(List.of("PAD HOLDER 1", "PAD HOLDER 2"));
    applicationDetailSearchItem.setPwaHolderNameList(List.of("PWA HOLDER 1", "PWA HOLDER 2"));
    applicationDetailSearchItem.setPadProjectName("PROJECT_NAME");
    applicationDetailSearchItem.setPadProposedStart(proposedStartDate);
    applicationDetailSearchItem.setPadStatusTimestamp(
        LocalDateTime.of(2020, 2, 3, 4, 5, 6)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    applicationDetailSearchItem.setPwaReference("PWA_REF");
    applicationDetailSearchItem.setPadReference("PAD_REF");
    applicationDetailSearchItem.setTipFlag(true);
    applicationDetailSearchItem.setSubmittedAsFastTrackFlag(false);
    return applicationDetailSearchItem;
  }

  public static Page<WorkAreaApplicationDetailSearchItem> setupFakeApplicationSearchResultPage(
      List<WorkAreaApplicationDetailSearchItem> results,
      Pageable pageable) {

    return new PageImpl<WorkAreaApplicationDetailSearchItem>(
        results,
        pageable,
        results.size());
  }
}
