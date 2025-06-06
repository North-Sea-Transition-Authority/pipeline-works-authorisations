package uk.co.ogauthority.pwa.service.workarea.consultations;

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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItemTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;

@ExtendWith(MockitoExtension.class)
class ConsultationRequestWorkAreaItemTest {

  private ConsultationRequestWorkAreaItem consultationRequestWorkAreaItem;

  private ApplicationDetailView applicationDetailView;
  private ConsultationRequestSearchItem consultationRequestSearchItem;

  private static final String VIEW_URL = "EXAMPLE_URL";
  private static final String CONSULTEE_GROUP_NAME = "LONG_NAME";
  private static final String CONSULTEE_GROUP_NAME_ABBREV = "NAME";


  @BeforeEach
  void setup() {
    applicationDetailView = new ApplicationDetailView();
    consultationRequestSearchItem = new ConsultationRequestSearchItem();
    setApplicationDetailItemViewValues(applicationDetailView);
    setConsultationRequestSearchItemValues(applicationDetailView, consultationRequestSearchItem);
  }

  private void setApplicationDetailItemViewValues(ApplicationDetailView applicationDetailItemView) {
    applicationDetailItemView.setApplicationType(PwaApplicationType.INITIAL);
    applicationDetailItemView.setPwaApplicationId(100);
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

  private void setConsultationRequestSearchItemValues(ApplicationDetailView applicationDetailItemView,
                                                      ConsultationRequestSearchItem consultationRequestSearchItem) {
    consultationRequestSearchItem.setApplicationDetailView(applicationDetailItemView);
    consultationRequestSearchItem.setConsulteeGroupId(101);
    consultationRequestSearchItem.setConsulteeGroupName(CONSULTEE_GROUP_NAME);
    consultationRequestSearchItem.setConsulteeGroupAbbr(CONSULTEE_GROUP_NAME_ABBREV);
    consultationRequestSearchItem.setConsultationRequestStatus(ConsultationRequestStatus.ALLOCATION);
    consultationRequestSearchItem.setAssignedResponderName("Assigned Responder");
    consultationRequestSearchItem.setDeadlineDate(LocalDateTime.of(2020, 3, 3, 14, 5, 6).toInstant(ZoneOffset.ofTotalSeconds(0)));
  }

  @Test
  void consultationRequestWorkAreaItem() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);

    assertThat(consultationRequestWorkAreaItem.getPwaApplicationId())
        .isEqualTo(applicationDetailView.getPwaApplicationId());
    assertThat(consultationRequestWorkAreaItem.getApplicationTypeDisplay())
        .isEqualTo(applicationDetailView.getApplicationType().getDisplayName());
    assertThat(consultationRequestWorkAreaItem.getOrderedFieldList()).isEqualTo(List.of("FIELD1", "FIELD2"));
    assertThat(consultationRequestWorkAreaItem.getMasterPwaReference())
        .isEqualTo(applicationDetailView.getPwaReference());
    assertThat(consultationRequestWorkAreaItem.getApplicationReference()).isEqualTo(applicationDetailView.getPadReference());
    assertThat(consultationRequestWorkAreaItem.getApplicationStatusDisplay())
        .isEqualTo(applicationDetailView.getPadStatus().getDisplayName());

    assertThat(consultationRequestWorkAreaItem.getMasterPwaReference()).isEqualTo("PWA_REF");
    assertThat(consultationRequestWorkAreaItem.getApplicationReference()).isEqualTo("PAD_REF");

    assertThat(consultationRequestWorkAreaItem.getProjectName()).isEqualTo("PROJECT_NAME");
    assertThat(consultationRequestWorkAreaItem.getFormattedStatusSetDatetime())
        .isEqualTo("03/02/2020 04:05");

    assertThat(consultationRequestWorkAreaItem.isTipFlag()).isEqualTo(applicationDetailView.isTipFlag());

