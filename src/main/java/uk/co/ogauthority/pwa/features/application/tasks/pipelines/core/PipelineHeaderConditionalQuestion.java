package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;

public enum PipelineHeaderConditionalQuestion {

  OUT_OF_USE_ON_SEABED_REASON(PipelineStatus.OUT_OF_USE_ON_SEABED);

  private final PipelineStatus appliesToPipelineStatus;

  PipelineHeaderConditionalQuestion(PipelineStatus status) {
    this.appliesToPipelineStatus = status;
  }


  public String getDisplayText() {
    return appliesToPipelineStatus.getDisplayText();
  }

  /**
   * get all pipeline header conditional questions that apply to pipelines with the given pipeline status.
   */
  public static Set<PipelineHeaderConditionalQuestion> getQuestionsForStatus(PipelineStatus status) {
    return Arrays.stream(PipelineHeaderConditionalQuestion.values())
        .filter(questionStatus -> questionStatus.appliesToPipelineStatus.equals(status))
        .collect(Collectors.toSet());
  }

}