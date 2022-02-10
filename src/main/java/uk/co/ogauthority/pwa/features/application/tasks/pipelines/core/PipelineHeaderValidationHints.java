package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.Set;

public class PipelineHeaderValidationHints {

  private final Set<PipelineHeaderQuestion> requiredQuestions;

  public PipelineHeaderValidationHints(Set<PipelineHeaderQuestion> requiredQuestions) {
    this.requiredQuestions = requiredQuestions;
  }

  public Set<PipelineHeaderQuestion> getRequiredQuestions() {
    return requiredQuestions;
  }

}
