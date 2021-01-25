package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;

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
