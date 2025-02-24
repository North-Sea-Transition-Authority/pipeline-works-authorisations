package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

class PipelineControllerRouteUtilsTest {

  private PwaApplicationContext applicationContext;
  private PwaApplicationDetail pwaApplicationDetail;
  private RedirectAttributes redirectAttributes;
  private PadPipeline padPipeline;

  private ModelAndView modelAndViewOk;

  @BeforeEach
  void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    applicationContext = new PwaApplicationContext(pwaApplicationDetail, new WebUserAccount(1), Set.of());
    redirectAttributes = new RedirectAttributesModelMap();

    padPipeline = new PadPipeline();

    applicationContext.setPadPipeline(padPipeline);

    modelAndViewOk = new ModelAndView();
    modelAndViewOk.setStatus(HttpStatus.OK);
  }

  @Test
  void ifAllowedFromOverviewOrRedirect_allowed() {
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var result = PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(
        applicationContext,
        redirectAttributes,
        () -> modelAndViewOk
    );
    assertThat(Objects.requireNonNull(result.getStatus())).isEqualTo(HttpStatus.OK);
  }

  @Test
  void ifAllowedFromOverviewOrRedirect_notAllowed() {
    padPipeline.setPipelineStatus(PipelineStatus.NEVER_LAID);
    var result = PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(
        applicationContext,
        redirectAttributes,
        () -> modelAndViewOk
    );
    assertThat(Objects.requireNonNull(result.getViewName())).startsWith("redirect:");
  }

  @Test
  void ifAllowedFromOverviewOrError_allowed() {
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var result = PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(
        applicationContext,
        () -> modelAndViewOk
    );
    assertThat(Objects.requireNonNull(result.getStatus())).isEqualTo(HttpStatus.OK);
  }

  @Test
  void ifAllowedFromOverviewOrError_notAllowed() {
    padPipeline.setPipelineStatus(PipelineStatus.NEVER_LAID);
    assertThrows(AccessDeniedException.class, () ->
      PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(
          applicationContext,
          () -> modelAndViewOk
      ));
  }

  @Test
  void isAccessible_assertNotAllowed() {
    PipelineControllerRouteUtils.disallowedStatuses.forEach(pipelineStatus ->
      assertThat(PipelineControllerRouteUtils.isAccessible(pipelineStatus)).isFalse());
  }

  @Test
  void isAccessible_assertAllowed() {
    var allowedStatuses = EnumSet.allOf(PipelineStatus.class);
    allowedStatuses.removeAll(PipelineControllerRouteUtils.disallowedStatuses);

    allowedStatuses.forEach(pipelineStatus ->
      assertThat(PipelineControllerRouteUtils.isAccessible(pipelineStatus)).isTrue());
  }
}