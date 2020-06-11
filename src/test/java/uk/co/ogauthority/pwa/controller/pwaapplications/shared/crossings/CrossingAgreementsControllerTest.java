package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CableCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.MedianLineCrossingFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PadPipelineCrossingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingFileService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CrossingAgreementsController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class CrossingAgreementsControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final int APP_ID = 1;
  private static final int APP_DETAIL_ID = 10;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @MockBean
  private BlockCrossingService blockCrossingService;

  @MockBean
  private BlockCrossingFileService blockCrossingFileService;

  @MockBean
  private CrossingAgreementsService crossingAgreementsService;

  @MockBean
  private MedianLineCrossingFileService medianLineCrossingFileService;

  @MockBean
  private PadCableCrossingService cableCrossingService;

  @MockBean
  private CableCrossingFileService cableCrossingFileService;

  @MockBean
  private PadPipelineCrossingService padPipelineCrossingService;

  @MockBean
  private PipelineCrossingFileService pipelineCrossingFileService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PwaApplicationContext pwaApplicationContext;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
        pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var userRoles = EnumSet.allOf(PwaContactRole.class);
    var wua = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(wua, Set.of());

    // support application context service
    when(pwaContactService.getContactRoles(eq(pwaApplicationDetail.getPwaApplication()), any())).thenReturn(userRoles);
    when(pwaApplicationDetailService.getTipDetail(APP_ID)).thenReturn(pwaApplicationDetail);
    when(crossingAgreementsService.getValidationResult(pwaApplicationDetail)).thenReturn(new CrossingAgreementsValidationResult(Set.of()));
  }

  @Test
  public void renderCrossingAgreementsOverview_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(PwaApplicationType.INITIAL, APP_ID, null, null)))
    ).andExpect(status().is3xxRedirection());
  }

  @Test
  public void renderCrossingAgreementsOverview() throws Exception {
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    entity.setNegotiatorName("Name");
    entity.setNegotiatorEmail("Email");
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(entity);

    mockMvc.perform(
        get(ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(PwaApplicationType.INITIAL, APP_ID, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }
}