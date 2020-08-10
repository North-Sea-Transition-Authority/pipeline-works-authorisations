package uk.co.ogauthority.pwa.service.workarea.consultations;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.consultations.search.ConsultationRequestSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationRequestWorkAreaItemTest {

  private ConsultationRequestWorkAreaItem consultationRequestWorkAreaItem;
  private ApplicationDetailSearchItem applicationDetailSearchItem;
  private ConsultationRequestSearchItem consultationRequestSearchItem;

  private static final String VIEW_URL = "EXAMPLE_URL";


  @Before
  public void setup() {

    applicationDetailSearchItem = new ApplicationDetailSearchItem();
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

    applicationDetailSearchItem.setPadStatus(PwaApplicationStatus.DRAFT);
    applicationDetailSearchItem.setTipFlag(true);
    applicationDetailSearchItem.setSubmittedAsFastTrackFlag(false);

    consultationRequestSearchItem = new ConsultationRequestSearchItem();
    consultationRequestSearchItem.setApplicationDetailSearchItem(applicationDetailSearchItem);
    consultationRequestSearchItem.setConsulteeGroupId(101);
    consultationRequestSearchItem.setConsulteeGroupName("Test");
    consultationRequestSearchItem.setConsulteeGroupAbbr("T");
    consultationRequestSearchItem.setConsultationRequestStatus(ConsultationRequestStatus.ALLOCATION);
    consultationRequestSearchItem.setAssignedResponderName("Assigned Responder");
    consultationRequestSearchItem.setDeadlineDate(LocalDateTime.of(2020, 3, 3, 14, 5, 6).toInstant(ZoneOffset.ofTotalSeconds(0)));

  }

  @Test
  public void consultationRequestWorkAreaItem() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);

    assertThat(consultationRequestWorkAreaItem.getPwaApplicationId())
        .isEqualTo(applicationDetailSearchItem.getPwaApplicationId());
    assertThat(consultationRequestWorkAreaItem.getApplicationTypeDisplay())
        .isEqualTo(applicationDetailSearchItem.getApplicationType().getDisplayName());
    assertThat(consultationRequestWorkAreaItem.getOrderedFieldList()).isEqualTo(List.of("FIELD1", "FIELD2"));
    assertThat(consultationRequestWorkAreaItem.getMasterPwaReference())
        .isEqualTo(applicationDetailSearchItem.getPwaReference());
    assertThat(consultationRequestWorkAreaItem.getApplicationReference()).isEqualTo(applicationDetailSearchItem.getPadReference());
    assertThat(consultationRequestWorkAreaItem.getAccessUrl()).isEqualTo(VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getApplicationStatusDisplay())
        .isEqualTo(applicationDetailSearchItem.getPadStatus().getDisplayName());

    assertThat(consultationRequestWorkAreaItem.getMasterPwaReference()).isEqualTo("PWA_REF");
    assertThat(consultationRequestWorkAreaItem.getApplicationReference()).isEqualTo("PAD_REF");

    assertThat(consultationRequestWorkAreaItem.getProjectName()).isEqualTo("PROJECT_NAME");
    assertThat(consultationRequestWorkAreaItem.getFormattedStatusSetDatetime())
        .isEqualTo("03/02/2020 04:05");

    assertThat(consultationRequestWorkAreaItem.isTipFlag()).isEqualTo(applicationDetailSearchItem.isTipFlag());

    assertThat(consultationRequestWorkAreaItem.getConsulteeGroupId()).isEqualTo(consultationRequestSearchItem.getConsulteeGroupId());
    assertThat(consultationRequestWorkAreaItem.getConsulteeGroupName()).isEqualTo(consultationRequestSearchItem.getConsulteeGroupName());
    assertThat(consultationRequestWorkAreaItem.getConsulteeGroupAbbr()).isEqualTo(consultationRequestSearchItem.getConsulteeGroupAbbr());
    assertThat(consultationRequestWorkAreaItem.getConsultationRequestId()).isEqualTo(consultationRequestSearchItem.getConsultationRequestId());
    assertThat(consultationRequestWorkAreaItem.getConsultationRequestStatus()).isEqualTo(consultationRequestSearchItem.getConsultationRequestStatus().getDisplayName());
    assertThat(consultationRequestWorkAreaItem.getConsultationRequestDeadlineDateTime()).isEqualTo("03/03/2020 14:05");

  }

  @Test
  public void getAssignedResponderName_whenSet() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getAssignedResponderName()).isEqualTo(consultationRequestSearchItem.getAssignedResponderName());
  }

  @Test
  public void getAssignedResponderName_whenNull() {
    consultationRequestSearchItem.setAssignedResponderName(null);
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getAssignedResponderName()).isNull();
  }

  @Test
  public void getFormattedProposedStartDate_whenSet() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getProposedStartDateDisplay()).isEqualTo("02/01/2020");
  }

  @Test
  public void getFormattedProposedStartDate_whenNull() {
    applicationDetailSearchItem.setPadProposedStart(null);
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getProposedStartDateDisplay()).isNull();
  }

  @Test
  public void getFormattedStatusSetDatetime_whenSet() {
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getFormattedStatusSetDatetime()).isEqualTo("03/02/2020 04:05");
  }

  @Test
  public void getFormattedStatusSetDatetime_whenNull() {
    applicationDetailSearchItem.setPadStatusTimestamp(null);
    consultationRequestWorkAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);
    assertThat(consultationRequestWorkAreaItem.getFormattedStatusSetDatetime()).isNull();
  }

  @Test
  public void getFastTrackLabelText_notFastTrack() {

    var workAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);

    assertThat(workAreaItem.getFastTrackLabelText()).isNull();

  }

  @Test
  public void getFastTrackLabelText_fastTrack_notAccepted() {

    applicationDetailSearchItem.setSubmittedAsFastTrackFlag(true);
    applicationDetailSearchItem.setPadInitialReviewApprovedTimestamp(null);

    var workAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);

    assertThat(workAreaItem.getFastTrackLabelText()).isEqualTo(ApplicationTask.FAST_TRACK.getDisplayName());

  }

  @Test
  public void getFastTrackLabelText_fastTrack_accepted() {

    applicationDetailSearchItem.setSubmittedAsFastTrackFlag(true);
    applicationDetailSearchItem.setPadInitialReviewApprovedTimestamp(Instant.now());

    var workAreaItem = new ConsultationRequestWorkAreaItem(consultationRequestSearchItem, searchItem -> VIEW_URL);

    assertThat(workAreaItem.getFastTrackLabelText()).isEqualTo(ApplicationTask.FAST_TRACK.getDisplayName() + " accepted");

  }

}