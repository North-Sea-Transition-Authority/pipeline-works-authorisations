package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem.STATUS_LABEL;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItemTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;

@ExtendWith(MockitoExtension.class)
class PwaApplicationWorkAreaItemTest {

  private PwaApplicationWorkAreaItem pwaApplicationWorkAreaItem;
  private WorkAreaApplicationDetailSearchItem workAreaApplicationDetailSearchItem;

  private static final String VIEW_URL = "EXAMPLE_URL";


  @BeforeEach
  void setup() {
    workAreaApplicationDetailSearchItem = new WorkAreaApplicationDetailSearchItem();
    workAreaApplicationDetailSearchItem.setApplicationType(PwaApplicationType.INITIAL);
    workAreaApplicationDetailSearchItem.setPwaApplicationId(100);
    workAreaApplicationDetailSearchItem.setPadFields(List.of("FIELD2", "FIELD1"));
    workAreaApplicationDetailSearchItem.setPadHolderNameList(List.of("PAD_HOLDER"));
    workAreaApplicationDetailSearchItem.setPwaHolderNameList(List.of("PWA_HOLDER"));
    workAreaApplicationDetailSearchItem.setPadFields(List.of("FIELD2", "FIELD1"));
    workAreaApplicationDetailSearchItem.setPadProjectName("PROJECT_NAME");
    workAreaApplicationDetailSearchItem.setPadProposedStart(
        LocalDateTime.of(2020, 1, 2, 3, 4, 5)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));
    workAreaApplicationDetailSearchItem.setPadStatusTimestamp(
        LocalDateTime.of(2020, 2, 3, 4, 5, 6)
            .toInstant(ZoneOffset.ofTotalSeconds(0)));

    workAreaApplicationDetailSearchItem.setPwaReference("PWA_REF");
    workAreaApplicationDetailSearchItem.setPadReference("PAD_REF");

