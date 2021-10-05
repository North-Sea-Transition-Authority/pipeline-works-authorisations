package uk.co.ogauthority.pwa.util.pipelines;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineNumberSortingUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineNumberSortingUtil.class);


  private static String getPipelineNumberOnlyFromReference(String pipelineNumber) {

    StringBuilder pipelineNumberValue = new StringBuilder();
    for (Character character: pipelineNumber.toCharArray()) {

      if (Character.isDigit(character) || character.equals('.')) {
        pipelineNumberValue.append(character);

      } else if (pipelineNumberValue.length() > 0) {
        break;
      }
    }

    return pipelineNumberValue.toString();
  }

  private static String getPipelineNumberWithoutPrefix(String pipelineNumber) {

    return pipelineNumber.replace("PLU", "")
        .replace("PL", "")
        .trim();
  }



  public static int compare(String firstPipelineNumber, String secondPipelineNumber) {

    try {
      var pipelineNumberValue = new BigDecimal(getPipelineNumberOnlyFromReference(firstPipelineNumber));
      var comparingPipelineNumberValue = new BigDecimal(getPipelineNumberOnlyFromReference(secondPipelineNumber));
      var comparisonResult = pipelineNumberValue.compareTo(comparingPipelineNumberValue);

      if (comparisonResult == 0) {
        //the pipeline numeric values are equal, need to compare by their suffix
        return getPipelineNumberWithoutPrefix(firstPipelineNumber).compareTo(getPipelineNumberWithoutPrefix(secondPipelineNumber));
      }

      return comparisonResult;

    } catch (NumberFormatException e) {

      LOGGER.error("Could not extract a numeric value from the pipeline numbers: {} and {}. " +
              "Falling back to default String comparison of the pipeline number",
          firstPipelineNumber, secondPipelineNumber);

      return firstPipelineNumber.compareTo(secondPipelineNumber);
    }

  }


}
