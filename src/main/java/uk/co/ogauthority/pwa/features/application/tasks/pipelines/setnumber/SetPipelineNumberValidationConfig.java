package uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber;


import org.apache.commons.lang3.Range;

public final class SetPipelineNumberValidationConfig {

  private final Range<Integer> pipelineNumberRange;

  private SetPipelineNumberValidationConfig(int minNumber,
                                            int maxNumber) {
    this.pipelineNumberRange = Range.between(minNumber, maxNumber);
  }

  public static SetPipelineNumberValidationConfig rangeCreate(int minNumber,
                                                              int maxNumber) {
    return new SetPipelineNumberValidationConfig(
        minNumber,
        maxNumber
    );
  }

  public Range<Integer> getPipelineNumberRange() {
    return this.pipelineNumberRange;
  }

}
