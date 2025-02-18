package uk.co.ogauthority.pwa.service.workarea.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem.STATUS_LABEL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationWorkAreaItemTest {

  private AsBuiltNotificationWorkAreaItem asBuiltNotificationWorkAreaItem;
  private AsBuiltNotificationWorkareaView asBuiltNotificationWorkareaView;

  private static final String ACCESS_URL = "EXAMPLE_URL";


  @BeforeEach
  void setup() {
    asBuiltNotificationWorkareaView = new AsBuiltNotificationWorkareaView();
    setAsBuiltNotificationWorkAreaViewValues(asBuiltNotificationWorkareaView);
    asBuiltNotificationWorkAreaItem = new AsBuiltNotificationWorkAreaItem(asBuiltNotificationWorkareaView, ACCESS_URL);
  }

  private void setAsBuiltNotificationWorkAreaViewValues(AsBuiltNotificationWorkareaView asBuiltNotificationWorkareaView) {
    asBuiltNotificationWorkareaView.setNgId(1);
    asBuiltNotificationWorkareaView.setPwaId(10);
    asBuiltNotificationWorkareaView.setConsentId(100);
    asBuiltNotificationWorkareaView.setNgReference("NG_REF");
    asBuiltNotificationWorkareaView.setMasterPwaReference("APP_REF");
    asBuiltNotificationWorkareaView.setDeadlineDate(LocalDate.of(2020, Month.APRIL, 1));
    asBuiltNotificationWorkareaView.setPwaHolderNameList(List.of("SHELL"));
    asBuiltNotificationWorkareaView.setStatus(AsBuiltNotificationGroupStatus.COMPLETE);
    asBuiltNotificationWorkareaView.setProjectName("Project name");
    asBuiltNotificationWorkareaView.setProjectCompletionDateTimestamp(LocalDateTime.of(2020, 1, 2, 3, 4, 5)
        .toInstant(ZoneOffset.ofTotalSeconds(0)));
  }

  @Test
  void asBuiltNotificationWorkAreaItem_correctConversionFromView() {
    assertThat(asBuiltNotificationWorkAreaItem.getAsBuiltNotificationGroupId()).isEqualTo(asBuiltNotificationWorkareaView.getNgId());
    assertThat(asBuiltNotificationWorkAreaItem.getAsBuiltNotificationGroupReference()).isEqualTo(asBuiltNotificationWorkareaView.getNgReference());
    assertThat(asBuiltNotificationWorkAreaItem.getProjectName()).isEqualTo(asBuiltNotificationWorkareaView.getProjectName());
    assertThat(asBuiltNotificationWorkAreaItem.getPwaId()).isEqualTo(asBuiltNotificationWorkareaView.getPwaId());
    assertThat(asBuiltNotificationWorkAreaItem.getPwaHolderNameList()).isEqualTo(asBuiltNotificationWorkareaView.getPwaHolderNameList());
    assertThat(asBuiltNotificationWorkAreaItem.getConsentId()).isEqualTo(asBuiltNotificationWorkareaView.getConsentId());
    assertThat(asBuiltNotificationWorkAreaItem.getAccessUrl()).isEqualTo(ACCESS_URL);
  }

  @Test
  void setAsBuiltNotificationWorkAreaItem_correctDisplayDateConversion() {
    assertThat(asBuiltNotificationWorkAreaItem.getAsBuiltNotificationDeadlineDateDisplay()).isEqualTo("01/04/2020");
    assertThat(asBuiltNotificationWorkAreaItem.getProjectCompletionDateDisplay()).isEqualTo("02/01/2020");
  }

  @Test
  void getAsBuiltNotificationStatusColumn() {
    assertThat(asBuiltNotificationWorkAreaItem.getStatusColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            STATUS_LABEL,
            asBuiltNotificationWorkAreaItem.getStatus().getDisplayName())
    );
  }

  @Test
  void getAsBuiltNotificationSummaryColumn() {
    assertThat(asBuiltNotificationWorkAreaItem.getSummaryColumn()).containsExactly(
        WorkAreaColumnItemView.createLabelledItem(
            "Project name", asBuiltNotificationWorkAreaItem.getProjectName()
        ),
        WorkAreaColumnItemView.createLabelledItem(
            "As-built deadline", asBuiltNotificationWorkAreaItem.getAsBuiltNotificationDeadlineDateDisplay()
        ),
        WorkAreaColumnItemView.createLabelledItem(
            "Project completion date", asBuiltNotificationWorkAreaItem.getProjectCompletionDateDisplay()
        ));
  }

  @Test
  void getAsBuiltNotificationHolderColumn() {
    assertThat(asBuiltNotificationWorkAreaItem.getHolderColumn()).containsExactly(
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE, "SHELL"));
  }

  @Test
  void getApplicationColumn() {
    assertThat(asBuiltNotificationWorkAreaItem.getApplicationColumn()).containsExactly(
        WorkAreaColumnItemView.createLinkItem(asBuiltNotificationWorkAreaItem.getAsBuiltNotificationGroupReference(),
            asBuiltNotificationWorkAreaItem.getAccessUrl()
        ),
        WorkAreaColumnItemView.createTagItem(WorkAreaColumnItemView.TagType.NONE,
            asBuiltNotificationWorkAreaItem.getMasterPwaReference())
    );
  }

}