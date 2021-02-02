package uk.co.ogauthority.pwa.controller.search.consents;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.testutils.PwaEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaViewController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {PwaContextService.class}))
public class PwaViewControllerTest extends PwaContextAbstractControllerTest {

  private PwaEndpointTestBuilder endpointTester;

  private MasterPwa masterPwa;
  private AuthenticatedUserAccount user;

  @MockBean
  protected PwaPermissionService pwaPermissionService;


  @Before
  public void setUp() {

    endpointTester = new PwaEndpointTestBuilder(mockMvc, masterPwaManagementService, pwaPermissionService, consentSearchService)
        .setAllowedProcessingPermissions(PwaPermission.VIEW_PWA);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        Set.of(PwaUserPrivilege.PWA_REGULATOR));

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);
    this.masterPwa.setCreatedTimestamp(Instant.MIN);
    when(masterPwaManagementService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);

    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of(PwaPermission.VIEW_PWA));

  }



  @Test
  public void renderViewPwa_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(PwaViewController.class)
                .renderViewPwa(1, null, null)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

}