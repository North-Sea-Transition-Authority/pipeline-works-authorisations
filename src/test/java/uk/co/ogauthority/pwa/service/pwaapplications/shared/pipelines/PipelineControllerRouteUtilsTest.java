package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

public class PipelineControllerRouteUtilsTest {

  private PwaApplicationContext applicationContext;
  private PwaApplicationDetail pwaApplicationDetail;
  private RedirectAttributes redirectAttributes;
  private PadPipeline padPipeline;

  private ModelAndView modelAndViewOk;

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    applicationContext = new PwaApplicationContext(pwaApplicationDetail, new WebUserAccount(1), EnumSet.allOf(
        PwaContactRole.class));
    redirectAttributes = new RedirectAttributesModelMap();

    padPipeline = new PadPipeline();

    applicationContext.setPadPipeline(padPipeline);

    modelAndViewOk = new ModelAndView();
    modelAndViewOk.setStatus(HttpStatus.OK);
  }

  @Test
  public void ifAllowedFromOverviewOrRedirect_allowed() {
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var result = PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(
        applicationContext,
        redirectAttributes,
        () -> modelAndViewOk
    );
    assertThat(Objects.requireNonNull(result.getStatus())).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void ifAllowedFromOverviewOrRedirect_notAllowed() {
    padPipeline.setPipelineStatus(PipelineStatus.NEVER_LAID);
    var result = PipelineControllerRouteUtils.ifAllowedFromOverviewOrRedirect(
        applicationContext,
        redirectAttributes,
        () -> modelAndViewOk
    );
    assertThat(Objects.requireNonNull(result.getViewName())).startsWith("redirect:");
  }

  @Test
  public void ifAllowedFromOverviewOrError_allowed() {
    padPipeline.setPipelineStatus(PipelineStatus.IN_SERVICE);
    var result = PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(
        applicationContext,
        redirectAttributes,
        () -> modelAndViewOk
    );
    assertThat(Objects.requireNonNull(result.getStatus())).isEqualTo(HttpStatus.OK);
  }

  @Test(expected = AccessDeniedException.class)
  public void ifAllowedFromOverviewOrError_notAllowed() {
    padPipeline.setPipelineStatus(PipelineStatus.NEVER_LAID);
    PipelineControllerRouteUtils.ifAllowedFromOverviewOrError(
        applicationContext,
        redirectAttributes,
        () -> modelAndViewOk
    );
  }

  @Test
  public void isAccessible_assertNotAllowed() {
    PipelineControllerRouteUtils.disallowedStatuses.forEach(pipelineStatus -> {
      padPipeline.setPipelineStatus(pipelineStatus);
      assertThat(PipelineControllerRouteUtils.isAccessible(padPipeline)).isFalse();
    });
  }

  @Test
  public void isAccessible_assertAllowed() {
    var allowedStatuses = EnumSet.allOf(PipelineStatus.class);
    allowedStatuses.removeAll(PipelineControllerRouteUtils.disallowedStatuses);

    allowedStatuses.forEach(pipelineStatus -> {
      padPipeline.setPipelineStatus(pipelineStatus);
      assertThat(PipelineControllerRouteUtils.isAccessible(padPipeline)).isTrue();
    });
  }
}