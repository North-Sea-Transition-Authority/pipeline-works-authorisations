package uk.co.ogauthority.pwa.controller;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.co.ogauthority.pwa.config.WebMvcConfig;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextArgumentResolver;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.AuthenticatedUserAccountArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaContextArgumentResolver;

@Import({
    WebMvcConfig.class,
    AuthenticatedUserAccountArgumentResolver.class,
})
public abstract class ResolverAbstractControllerTest extends AbstractControllerTest {
  @MockBean
  protected PwaContextArgumentResolver pwaContextArgumentResolver;

  @MockBean
  protected PwaApplicationContextArgumentResolver pwaApplicationContextArgumentResolver;

  @MockBean
  protected PwaAppProcessingContextArgumentResolver pwaAppProcessingContextArgumentResolver;
}