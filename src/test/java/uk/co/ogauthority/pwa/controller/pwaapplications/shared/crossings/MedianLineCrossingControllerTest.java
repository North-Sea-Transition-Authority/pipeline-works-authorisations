package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Map;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.MedianLineAgreementValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MedianLineCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class MedianLineCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {


  @MockBean
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @MockBean
  private MedianLineAgreementValidator medianLineAgreementValidator;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(pwaApplicationDetail);
    when(pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());
  }

  @Test
  public void renderMedianLineForm_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(MedianLineCrossingController.class).renderMedianLineForm(invalidAppType, 1, null, null)))
                    .with(authenticatedUserAndSession(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  public void renderMedianLineForm_authenticated() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(
            on(MedianLineCrossingController.class).renderMedianLineForm(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void renderMedianLineForm_unauthenticated() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(
            on(MedianLineCrossingController.class).renderMedianLineForm(PwaApplicationType.INITIAL, 1, null, null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void postContinueMedianLine_unauthenticated() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Save and complete later", "");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postAddContinueMedianLine(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .params(paramMap))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postCompleteMedianLine_unauthenticated() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Complete", "");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postAddContinueMedianLine(PwaApplicationType.INITIAL, 1, null, null, null, null)))
            .params(paramMap))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postContinueMedianLine() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Save and complete later", "");
      add("agreementStatus", "");
      add("negotiatorNameIfOngoing", "");
      add("negotiatorNameIfCompleted", "");
      add("negotiatorEmailIfOngoing", "");
      add("negotiatorEmailIfCompleted", "");
    }};

    var entity = new PadMedianLineAgreement();
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(entity);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postAddContinueMedianLine(PwaApplicationType.INITIAL, 1, null, null, null, null),
            Map.of("applicationId", 1)))
            .params(paramMap)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andReturn()
        .getModelAndView();
    verify(padMedianLineAgreementService, times(1)).saveEntityUsingForm(eq(entity), any());

  }

  @Test
  public void postCompleteMedianLine() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Complete", "");
      add("agreementStatus", "NOT_CROSSED");
      add("negotiatorNameIfOngoing", "");
      add("negotiatorNameIfCompleted", "");
      add("negotiatorEmailIfOngoing", "");
      add("negotiatorEmailIfCompleted", "");
    }};

    var entity = new PadMedianLineAgreement();
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(entity);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postAddContinueMedianLine(PwaApplicationType.INITIAL, 1, null, null, null, null),
            Map.of("applicationId", 1)))
            .params(paramMap)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andReturn()
        .getModelAndView();
    verify(padMedianLineAgreementService, times(1)).saveEntityUsingForm(eq(entity), any());

  }
}