    assertThat(consultationRequestWorkAreaItem.getConsulteeGroupId()).isEqualTo(consultationRequestSearchItem.getConsulteeGroupId());
    assertThat(consultationRequestWorkAreaItem.getConsulteeGroupName()).isEqualTo(consultationRequestSearchItem.getConsulteeGroupName());
    assertThat(consultationRequestWorkAreaItem.getConsulteeGroupAbbr()).isEqualTo(consultationRequestSearchItem.getConsulteeGroupAbbr());
    assertThat(consultationRequestWorkAreaItem.getConsultationRequestId()).isEqualTo(consultationRequestSearchItem.getConsultationRequestId());
    assertThat(consultationRequestWorkAreaItem.getConsultationRequestStatus()).isEqualTo(consultationRequestSearchItem.getConsultationRequestStatus().getDisplayName());
    assertThat(consultationRequestWorkAreaItem.getConsultationRequestDeadlineDateTime()).isEqualTo("03/03/2020 14:05");

  }

  @Test
  void getAssignedResponderName_whenSet() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);
    assertThat(consultationRequestWorkAreaItem.getAssignedResponderName()).isEqualTo(consultationRequestSearchItem.getAssignedResponderName());
  }

  @Test
  void getAssignedResponderName_whenNull() {
    consultationRequestSearchItem.setAssignedResponderName(null);
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);
    assertThat(consultationRequestWorkAreaItem.getAssignedResponderName()).isNull();
  }

  @Test
  void getFormattedProposedStartDate_whenSet() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);
    assertThat(consultationRequestWorkAreaItem.getProposedStartDateDisplay()).isEqualTo("02/01/2020");
  }

  @Test
  void getFormattedProposedStartDate_whenNull() {
    applicationDetailView.setPadProposedStart(null);
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);
    assertThat(consultationRequestWorkAreaItem.getProposedStartDateDisplay()).isNull();
  }

  @Test
  void getFormattedStatusSetDatetime_whenSet() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);
    assertThat(consultationRequestWorkAreaItem.getFormattedStatusSetDatetime()).isEqualTo("03/02/2020 04:05");
  }

  @Test
  void getFormattedStatusSetDatetime_whenNull() {
    applicationDetailView.setPadStatusTimestamp(null);
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);
    assertThat(consultationRequestWorkAreaItem.getFormattedStatusSetDatetime()).isNull();
  }

  @Test
  void getFastTrackLabelText_notFastTrack() {

    var workAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);

    assertThat(workAreaItem.getFastTrackLabelText()).isNull();

  }

  @Test
  void getFastTrackLabelText_fastTrack_notAccepted() {

    applicationDetailView.setSubmittedAsFastTrackFlag(true);
    applicationDetailView.setPadInitialReviewApprovedTimestamp(null);

    var workAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);

    assertThat(workAreaItem.getFastTrackLabelText()).isEqualTo(ApplicationTask.FAST_TRACK.getDisplayName());

  }

  @Test
  void getFastTrackLabelText_fastTrack_accepted() {

    applicationDetailView.setSubmittedAsFastTrackFlag(true);
    applicationDetailView.setPadInitialReviewApprovedTimestamp(Instant.now());

    var workAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);

    assertThat(workAreaItem.getFastTrackLabelText()).isEqualTo(ApplicationTask.FAST_TRACK.getDisplayName() + " accepted");

  }


  @Test
  void getApplicationStatusColumn_whenConsultationDue_andFastTrackApproved_andNoResponder() {
    var caseOfficer = "NAME";
    applicationDetailView.setCaseOfficerName(caseOfficer);
    applicationDetailView.setCaseOfficerPersonId(1);
    applicationDetailView.setSubmittedAsFastTrackFlag(true);
    applicationDetailView.setPadInitialReviewApprovedTimestamp(Instant.now());

    consultationRequestSearchItem.setAssignedResponderName(null);

    var workAreItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);

    assertThat(workAreItem.getApplicationStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            consultationRequestSearchItem.getConsultationRequestStatus().getDisplayName()),
        WorkAreaColumnItemView.createLabelledItem(
            "Due date",
            workAreItem.getConsultationRequestDeadlineDateTime()),
        WorkAreaColumnItemView.createLabelledItem(
            "Consultee",
            CONSULTEE_GROUP_NAME + " (" + CONSULTEE_GROUP_NAME_ABBREV + ")"),
        WorkAreaColumnItemView.createTagItem(
            WorkAreaColumnItemView.TagType.SUCCESS,
            workAreItem.getFastTrackLabelText())
    );
  }

  @Test
  void getApplicationStatusColumn_whenConsultationDue_andFastTrackApproved_andResponderAssigned() {
    var caseOfficer = "NAME";
    applicationDetailView.setCaseOfficerName(caseOfficer);
    applicationDetailView.setCaseOfficerPersonId(1);
    applicationDetailView.setSubmittedAsFastTrackFlag(true);
    applicationDetailView.setPadInitialReviewApprovedTimestamp(Instant.now());

    consultationRequestSearchItem.setAssignedResponderName("RESPONDER");

    var workAreItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem);

    assertThat(workAreItem.getApplicationStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            consultationRequestSearchItem.getConsultationRequestStatus().getDisplayName()),
        WorkAreaColumnItemView.createLabelledItem(
            "Due date",
            workAreItem.getConsultationRequestDeadlineDateTime()),
        WorkAreaColumnItemView.createLabelledItem(
            "Consultee",
            CONSULTEE_GROUP_NAME + " (" + CONSULTEE_GROUP_NAME_ABBREV + ")"),
        WorkAreaColumnItemView.createLabelledItem(
            "Responder",
            "RESPONDER"),
        WorkAreaColumnItemView.createTagItem(
            WorkAreaColumnItemView.TagType.SUCCESS,
            workAreItem.getFastTrackLabelText())
    );
  }

  /* Below are super type tests*/
  @Test
  void getAccessUrl_assertDefaultUrl(){
   ApplicationWorkAreaItemTestUtil.test_getAccessUrl_assertDefaultAccessUrl(
       applicationDetailView,
       PwaApplicationWorkAreaItem::new);
 }

  @Test
  void getSummaryColumn_whenFieldsExist(){
    ApplicationWorkAreaItemTestUtil.test_getSummaryColumn_whenFieldsExist(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getSummaryColumn_whenNoFields(){
    ApplicationWorkAreaItemTestUtil.test_getSummaryColumn_whenNoFields(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getHolderColumn_whenInitialType(){
    ApplicationWorkAreaItemTestUtil.test_getHolderColumn_whenInitialType(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);

  }

  @Test
  void getHolderColumn_whenNotInitialType() {
    ApplicationWorkAreaItemTestUtil.test_getHolderColumn_whenNotInitialType(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getApplicationColumn_whenApplicationNotComplete(){
    ApplicationWorkAreaItemTestUtil.test_getApplicationColumn_whenApplicationNotCompleteOrInitial(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);

  }

  @Test
  void getApplicationColumn_whenApplicationComplete() {
    ApplicationWorkAreaItemTestUtil.test_getApplicationColumn_whenApplicationCompleteOrNotInitial(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);
  }

  @Test
  void getApplicationColumn_whenUpdate() {
    ApplicationWorkAreaItemTestUtil.testGetApplicationColumnWhenUpdateRequestWithinDeadline(
        applicationDetailView,
        PwaApplicationWorkAreaItem::new);
  }

}