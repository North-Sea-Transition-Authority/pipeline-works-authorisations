package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

@Service
public class PipelineHeaderService {

  private final PipelineDetailService pipelineDetailService;

  private static final List<PwaApplicationType> ALREADY_EXISTS_APP_TYPES = List.of(
      PwaApplicationType.INITIAL,
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.DECOMMISSIONING);

  @Autowired
  public PipelineHeaderService(PipelineDetailService pipelineDetailService) {
    this.pipelineDetailService = pipelineDetailService;
  }

  public Set<PipelineHeaderQuestion> getRequiredQuestions(PadPipeline padPipeline,
                                                          PwaApplicationType pwaApplicationType) {

    var pipelineHeaderFormContext = getPipelineHeaderFormContext(padPipeline);

    return PipelineHeaderQuestion.stream()
        .filter(question -> {

          if (question == PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON) {
            return pipelineHeaderFormContext == PipelineHeaderFormContext.CONSENTED_PIPELINE
                && padPipeline.getPipelineStatus() == PipelineStatus.OUT_OF_USE_ON_SEABED;
          }

          if (question == PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED) {
            return ALREADY_EXISTS_APP_TYPES.contains(pwaApplicationType)
                && pipelineHeaderFormContext == PipelineHeaderFormContext.NON_CONSENTED_PIPELINE;
          }

          return false;

        })
        .collect(Collectors.toSet());

  }

  PipelineHeaderFormContext getPipelineHeaderFormContext(PadPipeline padPipeline) {

    if (padPipeline == null || !pipelineDetailService.isPipelineConsented(padPipeline.getPipeline())) {
      return PipelineHeaderFormContext.NON_CONSENTED_PIPELINE;
    }

    return PipelineHeaderFormContext.CONSENTED_PIPELINE;

  }

  public PipelineHeaderValidationHints getValidationHints(PadPipeline padPipeline,
                                                          PwaApplicationType pwaApplicationType) {
    return new PipelineHeaderValidationHints(getRequiredQuestions(padPipeline, pwaApplicationType));
  }

}
