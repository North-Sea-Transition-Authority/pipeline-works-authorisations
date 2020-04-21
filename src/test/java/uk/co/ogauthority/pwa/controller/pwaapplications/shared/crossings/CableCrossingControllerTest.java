package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.PadCableCrossingService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CableCrossingController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class CableCrossingControllerTest extends PwaApplicationContextAbstractControllerTest {

  private PwaApplicationDetail pwaApplicationDetail;
  private EnumSet<PwaApplicationType> allowedApplicationTypes;
  private AuthenticatedUserAccount user;

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private PadCableCrossingService padCableCrossingService;

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
  public void renderAddCableCrossing_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(CableCrossingController.class).renderAddCableCrossing(invalidAppType, 1, null, null)))
                    .with(authenticatedUserAndSession(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  public void renderEditCableCrossing_authenticated_invalidAppType() {

    PwaApplicationType.stream()
        .filter(t -> !allowedApplicationTypes.contains(t))
        .forEach(invalidAppType -> {
          pwaApplicationDetail.getPwaApplication().setApplicationType(invalidAppType);
          try {
            mockMvc.perform(
                get(ReverseRouter.route(
                    on(CableCrossingController.class).renderEditCableCrossing(invalidAppType, 1, 1, null, null)))
                    .with(authenticatedUserAndSession(user))
                    .with(csrf()))
                .andExpect(status().isForbidden());
          } catch (Exception e) {

            throw new AssertionError("Fail at: " + invalidAppType + "\n" + e.getMessage(), e);

          }

        });
  }

  @Test
  public void renderAddCableCrossing_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderAddCableCrossing(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void renderEditCableCrossing_authenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  public void renderAddCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderAddCableCrossing(PwaApplicationType.INITIAL, 1, null, null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void renderEditCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void postAddCableCrossings_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postAddCableCrossings(PwaApplicationType.INITIAL, 1, null, null, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postEditCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null,
                null))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void postRemoveCableCrossing_unauthenticated() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postRemoveCableCrossing(PwaApplicationType.INITIAL, 1, 1, null))))
        .andExpect(status().isForbidden());
  }

  @Test
  public void renderAddCableCrossing() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderAddCableCrossing(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/crossings/addCableCrossing"));
  }

  @Test
  public void postAddCableCrossings_invalid() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postAddCableCrossings(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
    verify(padCableCrossingService, never()).createCableCrossing(eq(pwaApplicationDetail), any());
  }

  @Test
  public void postAddCableCrossings_valid() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("cableName", "abc");
      add("cableOwner", "def");
      add("location", "ghi");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postAddCableCrossings(PwaApplicationType.INITIAL, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(paramMap))
        .andExpect(status().is3xxRedirection());
    verify(padCableCrossingService, times(1)).createCableCrossing(eq(pwaApplicationDetail), any());
  }

  @Test
  public void renderEditCableCrossing() throws Exception {

    var cableCrossing = new PadCableCrossing();
    when(padCableCrossingService.getCableCrossing(pwaApplicationDetail, 1)).thenReturn(cableCrossing);

    mockMvc.perform(
        get(ReverseRouter.route(
            on(CableCrossingController.class).renderEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("pwaApplication/shared/crossings/editCableCrossing"));
    verify(padCableCrossingService, times(1)).mapCrossingToForm(eq(cableCrossing), any());
  }

  @Test
  public void postEditCableCrossing_invalid() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null,
                null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().isOk());
    verify(padCableCrossingService, never()).updateCableCrossing(eq(pwaApplicationDetail), eq(1), any());
  }

  @Test
  public void postEditCableCrossing_valid() throws Exception {

    MultiValueMap paramMap = new LinkedMultiValueMap<String, String>() {{
      add("cableName", "abc");
      add("cableOwner", "def");
      add("location", "ghi");
    }};

    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class)
                .postEditCableCrossing(PwaApplicationType.INITIAL, 1, 1, null, null, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf())
            .params(paramMap))
        .andExpect(status().is3xxRedirection());
    verify(padCableCrossingService, times(1)).updateCableCrossing(eq(pwaApplicationDetail), eq(1), any());
  }

  @Test
  public void postRemoveCableCrossing() throws Exception {
    mockMvc.perform(
        post(ReverseRouter.route(
            on(CableCrossingController.class).postRemoveCableCrossing(PwaApplicationType.INITIAL, 1, 1, null)))
            .with(authenticatedUserAndSession(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());
    verify(padCableCrossingService, times(1)).removeCableCrossing(eq(pwaApplicationDetail), eq(1));
  }
}