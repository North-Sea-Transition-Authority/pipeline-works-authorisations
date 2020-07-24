package uk.co.ogauthority.pwa.service.validation;

import java.util.Map;

public class SummaryScreenValidationResultTestUtils {

  public SummaryScreenValidationResultTestUtils() {
    throw new AssertionError();
  }

  public static SummaryScreenValidationResult incompleteResult() {
    return new SummaryScreenValidationResult(Map.of(), "", "", false, "");
  }

  public static SummaryScreenValidationResult completeResult() {
    return new SummaryScreenValidationResult(Map.of(), "", "", true, "");
  }

}
