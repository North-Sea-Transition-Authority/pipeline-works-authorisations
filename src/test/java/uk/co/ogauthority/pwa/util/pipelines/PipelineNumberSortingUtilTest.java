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
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_comparisonExcludesAnyLetterSuffix_pipelineNumberNumericValuesCompared() {
    var pipelineNumber1 = "4353RTD";
    var pipelineNumber2 = "PL638NL";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_comparisonIncludesDot_pipelineNumberNumericValuesCompared() {
    var pipelineNumber1 = "PL638.65";
    var pipelineNumber2 = "PL638.23";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_comparisonIncludesDot_sameDimension_firstNumHasTensAndUnits_secondNumHasUnitsOnly() {
    var pipelineNumber1 = "PL123.11";
    var pipelineNumber2 = "PL123.2";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_comparisonIncludesMultipleDots_sameDimension_firstSectionEqual_comparisonMadeAtSecondSection() {
    var pipelineNumber1 = "PL638.65.34";
    var pipelineNumber2 = "PL638.23.34";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_comparisonIncludesMultipleDots_sameDimension_numbersEqualUpToLastSection_comparisonMadeAtLastSection() {
    var pipelineNumber1 = "PL638.11.45";
    var pipelineNumber2 = "PL638.11.25";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_comparisonIncludesMultipleDots_firstNumHasSmallerDimension_equalUpToSameDimension_firstNumIsSmaller() {
    var pipelineNumber1 = "PL638.11";
    var pipelineNumber2 = "PL638.11.25";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isNegative();
  }

  @Test
  public void compare_comparisonIncludesMultipleDots_secondNumHasSmallerDimension_equalUpToSameDimension_secondNumIsSmaller() {
    var pipelineNumber1 = "PL638.11.25";
    var pipelineNumber2 = "PL638.11";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_numericValuesAreEqualUpToLetterSuffix_pipelineNumberWithAdditionalNumericValueIsLarger() {
    var commonPipelineNumberPart = "1905.34.98.58.36(J)21/24-T";
    var pipelineNumber1 = "PL" + commonPipelineNumberPart + "1";
    var pipelineNumber2 = "PLU" + commonPipelineNumberPart + "U";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_numericValuesAreEqualUpToLetterSuffix_numbersAfterLetterSuffixCompared() {
    var pipelineNumber1 = "PL1841.2JP10";
    var pipelineNumber2 = "PL1841.2JP3";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_numericValuesEqual_comparedAsStringIgnoringWhiteSpace() {
    var pipelineNumber1 = "PLU2447 JP2";
    var pipelineNumber2 = "PLU2447JI2";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }

  @Test
  public void compare_pipelineNumericValueNotFound_defaultStringComparisonPerformed() {
    var pipelineNumber1 = "PLURTS";
    var pipelineNumber2 = "PLNL";
    assertThat(PipelineNumberSortingUtil.compare(pipelineNumber1, pipelineNumber2)).isPositive();
  }



}