    workAreaApplicationDetailSearchItem.setPadStatus(PwaApplicationStatus.DRAFT);
    workAreaApplicationDetailSearchItem.setTipFlag(true);
    workAreaApplicationDetailSearchItem.setSubmittedAsFastTrackFlag(false);


  }


  @Test
  void pwaApplicationWorkAreaItem() {
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);

    assertThat(pwaApplicationWorkAreaItem.getPwaApplicationId())
        .isEqualTo(workAreaApplicationDetailSearchItem.getPwaApplicationId());
    assertThat(pwaApplicationWorkAreaItem.getApplicationTypeDisplay())
        .isEqualTo(workAreaApplicationDetailSearchItem.getApplicationType().getDisplayName());
    assertThat(pwaApplicationWorkAreaItem.getOrderedFieldList()).isEqualTo(List.of("FIELD1", "FIELD2"));
    assertThat(pwaApplicationWorkAreaItem.getMasterPwaReference())
        .isEqualTo(workAreaApplicationDetailSearchItem.getPwaReference());
    assertThat(pwaApplicationWorkAreaItem.getApplicationReference()).isEqualTo(
        workAreaApplicationDetailSearchItem.getPadReference());
    assertThat(pwaApplicationWorkAreaItem.getApplicationStatusDisplay())
        .isEqualTo(workAreaApplicationDetailSearchItem.getPadStatus().getDisplayName());

    assertThat(pwaApplicationWorkAreaItem.getMasterPwaReference()).isEqualTo("PWA_REF");
    assertThat(pwaApplicationWorkAreaItem.getApplicationReference()).isEqualTo("PAD_REF");

    assertThat(pwaApplicationWorkAreaItem.getProjectName()).isEqualTo("PROJECT_NAME");
    assertThat(pwaApplicationWorkAreaItem.getFormattedStatusSetDatetime())
        .isEqualTo("03/02/2020 04:05");

    assertThat(pwaApplicationWorkAreaItem.isTipFlag()).isEqualTo(workAreaApplicationDetailSearchItem.isTipFlag());

  }


  @Test
  void getFormattedProposedStartDate_whenSet() {
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);
    assertThat(pwaApplicationWorkAreaItem.getProposedStartDateDisplay()).isEqualTo("02/01/2020");
  }

  @Test
  void getFormattedProposedStartDate_whenNull() {
    workAreaApplicationDetailSearchItem.setPadProposedStart(null);
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);
    assertThat(pwaApplicationWorkAreaItem.getProposedStartDateDisplay()).isNull();
  }

  @Test
  void getFormattedStatusSetDatetime_whenSet() {
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);
    assertThat(pwaApplicationWorkAreaItem.getFormattedStatusSetDatetime()).isEqualTo("03/02/2020 04:05");
  }

  @Test
  void getFormattedStatusSetDatetime_whenNull() {
    workAreaApplicationDetailSearchItem.setPadStatusTimestamp(null);
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);
    assertThat(pwaApplicationWorkAreaItem.getFormattedStatusSetDatetime()).isNull();
  }

  @Test
  void getFastTrackLabelText_notFastTrack() {

    var workAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);

    assertThat(workAreaItem.getFastTrackLabelText()).isNull();

  }

  @Test
  void getFastTrackLabelText_fastTrack_notAccepted() {

    workAreaApplicationDetailSearchItem.setSubmittedAsFastTrackFlag(true);
    workAreaApplicationDetailSearchItem.setPadInitialReviewApprovedTimestamp(null);

    var workAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);

    assertThat(workAreaItem.getFastTrackLabelText()).isEqualTo(ApplicationTask.FAST_TRACK.getDisplayName());

  }

  @Test
  void getFastTrackLabelText_fastTrack_accepted() {

    workAreaApplicationDetailSearchItem.setSubmittedAsFastTrackFlag(true);
    workAreaApplicationDetailSearchItem.setPadInitialReviewApprovedTimestamp(Instant.now());

    var workAreaItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);

    assertThat(workAreaItem.getFastTrackLabelText()).isEqualTo(
        ApplicationTask.FAST_TRACK.getDisplayName() + " accepted");

  }


  @Test
  void getApplicationStatusColumn_whenNoCaseOfficer_andNotFastTrack() {
    workAreaApplicationDetailSearchItem.setCaseOfficerName(null);

    var workAreItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);

    assertThat(workAreItem.getApplicationStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            PwaApplicationStatus.DRAFT.getDisplayName()),
        WorkAreaColumnItemView.createLabelledItem(
            ApplicationWorkAreaItem.DEFAULT_APP_STATUS_SET_LABEL,
            workAreItem.getFormattedStatusSetDatetime())
    );

  }

  @Test
  void getApplicationStatusColumn_whenCaseOfficer_andFastTrackApproved() {
    var caseOfficer = "NAME";
    workAreaApplicationDetailSearchItem.setCaseOfficerName(caseOfficer);
    workAreaApplicationDetailSearchItem.setCaseOfficerPersonId(1);
    workAreaApplicationDetailSearchItem.setSubmittedAsFastTrackFlag(true);
    workAreaApplicationDetailSearchItem.setPadInitialReviewApprovedTimestamp(Instant.now());

    var workAreItem = new PwaApplicationWorkAreaItem(workAreaApplicationDetailSearchItem);

    assertThat(workAreItem.getApplicationStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            PwaApplicationStatus.DRAFT.getDisplayName()),
        WorkAreaColumnItemView.createLabelledItem(
            ApplicationWorkAreaItem.CASE_OFFICER_DISPLAY_LABEL,
            caseOfficer),
        WorkAreaColumnItemView.createLabelledItem(
            ApplicationWorkAreaItem.DEFAULT_APP_STATUS_SET_LABEL,
            workAreItem.getFormattedStatusSetDatetime()),
        WorkAreaColumnItemView.createTagItem(
            WorkAreaColumnItemView.TagType.SUCCESS,
            workAreItem.getFastTrackLabelText())
    );
  }

  /* Below are super type tests*/
  @Test
  void getAccessUrl_assertDefaultUrl(){
    ApplicationWorkAreaItemTestUtil.test_getAccessUrl_assertDefaultAccessUrl(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getSummaryColumn_whenFieldsExist(){
    ApplicationWorkAreaItemTestUtil.test_getSummaryColumn_whenFieldsExist(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getSummaryColumn_whenNoFields(){
    ApplicationWorkAreaItemTestUtil.test_getSummaryColumn_whenNoFields(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getHolderColumn_whenInitialType(){
    ApplicationWorkAreaItemTestUtil.test_getHolderColumn_whenInitialType(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);

  }

  @Test
  void getHolderColumn_whenNotInitialType() {
    ApplicationWorkAreaItemTestUtil.test_getHolderColumn_whenNotInitialType(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getApplicationColumn_whenApplicationNotComplete(){
    ApplicationWorkAreaItemTestUtil.test_getApplicationColumn_whenApplicationNotCompleteOrInitial(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);

  }

  @Test
  void getApplicationColumn_whenApplicationComplete() {
    ApplicationWorkAreaItemTestUtil.test_getApplicationColumn_whenApplicationCompleteOrNotInitial(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getApplicationColumn_whenUpdate_withinDeadline() {
    ApplicationWorkAreaItemTestUtil.testGetApplicationColumnWhenUpdateRequestWithinDeadline(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getApplicationColumn_whenUpdate_dueToday() {
    ApplicationWorkAreaItemTestUtil.testGetApplicationColumnWhenUpdateRequestDueToday(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getApplicationColumn_whenUpdate_overdue() {
    ApplicationWorkAreaItemTestUtil.testGetApplicationColumnWhenUpdateRequestOverdue(
        workAreaApplicationDetailSearchItem,
        PwaApplicationWorkAreaItem::new);
  }

}

