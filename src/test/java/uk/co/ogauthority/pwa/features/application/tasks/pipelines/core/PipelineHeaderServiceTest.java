package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@RunWith(MockitoJUnitRunner.class)
public class PipelineHeaderServiceTest {

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PipelineHeaderService pipelineHeaderService;

  @Before
  public void setUp() {
    pipelineHeaderService = new PipelineHeaderService(pipelineDetailService);
  }

  @Test
  public void canShowAlreadyExistsOnSeabedQuestions_newPipeline() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);

    var notAllowedTypes = List.of(
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.HUOO_VARIATION);

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      var requiredQuestions = pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType);

      boolean expected = !notAllowedTypes.contains(pwaApplicationType);

      assertThat(requiredQuestions.contains(PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED)).isEqualTo(expected);

    });

  }

  @Test
  public void canShowAlreadyExistsOnSeabedQuestions_consentedPipeline() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      var requiredQuestions = pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType);

      boolean expected = false;

      assertThat(requiredQuestions.contains(PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED)).isEqualTo(expected);

    });

  }

  @Test
  public void outOfUseOnSeabedReason_newPipeline() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      var requiredQuestions = pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType);

      boolean expected = false;

      assertThat(requiredQuestions.contains(PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON)).isEqualTo(expected);

    });

  }

  @Test
  public void outOfUseOnSeabedReason_consentedPipeline() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      PipelineStatus.stream().forEach(pipelineStatus -> {

        padPipeline.setPipelineStatus(pipelineStatus);

        var requiredQuestions = pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType);

        boolean expected = pipelineStatus == PipelineStatus.OUT_OF_USE_ON_SEABED;

        assertThat(requiredQuestions.contains(PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON)).isEqualTo(expected);

      });

    });

  }

  @Test
  public void getPipelineHeaderFormContext_pipelineNull() {
    var headerFormContext = pipelineHeaderService.getPipelineHeaderFormContext(null);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.NON_CONSENTED_PIPELINE);
  }

  @Test
  public void getPipelineHeaderFormContext_nonConsentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);
    var headerFormContext = pipelineHeaderService.getPipelineHeaderFormContext(padPipeline);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.NON_CONSENTED_PIPELINE);
  }

  @Test
  public void getPipelineHeaderFormContext_consentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);
    var headerFormContext = pipelineHeaderService.getPipelineHeaderFormContext(padPipeline);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.CONSENTED_PIPELINE);
  }

}