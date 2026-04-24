package uk.co.ogauthority.pwa.features.application.creation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Comparator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.controller.ResolverAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(controllers = StartPwaApplicationController.class)
class StartPwaApplicationControllerTest extends ResolverAbstractControllerTest {

  private static final AuthenticatedUserAccount USER = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

  @ParameterizedTest
  @EnumSource(PwaResourceType.class)
  void renderStartApplication_checkExpectedApplicationTypes(PwaResourceType pwaResourceType) throws Exception {
    var expectedApplicationTypes = pwaResourceType.getPermittedApplicationTypes()
            .stream()
            .filter(type -> type != PwaApplicationType.PIPELINE_RECORD_MANAGEMENT)
            .sorted(Comparator.comparing(PwaApplicationType::getDisplayOrder))
            .toList();

    mockMvc.perform(get(ReverseRouter.route(on(StartPwaApplicationController.class).renderStartApplication(pwaResourceType, null)))
            .with(user(USER)))
        .andExpect(model().attribute("applicationTypes", expectedApplicationTypes));
  }

}
