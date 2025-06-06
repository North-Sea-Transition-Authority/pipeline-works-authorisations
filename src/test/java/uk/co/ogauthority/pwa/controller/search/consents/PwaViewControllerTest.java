package uk.co.ogauthority.pwa.controller.search.consents;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.ogauthority.pwa.controller.PwaContextAbstractControllerTest;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.testutils.PwaEndpointTestBuilder;

@WebMvcTest(controllers = PwaViewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaContextService.class}))
class PwaViewControllerTest extends PwaContextAbstractControllerTest {

  private PwaEndpointTestBuilder endpointTester;

  @MockBean
  protected PwaPermissionService pwaPermissionService;

  @MockBean
  protected PwaViewTabService pwaViewTabService;

  @BeforeEach
  void setUp() throws SQLException {

    endpointTester = new PwaEndpointTestBuilder(mockMvc, masterPwaService, pwaPermissionService, consentSearchService)
        .setAllowedProcessingPermissions(PwaPermission.VIEW_PWA);

  }

  @Test
  void renderViewPwa_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaViewController.class)
                .renderViewPwa(1, PwaViewTab.PIPELINES, null, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

}