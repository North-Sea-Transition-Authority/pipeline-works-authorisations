package uk.co.ogauthority.pwa.util;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PwaNumberUtilsTest {

  @SuppressWarnings("ConstantConditions")// suppress warning that output is always true or false. That's what we want to test!
  @Test
  public void numberOfDecimalPlacesLessThanOrEqual_whenNull() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(null, 2, true);
    assertThat(expectTrue).isTrue();

    var expectFalse = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(null, 2, false);
    assertThat(expectFalse).isFalse();

  }

  @Test
  public void numberOfDecimalPlacesLessThanOrEqual_whenMaxDecimalPlaces() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(BigDecimal.valueOf(0.01), 2, true);
    assertThat(expectTrue).isTrue();

  }

  @Test
  public void numberOfDecimalPlacesLessThanOrEqual_whenMoreThanMaxDecimalPlaces() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(BigDecimal.valueOf(0.001), 2, true);
    assertThat(expectTrue).isFalse();

  }

  @Test
  public void numberOfDecimalPlacesLessThanOrEqual_whenLesThanMaxDecimalPlaces() {

    var expectTrue = PwaNumberUtils.numberOfDecimalPlacesLessThanOrEqual(BigDecimal.valueOf(0.1), 2, true);
    assertThat(expectTrue).isTrue();

  }
}