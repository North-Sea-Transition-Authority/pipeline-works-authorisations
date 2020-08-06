package uk.co.ogauthority.pwa.service.consultations.search;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Util to simplify tests interfacing with consultation search code.
 */
public class ConsultationRequestSearchTestUtil {

  public static ConsultationRequestSearchItem getSearchDetailItem(ConsultationRequestStatus status) {

    var consultationRequestSearchItem = new ConsultationRequestSearchItem();

    var applicationDetailSearchItem = new ApplicationDetailSearchItem();

    //defaults
    applicationDetailSearchItem.setPadStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
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

    consultationRequestSearchItem.setApplicationDetailSearchItem(applicationDetailSearchItem);

    consultationRequestSearchItem.setConsultationRequestId(1000);
    consultationRequestSearchItem.setConsultationRequestStatus(status);
    consultationRequestSearchItem.setConsulteeGroupId(1001);
    consultationRequestSearchItem.setConsulteeGroupName("test");

    return consultationRequestSearchItem;

  }

  public static Page<ConsultationRequestSearchItem> setupFakeConsultationSearchResultPage(
      List<ConsultationRequestSearchItem> results,
      Pageable pageable) {

    return new PageImpl<ConsultationRequestSearchItem>(
        results,
        pageable,
        results.size());
  }
}
