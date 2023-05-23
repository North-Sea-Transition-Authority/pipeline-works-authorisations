package uk.co.ogauthority.pwa.service.search.applicationsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem.STATUS_LABEL;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItemTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationSearchDisplayItemTest {

  private static final int APP_ID = 100;

  private ApplicationSearchDisplayItem applicationSearchDisplayItem;
  private ApplicationDetailItemView applicationDetailItemView;


  @Before
  public void setup() {

    applicationDetailItemView = new ApplicationDetailView();
    applicationDetailItemView.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailItemView.setResourceType(PwaResourceType.PETROLEUM);
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
  public void getApplicationStatusColumn_whenNoCaseOfficer_andNotFastTrack() {
    applicationDetailItemView.setCaseOfficerName(null);

    applicationSearchDisplayItem = new ApplicationSearchDisplayItem(applicationDetailItemView);

    assertThat(applicationSearchDisplayItem.getApplicationStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            PwaApplicationStatus.DRAFT.getDisplayName()),
        WorkAreaColumnItemView.createLabelledItem(
            ApplicationWorkAreaItem.DEFAULT_APP_STATUS_SET_LABEL,
            applicationSearchDisplayItem.getFormattedStatusSetDatetime())
    );

  }

  @Test
  public void getApplicationStatusColumn_whenCaseOfficer_andFastTrackApproved() {
    var caseOfficer = "NAME";
    applicationDetailItemView.setCaseOfficerName(caseOfficer);
    applicationDetailItemView.setCaseOfficerPersonId(1);
    applicationDetailItemView.setSubmittedAsFastTrackFlag(true);
    applicationDetailItemView.setPadInitialReviewApprovedTimestamp(Instant.now());

    applicationSearchDisplayItem = new ApplicationSearchDisplayItem(applicationDetailItemView);

    assertThat(applicationSearchDisplayItem.getApplicationStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            PwaApplicationStatus.DRAFT.getDisplayName()),
        WorkAreaColumnItemView.createLabelledItem(
            ApplicationWorkAreaItem.CASE_OFFICER_DISPLAY_LABEL,
            caseOfficer),
        WorkAreaColumnItemView.createLabelledItem(
            ApplicationWorkAreaItem.DEFAULT_APP_STATUS_SET_LABEL,
            applicationSearchDisplayItem.getFormattedStatusSetDatetime()),
        WorkAreaColumnItemView.createTagItem(
            WorkAreaColumnItemView.TagType.SUCCESS,
            applicationSearchDisplayItem.getFastTrackLabelText())
    );
  }

  /* Below are super type tests*/
  @Test
  public void getAccessUrl_assertDefaultUrl(){
    ApplicationWorkAreaItemTestUtil.test_getAccessUrl_assertDefaultAccessUrl(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);
  }

  @Test
  public void getSummaryColumn_whenFieldsExist() {
    ApplicationWorkAreaItemTestUtil.test_getSummaryColumn_whenFieldsExist(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);
  }

  @Test
  public void getSummaryColumn_whenNoFields() {
    ApplicationWorkAreaItemTestUtil.test_getSummaryColumn_whenNoFields(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);
  }

  @Test
  public void getHolderColumn_whenInitialType() {
    ApplicationWorkAreaItemTestUtil.test_getHolderColumn_whenInitialType(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);

  }

  @Test
  public void getHolderColumn_whenNotInitialType() {
    ApplicationWorkAreaItemTestUtil.test_getHolderColumn_whenNotInitialType(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);
  }

  @Test
  public void getApplicationColumn_whenApplicationNotComplete() {
    ApplicationWorkAreaItemTestUtil.test_getApplicationColumn_whenApplicationNotCompleteOrInitial(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);

  }

  @Test
  public void getApplicationColumn_whenApplicationCompletee() {
    ApplicationWorkAreaItemTestUtil.test_getApplicationColumn_whenApplicationCompleteOrNotInitial(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);
  }

  @Test
  public void getApplicationColumn_whenUpdate() {
    ApplicationWorkAreaItemTestUtil.testGetApplicationColumnWhenUpdateRequestWithinDeadline(
        applicationDetailItemView,
        ApplicationSearchDisplayItem::new);
  }
}
