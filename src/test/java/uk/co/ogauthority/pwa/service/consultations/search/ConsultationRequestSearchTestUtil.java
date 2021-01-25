package uk.co.ogauthority.pwa.service.consultations.search;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Util to simplify tests interfacing with consultation search code.
 */
public class ConsultationRequestSearchTestUtil {

  public static ConsultationRequestSearchItem getSearchDetailItem(ConsultationRequestStatus status) {

    var consultationRequestSearchItem = new ConsultationRequestSearchItem();

    ApplicationDetailView applicationDetailView = new ApplicationDetailView();

    //defaults
    applicationDetailView.setPadStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    applicationDetailView.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailView.setPwaApplicationId(100);
    applicationDetailView.setPadFields(List.of("FIELD2", "FIELD1"));
    applicationDetailView.setPadProjectName("PROJECT_NAME");
    applicationDetailView.setPadProposedStart(
        LocalDateTime.of(2020, 1, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    applicationDetailView.setPadStatusTimestamp(
        LocalDateTime.of(2020, 2, 3, 4, 5, 6)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    applicationDetailView.setPwaReference("PWA_REF");
    applicationDetailView.setPadReference("PAD_REF");
    applicationDetailView.setTipFlag(true);
    applicationDetailView.setSubmittedAsFastTrackFlag(false);

    consultationRequestSearchItem.setApplicationDetailView(applicationDetailView);

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
