package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@ExtendWith(MockitoExtension.class)
class PipelineHeaderServiceTest {

  @Mock
  private PipelineDetailService pipelineDetailService;

  private PipelineHeaderService pipelineHeaderService;

  @BeforeEach
  void setUp() {
    pipelineHeaderService = new PipelineHeaderService(pipelineDetailService);
  }

  @Test
  void canShowAlreadyExistsOnSeabedQuestions_newPipeline() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);

    var notAllowedTypes = List.of(
        PwaApplicationType.DEPOSIT_CONSENT,
        PwaApplicationType.HUOO_VARIATION);

    PwaApplicationType.stream().forEach(pwaApplicationType -> {

      var requiredQuestions = pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType);

      boolean expected = !notAllowedTypes.contains(pwaApplicationType);

      assertThat(requiredQuestions.contains(PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED)).isEqualTo(expected);

    });

  }

  @Test
  void canShowAlreadyExistsOnSeabedQuestions_consentedPipeline() {

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
  void outOfUseOnSeabedReason_newPipeline() {

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
  void outOfUseOnSeabedReason_consentedPipeline() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);

    PwaApplicationType.stream().forEach(pwaApplicationType ->

      PipelineStatus.stream().forEach(pipelineStatus -> {

        padPipeline.setPipelineStatus(pipelineStatus);

        var requiredQuestions = pipelineHeaderService.getRequiredQuestions(padPipeline, pwaApplicationType);

        boolean expected = pipelineStatus == PipelineStatus.OUT_OF_USE_ON_SEABED;

        assertThat(requiredQuestions.contains(PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON)).isEqualTo(expected);

      }));

  }

  @Test
  void getPipelineHeaderFormContext_pipelineNull() {
    var headerFormContext = pipelineHeaderService.getPipelineHeaderFormContext(null);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.NON_CONSENTED_PIPELINE);
  }

  @Test
  void getPipelineHeaderFormContext_nonConsentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(false);
    var headerFormContext = pipelineHeaderService.getPipelineHeaderFormContext(padPipeline);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.NON_CONSENTED_PIPELINE);
  }

  @Test
  void getPipelineHeaderFormContext_consentedPipeline() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline();
    padPipeline.setPipeline(pipeline);
    when(pipelineDetailService.isPipelineConsented(pipeline)).thenReturn(true);
    var headerFormContext = pipelineHeaderService.getPipelineHeaderFormContext(padPipeline);
    assertThat(headerFormContext).isEqualTo(PipelineHeaderFormContext.CONSENTED_PIPELINE);
  }

}