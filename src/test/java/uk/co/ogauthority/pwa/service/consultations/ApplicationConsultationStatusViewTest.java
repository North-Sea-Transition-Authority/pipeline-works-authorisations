package uk.co.ogauthority.pwa.service.consultations;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationConsultationStatusViewTest {

  private Map<ConsultationRequestStatus, Long> consultationRequestStatusLongMap;

  @Before
  public void setUp() throws Exception {

    consultationRequestStatusLongMap = new HashMap<>();

    Arrays.stream(ConsultationRequestStatus.values())
        .forEach(consultationRequestStatus -> consultationRequestStatusLongMap.put(consultationRequestStatus, 1L));
  }

  @Test
  public void getCountOfRequestsWithStatus_whenAllStatusesInConstructionMap() {

    var applicationStatusView = new ApplicationConsultationStatusView(consultationRequestStatusLongMap);

    for (ConsultationRequestStatus status : ConsultationRequestStatus.values()) {
      try {
        assertThat(applicationStatusView.getCountOfRequestsWithStatus(status)).isEqualTo(1L);
      } catch (AssertionError e) {
        throw new AssertionError("Failed for status:" + status, e);
      }
    }
  }

  @Test
  public void getCountOfRequestsWithStatus_whenEmptyMap() {

    var applicationStatusView = new ApplicationConsultationStatusView(new HashMap<>());

    for (ConsultationRequestStatus status : ConsultationRequestStatus.values()) {
      try {
        assertThat(applicationStatusView.getCountOfRequestsWithStatus(status)).isEqualTo(0L);
      } catch (AssertionError e) {
        throw new AssertionError("Failed for status:" + status, e);
      }
    }

  }

  @Test
  public void sumFilteredStatusCounts_whenEmptyMapConstruction_andNoStatusFiltered() {

    var applicationStatusView = new ApplicationConsultationStatusView(new HashMap<>());

    assertThat(applicationStatusView.sumFilteredStatusCounts(Objects::nonNull)).isEqualTo(0L);
  }

  @Test
  public void sumFilteredStatusCounts_whenFullConstructionMap_andNoStatusFiltered() {

    var applicationStatusView = new ApplicationConsultationStatusView(consultationRequestStatusLongMap);

    assertThat(applicationStatusView.sumFilteredStatusCounts(Objects::nonNull)).isEqualTo(4L);
  }

  @Test
  public void sumFilteredStatusCounts_whenFullConstructionMap_andStatusFiltered() {

    var applicationStatusView = new ApplicationConsultationStatusView(consultationRequestStatusLongMap);

    assertThat(applicationStatusView.sumFilteredStatusCounts(
        consultationRequestStatus -> consultationRequestStatus.equals(ConsultationRequestStatus.RESPONDED)
    )).isEqualTo(1L);
  }


  @Test(expected = UnsupportedOperationException.class)
  public void getStatusCounts_immutableMap() {

    var applicationStatusView = new ApplicationConsultationStatusView(consultationRequestStatusLongMap);
    var map = applicationStatusView.getStatusCounts();
    map.put(ConsultationRequestStatus.RESPONDED, 1000L);
  }
}