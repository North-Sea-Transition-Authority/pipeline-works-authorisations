package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A list of questions that require certain conditions to be met if they are to be shown onscreen/answered by
 * applicants. Not an exhaustive list of all pipeline header questions as most of them apply in all cases.
 */
public enum PipelineHeaderQuestion {

  OUT_OF_USE_ON_SEABED_REASON,

  ALREADY_EXISTS_ON_SEABED;

  public static Stream<PipelineHeaderQuestion> stream() {
    return Arrays.stream(PipelineHeaderQuestion.values());
  }

}
