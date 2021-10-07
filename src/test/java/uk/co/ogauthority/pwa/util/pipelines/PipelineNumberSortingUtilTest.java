package uk.co.ogauthority.pwa.util.pipelines;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PipelineNumberSortingUtilTest {


  @Test
  public void compare_comparisonExcludesAnyLetterPrefix_pipelineNumberNumericValuesCompared() {
    var pipelineNumber1 = "  PLU4353";
    var pipelineNumber2 = "PL638   ";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isGreaterThan(0);
  }

  @Test
  public void compare_comparisonExcludesAnyLetterSuffix_pipelineNumberNumericValuesCompared() {
    var pipelineNumber1 = "4353RTD";
    var pipelineNumber2 = "PL638NL";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isGreaterThan(0);
  }

  @Test
  public void compare_comparisonIncludesDot_pipelineNumberNumericValuesCompared() {
    var pipelineNumber1 = "PL4353.65";
    var pipelineNumber2 = "PL638.23";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isGreaterThan(0);
  }

  @Test
  public void compare_numericValuesAreEqual_pipelineNumberNumberExcludingPrefixCompared() {
    var pipelineNumber1 = "PL1905(J)21/24-TU";
    var pipelineNumber2 = "PL1905(J)21/24-T1";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isGreaterThan(0);
  }

  @Test
  public void compare_pipelineNumericValueNotFound_defaultStringComparisonPerformed() {
    var pipelineNumber1 = "PLURTS";
    var pipelineNumber2 = "PLNL";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isGreaterThan(0);
  }


}