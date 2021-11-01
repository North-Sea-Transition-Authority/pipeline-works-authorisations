package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;

public class PipelineHeaderValidationHints {

  private final PipelineStatus pipelineStatus;
  private final Boolean validateAlreadyExistsOnSeabedQuestion;

  public PipelineHeaderValidationHints(PipelineStatus pipelineStatus,
                                       Boolean validateAlreadyExistsOnSeabedQuestion) {
    this.pipelineStatus = pipelineStatus;
    this.validateAlreadyExistsOnSeabedQuestion = validateAlreadyExistsOnSeabedQuestion;
  }


  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public Boolean getValidateAlreadyExistsOnSeabedQuestion() {
    return validateAlreadyExistsOnSeabedQuestion;
  }
}
