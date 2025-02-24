package uk.co.ogauthority.pwa.util.pipelines;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.ogauthority.pwa.externalapi.PipelineDto;
import uk.co.ogauthority.pwa.externalapi.PipelineDtoTestUtil;

public class PipelineNumberSortingUtilTest {

  private String firstPipelineNumber;
  private String secondPipelineNumber;

  public void initPipelineNumberSortingUtilTest(String firstPipelineNumber, String secondPipelineNumber) {
    this.firstPipelineNumber = firstPipelineNumber;
    this.secondPipelineNumber = secondPipelineNumber;
  }

  @MethodSource("getPipelinesToCompare")
  @ParameterizedTest(name = "{0} {1}")
  public void compare_comparisonExcludesAnyLetterPrefix_pipelineNumberNumericValuesCompared(String firstPipelineNumber, String secondPipelineNumber) {
    initPipelineNumberSortingUtilTest(firstPipelineNumber, secondPipelineNumber);
    var firstPipelineDto = PipelineDtoTestUtil.builder()
        .withNumber(firstPipelineNumber)
        .build();
    var secondPipelineDto = PipelineDtoTestUtil.builder()
        .withNumber(secondPipelineNumber)
        .build();

    var sortedList = Stream.of(secondPipelineDto, firstPipelineDto)
        .sorted((pipelineDto1, pipelineDto2) -> PipelineNumberSortingUtil.compare(pipelineDto1.getPipelineNumber(), pipelineDto2.getPipelineNumber()))
        .collect(Collectors.toList());

    assertThat(sortedList)
        .extracting(PipelineDto::getPipelineNumber)
        .containsExactly(
            firstPipelineDto.getPipelineNumber(),
            secondPipelineDto.getPipelineNumber()
        );
  }

  public static Collection getPipelinesToCompare() {
    return Arrays.asList(new Object[][] {
        {"PL638   ", "  PLU4353"},
        {"PL638NL", "4353RTD"},
        {"PL638.23", "PL638.65"},
        {"PL123.2", "PL123.11"},
        {"PL638.23.34", "PL638.65.34"},
        {"PL638.11.25", "PL638.11.45"},
        {"PL638.11", "PL638.11.25"},
        {"PLU1905.34.98.58.36(J)21/24-TU", "PL1905.34.98.58.36(J)21/24-T1"},
        {"PL1841.2JP3", "PL1841.2JP10"},
        {"PLU2447JI2", "PLU2447 JP2"},
        {"PLNL", "PLURTS"}
    });
  }
}