package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PwaReferenceComparatorTest {

  private String firstPwaReference;
  private String secondPwaReference;
  private String thirdPwaReference;

  public void initPwaReferenceComparatorTest(String firstPwaReference,
                                    String secondPwaReference,
                                    String thirdPwaReference) {
    this.firstPwaReference = firstPwaReference;
    this.secondPwaReference = secondPwaReference;
    this.thirdPwaReference = thirdPwaReference;
  }

  @MethodSource("getPwasToCompare")
  @ParameterizedTest(name = "{0} {1} {2}")
  public void compare(String firstPwaReference, String secondPwaReference, String thirdPwaReference) {
    initPwaReferenceComparatorTest(firstPwaReference, secondPwaReference, thirdPwaReference);
    var firstPwaDto = PwaDtoTestUtil.builder()
        .withReference(firstPwaReference)
        .build();

    var secondPwaDto = PwaDtoTestUtil.builder()
        .withReference(secondPwaReference)
        .build();

    var thirdPwaDto = PwaDtoTestUtil.builder()
        .withReference(thirdPwaReference)
        .build();

    var sortedList = Stream.of(secondPwaDto, thirdPwaDto, firstPwaDto)
        .sorted(new PwaReferenceComparator())
        .collect(Collectors.toUnmodifiableList());

    assertThat(sortedList)
        .extracting(PwaDto::getReference)
        .containsExactly(
            firstPwaDto.getReference(),
            secondPwaDto.getReference(),
            thirdPwaDto.getReference()
        );
  }

  public static Collection getPwasToCompare() {
    return Arrays.asList(new Object[][] {
        {"1/W/01", "2/W/01", "10/W/01"},
        {"1/W/01", "1/W/02", "1/W/10"},
        {"1/D/01", "1/V/01", "1/W/01"},
        {"1/D/01", "1/W/01", "1/V/02"},
        {"1/W/01", "10/W/01", "1/W/02"},
        {"1/W/01", "PA/1", "PA/2"},
        {"NONPWA/99", "PA/2", "PWADATE/1983-01-12"},
        {"NONPWA/2", "NONPWA/10", "PWADATE/1983-01-12"},
        {"NONPWA/99", "PWADATE/1983-01-12", "PWADATE/1983-10-12"}
    });
  }
}