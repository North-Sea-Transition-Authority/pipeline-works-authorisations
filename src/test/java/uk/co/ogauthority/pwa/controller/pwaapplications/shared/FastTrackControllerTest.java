package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.time.Instant;
import java.time.Period;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadProjectInformationService;
import uk.co.ogauthority.pwa.validators.FastTrackValidator;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = FastTrackController.class)
public class FastTrackControllerTest extends AbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  private PadProjectInformationService padProjectInformationService;

  @SpyBean
  private FastTrackValidator fastTrackValidator;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private PadProjectInformation padProjectInformation;

  @Before
  public void setUp() {
    pwaApplication = new PwaApplication();
    pwaApplication.setApplicationType(PwaApplicationType.INITIAL);
    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setId(1);
    pwaApplicationDetail.setPwaApplication(pwaApplication);
    var wua = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(wua, Set.of());
    padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(Instant.now().plus(Period.ofDays(1)));

    when(pwaApplicationDetailService.withDraftTipDetail(eq(1), eq(user), any())).thenCallRealMethod();
    when(pwaApplicationDetailService.getTipDetailWithStatus(1, PwaApplicationStatus.DRAFT)).thenReturn(pwaApplicationDetail);
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(padProjectInformation);
  }

  @Test
  public void authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
    ).andExpect(status().isOk());


    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postCompleteFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(completeParams)
    ).andExpect(status().isOk());

    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postContinueFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(continueParams)
    ).andExpect(status().is3xxRedirection());
  }

  @Test
  public void unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null)))
    ).andExpect(status().is3xxRedirection());


    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postCompleteFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(completeParams)
    ).andExpect(status().isForbidden());

    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postContinueFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(continueParams)
    ).andExpect(status().isForbidden());
  }

  @Test
  public void renderFastTrack() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
    ).andExpect(status().isOk())
    .andExpect(view().name("pwaApplication/shared/fastTrack"));
  }

  @Test
  public void postCompleteFastTrack_EmptyData() throws Exception {
    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "");
      add("avoidEnvironmentalDisaster", null);
      add("environmentalDisasterReason", null);
      add("savingBarrels", null);
      add("savingBarrelsReason", null);
      add("projectPlanning", null);
      add("projectPlanningReason", null);
      add("hasOtherReason", null);
      add("otherReason", null);
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postContinueFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(completeParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().isOk());
    verify(padFastTrackService, never()).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postCompleteFastTrack_WithData() throws Exception {
    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add("Complete", "");
      add("avoidEnvironmentalDisaster", "true");
      add("environmentalDisasterReason", "reason");
      add("savingBarrels", "true");
      add("savingBarrelsReason", "reason");
      add("projectPlanning", "true");
      add("projectPlanningReason", "reason");
      add("hasOtherReason", "true");
      add("otherReason", "reason");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postCompleteFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(completeParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
    verify(padFastTrackService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postContinueFastTrack_EmptyData() throws Exception {
    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "");
      add("avoidEnvironmentalDisaster", null);
      add("environmentalDisasterReason", null);
      add("savingBarrels", null);
      add("savingBarrelsReason", null);
      add("projectPlanning", null);
      add("projectPlanningReason", null);
      add("hasOtherReason", null);
      add("otherReason", null);
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postContinueFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(continueParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
    verify(padFastTrackService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postContinueFastTrack_WithData() throws Exception {
    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add("Save and complete later", "");
      add("avoidEnvironmentalDisaster", "true");
      add("environmentalDisasterReason", "reason");
      add("savingBarrels", "true");
      add("savingBarrelsReason", "reason");
      add("projectPlanning", "true");
      add("projectPlanningReason", "reason");
      add("hasOtherReason", "true");
      add("otherReason", "reason");
    }};
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postContinueFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .params(continueParams)
            .with(authenticatedUserAndSession(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
    verify(padFastTrackService, times(1)).saveEntityUsingForm(any(), any());
  }
}