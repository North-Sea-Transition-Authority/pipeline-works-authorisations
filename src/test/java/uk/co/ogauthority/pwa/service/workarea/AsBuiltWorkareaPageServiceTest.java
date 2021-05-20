package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltWorkareaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  private AsBuiltWorkareaPageService asBuiltWorkareaPageService;

  @Before
  public void setup() {
    asBuiltWorkareaPageService = new AsBuiltWorkareaPageService();
  }

  @Test
  public void getAsBuiltNotificationsPageView_zeroResults() {

    var workareaPage = asBuiltWorkareaPageService.getAsBuiltNotificationsPageView(REQUESTED_PAGE);

    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

  }

}
