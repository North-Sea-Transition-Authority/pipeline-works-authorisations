package uk.co.ogauthority.pwa.util.pipelines;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineNumberSortingUtil {


  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineNumberSortingUtil.class);


  private static List<Integer> getPipelineNumberNumericalParts(String pipelineNumber) {

    List<Integer> numberParts = new ArrayList<>();
    StringBuilder pipelineNumberValue = new StringBuilder();

    for (Character character: pipelineNumber.toCharArray()) {

      if (Character.isDigit(character)) {
        pipelineNumberValue.append(character);

      } else if (pipelineNumberValue.length() > 0) {
        numberParts.add(Integer.parseInt(pipelineNumberValue.toString()));
        pipelineNumberValue.setLength(0);
      }
    }

    if (pipelineNumberValue.length() > 0) {
      numberParts.add(Integer.parseInt(pipelineNumberValue.toString()));
    }

    return numberParts;
  }

  private static String getPipelineNumberWithoutPrefix(String pipelineNumber) {

    return pipelineNumber.replace("PLU", "")
        .replace("PL", "")
        .replaceAll("\\s","");
  }




  public static int compare(String pipelineNumberA, String pipelineNumberB) {

    var pipelineANumberParts = getPipelineNumberNumericalParts(pipelineNumberA);
    var pipelineBNumberParts = getPipelineNumberNumericalParts(pipelineNumberB);

    for (var x = 0; x < pipelineANumberParts.size(); x++) {

      //pipeline A has numbers remaining, whilst B has reached the end, so A is larger
      if (pipelineBNumberParts.size() == x) {
        return 1;
      }

      var comparisonResult = pipelineANumberParts.get(x).compareTo(pipelineBNumberParts.get(x));
      if (comparisonResult != 0) {
        return comparisonResult;
      }
    }

    if (pipelineANumberParts.size() == 0 && pipelineBNumberParts.size() == 0) {
      //numerical values were not found in either pipeline number
      LOGGER.error("Could not extract a numeric value from the pipeline numbers: {} and {}. " +
              "Falling back to default String comparison of the pipeline number",
          pipelineNumberA, pipelineNumberB);

      return pipelineNumberA.compareTo(pipelineNumberB);

    } else if (pipelineBNumberParts.size() > pipelineANumberParts.size()) {
      //the pipeline numbers were equal so far but pipeline b still has numbers remaining, so B is larger
      return -1;

    }  else {
      //the pipeline numeric values are completely equal, need to compare by their suffix
      return getPipelineNumberWithoutPrefix(pipelineNumberA).compareTo(getPipelineNumberWithoutPrefix(pipelineNumberB));
    }


  }


}
