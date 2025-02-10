package uk.co.ogauthority.pwa.features.application.tasks.fasttrack.controller;

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
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.time.Period;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.FastTrackForm;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = FastTrackController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class FastTrackControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadFastTrackService padFastTrackService;

  @MockBean
  private PadProjectInformationService padProjectInformationService;

  private PwaApplication pwaApplication;
  private PwaApplicationDetail pwaApplicationDetail;
  private AuthenticatedUserAccount user;
  private PadProjectInformation padProjectInformation;

  @Before
  public void setUp() {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1);
    pwaApplication = pwaApplicationDetail.getPwaApplication();

    var wua = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(wua, Set.of());
    padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(Instant.now().plus(Period.ofDays(1)));

    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(padProjectInformation);
    when(padFastTrackService.isFastTrackRequired(pwaApplicationDetail)).thenReturn(true);

    //support app context code
    when(pwaApplicationDetailService.getTipDetailByAppId(1)).thenReturn(pwaApplicationDetail);
    // by default has all roles
    when(pwaApplicationPermissionService.getPermissions(eq(pwaApplicationDetail), any())).thenReturn(EnumSet.allOf(PwaApplicationPermission.class));
  }

  @Test
  public void authenticated_renderFastTrack() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
    ).andExpect(status().isOk());
  }

  @Test
  public void authenticated_postComplete() throws Exception {

    ControllerTestUtils.failValidationWhenPost(padFastTrackService, new FastTrackForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams())
    ).andExpect(status().isOk());
  }

  @Test
  public void authenticated_postContinue() throws Exception {

    ControllerTestUtils.passValidationWhenPost(padFastTrackService, new FastTrackForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.partialValidationPostParams())
    ).andExpect(status().is3xxRedirection());
  }

  @Test
  public void unauthenticated_renderFastTrack() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
    ).andExpect(status().is3xxRedirection());
  }

  @Test
  public void unauthenticated_postComplete() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .params(ControllerTestUtils.fullValidationPostParams())
    ).andExpect(status().isForbidden());
  }

  @Test
  public void unauthenticated_postContinue() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .params(ControllerTestUtils.partialValidationPostParams())
    ).andExpect(status().isForbidden());
  }

  @Test
  public void fastTrackNotAllowed() throws Exception {
    when(padFastTrackService.isFastTrackRequired(pwaApplicationDetail)).thenReturn(false);
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
    ).andExpect(status().isForbidden());

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.fullValidationPostParams())
    ).andExpect(status().isForbidden());

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .with(user(user))
            .with(csrf())
            .params(ControllerTestUtils.partialValidationPostParams())
    ).andExpect(status().isForbidden());
  }

  @Test
  public void renderFastTrack() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(user(user))
    ).andExpect(status().isOk())
    .andExpect(view().name("pwaApplication/shared/fastTrack"));
  }

  @Test
  public void postCompleteFastTrack_EmptyData() throws Exception {

    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());
      add("avoidEnvironmentalDisaster", null);
      add("environmentalDisasterReason", null);
      add("savingBarrels", null);
      add("savingBarrelsReason", null);
      add("projectPlanning", null);
      add("projectPlanningReason", null);
      add("hasOtherReason", null);
      add("otherReason", null);
    }};

    ControllerTestUtils.failValidationWhenPost(padFastTrackService, new FastTrackForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .params(completeParams)
            .with(user(user))
            .with(csrf())
    ).andExpect(status().isOk());
    verify(padFastTrackService, never()).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postCompleteFastTrack_WithData() throws Exception {
    MultiValueMap<String, String> completeParams = new LinkedMultiValueMap<>() {{
      add(ValidationType.FULL.getButtonText(), ValidationType.FULL.getButtonText());
      add("avoidEnvironmentalDisaster", "true");
      add("environmentalDisasterReason", "reason");
      add("savingBarrels", "true");
      add("savingBarrelsReason", "reason");
      add("projectPlanning", "true");
      add("projectPlanningReason", "reason");
      add("hasOtherReason", "true");
      add("otherReason", "reason");
    }};

    ControllerTestUtils.passValidationWhenPost(padFastTrackService, new FastTrackForm(), ValidationType.FULL);

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .params(completeParams)
            .with(user(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
    verify(padFastTrackService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postFastTrack_EmptyData() throws Exception {
    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add(ValidationType.PARTIAL.getButtonText(), ValidationType.PARTIAL.getButtonText());
      add("avoidEnvironmentalDisaster", null);
      add("environmentalDisasterReason", null);
      add("savingBarrels", null);
      add("savingBarrelsReason", null);
      add("projectPlanning", null);
      add("projectPlanningReason", null);
      add("hasOtherReason", null);
      add("otherReason", null);
    }};

    ControllerTestUtils.passValidationWhenPost(padFastTrackService, new FastTrackForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .params(continueParams)
            .with(user(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
    verify(padFastTrackService, times(1)).saveEntityUsingForm(any(), any());
  }

  @Test
  public void postFastTrack_WithData() throws Exception {
    MultiValueMap<String, String> continueParams = new LinkedMultiValueMap<>() {{
      add(ValidationType.PARTIAL.getButtonText(), ValidationType.PARTIAL.getButtonText());
      add("avoidEnvironmentalDisaster", "true");
      add("environmentalDisasterReason", "reason");
      add("savingBarrels", "true");
      add("savingBarrelsReason", "reason");
      add("projectPlanning", "true");
      add("projectPlanningReason", "reason");
      add("hasOtherReason", "true");
      add("otherReason", "reason");
    }};

    ControllerTestUtils.passValidationWhenPost(padFastTrackService, new FastTrackForm(), ValidationType.PARTIAL);

    mockMvc.perform(
        post(ReverseRouter.route(on(FastTrackController.class)
            .postFastTrack(PwaApplicationType.INITIAL, 1, null, null, null, null, null)))
            .params(continueParams)
            .with(user(user))
            .with(csrf())
    ).andExpect(status().is3xxRedirection());
    verify(padFastTrackService, times(1)).saveEntityUsingForm(any(), any());
  }
}
