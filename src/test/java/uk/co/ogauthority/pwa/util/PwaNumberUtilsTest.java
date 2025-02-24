package uk.co.ogauthority.pwa.util;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PwaNumberUtilsTest {

  @SuppressWarnings("ConstantConditions")// suppress warning that output is always true or false. That's what we want to test!
  @Test
  void numberOfDecimalPlacesLessThanOrEqual_whenNull() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(null, 2, true);
    assertThat(expectTrue).isTrue();

    var expectFalse = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(null, 2, false);
    assertThat(expectFalse).isFalse();

  }

  @Test
  void numberOfDecimalPlacesLessThanOrEqual_whenMaxDecimalPlaces() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(BigDecimal.valueOf(0.01), 2, true);
    assertThat(expectTrue).isTrue();

  }

  @Test
  void numberOfDecimalPlacesLessThanOrEqual_whenMoreThanMaxDecimalPlaces() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(BigDecimal.valueOf(0.001), 2, true);
    assertThat(expectTrue).isFalse();

  }

  @Test
  void numberOfDecimalPlacesLessThanOrEqual_whenLesThanMaxDecimalPlaces() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(BigDecimal.valueOf(0.1), 2, true);
    assertThat(expectTrue).isTrue();

  }

  @Test
  void numberOfDecimalPlacesLessThanOrEqual_valid() {
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0"), 0, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0"), 1, true)).isTrue();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.0"), 0, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.0"), 1, true)).isTrue();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.1"), 1, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.1"), 2, true)).isTrue();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1"), 0, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1"), 1, true)).isTrue();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.0"), 0, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.0"), 1, true)).isTrue();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.1"), 1, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.10"), 1, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.1"), 2, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.10"), 2, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.11"), 2, true)).isTrue();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.1"), 1, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.10"), 1, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.1"), 2, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.10"), 2, true)).isTrue();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.11"), 2, true)).isTrue();
  }

  @Test
  void numberOfDecimalPlacesLessThanOrEqual_invalid() {

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.1"), 0, true)).isFalse();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.10"), 0, true)).isFalse();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("0.11"), 1, true)).isFalse();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.1"), 0, true)).isFalse();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.10"), 0, true)).isFalse();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("1.11"), 1, true)).isFalse();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.1"), 0, true)).isFalse();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.10"), 0, true)).isFalse();
    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("10.11"), 1, true)).isFalse();

    assertThat(PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(new BigDecimal("99.011"), 2, true)).isFalse();
  }

  @Test
  void getNumberOfDpIncludingTrailingZeros() {
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("0.1"))).isEqualTo(1);
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("0.11"))).isEqualTo(2);
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("0.0"))).isEqualTo(1);
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("0.00"))).isEqualTo(2);

    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("1.1"))).isEqualTo(1);
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("1.11"))).isEqualTo(2);
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("1.0"))).isEqualTo(1);
    assertThat(PwaNumberUtils.getNumberOfDpIncludingTrailingZeros(new BigDecimal("1.00"))).isEqualTo(2);
  }

  @Test
  void getNumberOfDp() {
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("0.1"))).isEqualTo(1);
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("0.11"))).isEqualTo(2);
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("0.0"))).isZero();
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("0.00"))).isZero();

    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("1.1"))).isEqualTo(1);
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("1.11"))).isEqualTo(2);
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("1.0"))).isZero();
    assertThat(PwaNumberUtils.getNumberOfDp(new BigDecimal("1.00"))).isZero();
  }
}