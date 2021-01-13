package uk.co.ogauthority.pwa.service.search.applicationsearch;



import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSearchDisplayItemCreatorTest {
  private static final int APP_ID = 100;
  private static final PwaApplicationType TYPE = PwaApplicationType.INITIAL;

  private ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator;

  private ApplicationDetailItemView applicationDetailItemView;

  @Before
  public void setUp() throws Exception {
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
  public void createDisplayItem() {
    // simple test to make sure we use the expected access url on results. Object construction done and tested as
    // part of ApplicationSearchDisplayItem.

    var displayItem = applicationSearchDisplayItemCreator.createDisplayItem(applicationDetailItemView);

    assertThat(displayItem.getAccessUrl()).isEqualTo(CaseManagementUtils.routeCaseManagement(APP_ID, TYPE));
  }
}