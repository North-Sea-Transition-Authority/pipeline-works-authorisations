package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.validators.MedianLineAgreementValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = MedianLineCrossingController.class)
public class MedianLineCrossingControllerTest extends AbstractControllerTest {

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
    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, 1);
    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);
    allowedApplicationTypes = EnumSet.of(
        PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.CAT_2_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.OPTIONS_VARIATION);
    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());
  }

  @Test
  public void renderMedianLineForm_authenticated_invalidAppType() {

    when(pwaApplicationContextService.getApplicationContext(any(), any(), anySet(), any(), any()))
        .thenThrow(AccessDeniedException.class);

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {

          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(MedianLineCrossingController.class).renderMedianLineForm(invalidAppType, null, null, null),
                    Map.of("applicationId", 1)))
                    .with(authenticatedUserAndSession(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {
            if (!(e instanceof AccessDeniedException)) {
              throw new AssertionError();
            }
          }

        });

  }

  @Test
  public void renderMedianLineForm_authenticated() throws Exception {

    var applicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any())).thenReturn(applicationContext);

    mockMvc.perform(
        get(ReverseRouter.route(
            on(MedianLineCrossingController.class).renderMedianLineForm(PwaApplicationType.INITIAL, null, null, null),
            Map.of("applicationId", 1)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void renderMedianLineForm_unauthenticated() throws Exception {

    var applicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any())).thenReturn(applicationContext);

    mockMvc.perform(
        get(ReverseRouter.route(
            on(MedianLineCrossingController.class).renderMedianLineForm(PwaApplicationType.INITIAL, null, null, null),
            Map.of("applicationId", 1))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void postContinueMedianLine_unauthenticated() throws Exception {

    var applicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any())).thenReturn(applicationContext);

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Save and complete later", "");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postContinueMedianLine(PwaApplicationType.INITIAL, null, null, null, null),
            Map.of("applicationId", 1)))
            .params(paramMap))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postCompleteMedianLine_unauthenticated() throws Exception {

    var applicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any())).thenReturn(applicationContext);

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Complete", "");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postCompleteMedianLine(PwaApplicationType.INITIAL, null, null, null, null),
            Map.of("applicationId", 1)))
            .params(paramMap))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postContinueMedianLine() throws Exception {
    var applicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any())).thenReturn(applicationContext);

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Save and complete later", "");
      add("agreementStatus", "");
      add("negotiatorNameIfOngoing", "");
      add("negotiatorNameIfCompleted", "");
      add("negotiatorEmailIfOngoing", "");
      add("negotiatorEmailIfCompleted", "");
    }};

    var entity = new PadMedianLineAgreement();
    when(padMedianLineAgreementService.getMedianLineAgreementForDraft(pwaApplicationDetail)).thenReturn(entity);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postContinueMedianLine(PwaApplicationType.INITIAL, null, null, null, null),
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
    var applicationContext = new PwaApplicationContext(pwaApplicationDetail, user, Set.of());

    when(pwaApplicationContextService.getApplicationContext(eq(1), eq(user), any(), eq(PwaApplicationStatus.DRAFT),
        any())).thenReturn(applicationContext);

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("Complete", "");
      add("agreementStatus", "NOT_CROSSED");
      add("negotiatorNameIfOngoing", "");
      add("negotiatorNameIfCompleted", "");
      add("negotiatorEmailIfOngoing", "");
      add("negotiatorEmailIfCompleted", "");
    }};

    var entity = new PadMedianLineAgreement();
    when(padMedianLineAgreementService.getMedianLineAgreementForDraft(pwaApplicationDetail)).thenReturn(entity);

    mockMvc.perform(
        post(ReverseRouter.route(
            on(MedianLineCrossingController.class)
                .postContinueMedianLine(PwaApplicationType.INITIAL, null, null, null, null),
            Map.of("applicationId", 1)))
            .params(paramMap)
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andReturn()
        .getModelAndView();
    verify(padMedianLineAgreementService, times(1)).saveEntityUsingForm(eq(entity), any());

  }
}