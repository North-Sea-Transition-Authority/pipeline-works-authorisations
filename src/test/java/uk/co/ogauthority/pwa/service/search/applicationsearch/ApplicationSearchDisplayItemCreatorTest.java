package uk.co.ogauthority.pwa.service.search.applicationsearch;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItemTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationSearchDisplayItemCreatorTest {
  private static final int APP_ID = 100;
  private static final PwaApplicationType TYPE = PwaApplicationType.INITIAL;

  private ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator;

  private ApplicationDetailItemView applicationDetailItemView;

  @BeforeEach
  void setUp() throws Exception {
    applicationSearchDisplayItemCreator = new ApplicationSearchDisplayItemCreator();

    applicationDetailItemView = new ApplicationDetailView();
    applicationDetailItemView.setApplicationType(TYPE);
    applicationDetailItemView.setPwaApplicationId(APP_ID);
    applicationDetailItemView.setPadFields(List.of("FIELD2", "FIELD1"));
    applicationDetailItemView.setPadHolderNameList(List.of("PAD_HOLDER"));
    applicationDetailItemView.setPwaHolderNameList(List.of("PWA_HOLDER"));
    applicationDetailItemView.setPadFields(List.of("FIELD2", "FIELD1"));
    applicationDetailItemView.setPadProjectName("PROJECT_NAME");
    applicationDetailItemView.setPadProposedStart(
        LocalDateTime.of(2020, 1, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    applicationDetailItemView.setPadStatusTimestamp(
        LocalDateTime.of(2020, 2, 3, 4, 5, 6)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));

    applicationDetailItemView.setPwaReference("PWA_REF");
    applicationDetailItemView.setPadReference("PAD_REF");

    applicationDetailItemView.setPadStatus(PwaApplicationStatus.DRAFT);
    applicationDetailItemView.setTipFlag(true);
    applicationDetailItemView.setSubmittedAsFastTrackFlag(false);
  }

  @Test
  void createDisplayItem_usesDefaultAccessUrl() {
    // simple test to make sure we use the expected access url on results. Object construction done and tested as
    // part of ApplicationSearchDisplayItem.

    ApplicationWorkAreaItemTestUtil.test_getAccessUrl_assertDefaultAccessUrl(
        applicationDetailItemView,
        applicationSearchDisplayItemCreator::createDisplayItem
    );

  }
}