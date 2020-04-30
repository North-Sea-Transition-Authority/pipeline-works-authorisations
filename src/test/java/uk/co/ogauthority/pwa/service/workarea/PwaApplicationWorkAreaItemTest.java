package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationWorkAreaItemTest {

  private PwaApplicationWorkAreaItem pwaApplicationWorkAreaItem;
  private ApplicationDetailSearchItem applicationDetailSearchItem;

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


  }


  @Test
  public void pwaApplicationWorkAreaItem() {
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(applicationDetailSearchItem, searchItem -> VIEW_URL);

    assertThat(pwaApplicationWorkAreaItem.getPwaApplicationId())
        .isEqualTo(applicationDetailSearchItem.getPwaApplicationId());
    assertThat(pwaApplicationWorkAreaItem.getApplicationType())
        .isEqualTo(applicationDetailSearchItem.getApplicationType().getDisplayName());
    assertThat(pwaApplicationWorkAreaItem.getOrderedFieldList()).isEqualTo(List.of("FIELD1", "FIELD2"));
    assertThat(pwaApplicationWorkAreaItem.getMasterPwaReference())
        .isEqualTo(applicationDetailSearchItem.getPwaReference());
    assertThat(pwaApplicationWorkAreaItem.getPadReference()).isEqualTo(applicationDetailSearchItem.getPadReference());
    assertThat(pwaApplicationWorkAreaItem.getViewApplicationUrl()).isEqualTo(VIEW_URL);
    assertThat(pwaApplicationWorkAreaItem.getPadDisplayStatus())
        .isEqualTo(applicationDetailSearchItem.getPadStatus().getDisplayName());

    assertThat(pwaApplicationWorkAreaItem.getMasterPwaReference()).isEqualTo("PWA_REF");
    assertThat(pwaApplicationWorkAreaItem.getPadReference()).isEqualTo("PAD_REF");

    assertThat(pwaApplicationWorkAreaItem.getProjectName()).isEqualTo("PROJECT_NAME");
    assertThat(pwaApplicationWorkAreaItem.getPadStatusSetInstant())
        .isEqualTo(applicationDetailSearchItem.getPadStatusTimestamp());
    assertThat(pwaApplicationWorkAreaItem.getPadStatusSetInstant())
        .isEqualTo(applicationDetailSearchItem.getPadStatusTimestamp());

    assertThat(pwaApplicationWorkAreaItem.getIsTipFlag()).isEqualTo(applicationDetailSearchItem.isTipFlag());

  }


  @Test
  public void getFormattedProposedStartDate_whenSet() {
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(applicationDetailSearchItem, searchItem -> VIEW_URL);
    assertThat(pwaApplicationWorkAreaItem.getFormattedProposedStartDate()).isEqualTo("02/01/2020");
  }

  @Test
  public void getFormattedProposedStartDate_whenNull() {
    applicationDetailSearchItem.setPadProposedStart(null);
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(applicationDetailSearchItem, searchItem -> VIEW_URL);
    assertThat(pwaApplicationWorkAreaItem.getFormattedProposedStartDate()).isNull();
  }

  @Test
  public void getFormattedStatusSetDatetime_whenSet() {
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(applicationDetailSearchItem, searchItem -> VIEW_URL);
    assertThat(pwaApplicationWorkAreaItem.getFormattedStatusSetDatetime()).isEqualTo("03/02/2020 04:05");
  }

  @Test
  public void getFormattedStatusSetDatetime_whenNull() {
    applicationDetailSearchItem.setPadStatusTimestamp(null);
    pwaApplicationWorkAreaItem = new PwaApplicationWorkAreaItem(applicationDetailSearchItem, searchItem -> VIEW_URL);
    assertThat(pwaApplicationWorkAreaItem.getFormattedStatusSetDatetime()).isNull();
  }
}