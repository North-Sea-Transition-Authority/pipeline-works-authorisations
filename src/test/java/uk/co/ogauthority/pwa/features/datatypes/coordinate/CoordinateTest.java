package uk.co.ogauthority.pwa.features.datatypes.coordinate;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CoordinateTest {

  @Test
  void convertToDecimalDegrees_smoke() {
    // deg min second and expected decimal degrees conversion output values
    // test values taken from the wikipedia page https://en.wikipedia.org/wiki/Decimal_degrees
    // with scale increased
    Map<CoordinateImpl, Double> convertToDecimalDegreesSmoke = Map.of(
        new CoordinateImpl(1, 0, BigDecimal.ZERO), 1d,
        new CoordinateImpl(0, 6, BigDecimal.ZERO), 0.1d,
        new CoordinateImpl(0, 0, BigDecimal.valueOf(36)), 0.01d,
        new CoordinateImpl(0, 0, BigDecimal.valueOf(3.6)), 0.001d,
        new CoordinateImpl(0, 0, BigDecimal.valueOf(0.36)), 0.0001d,

        new CoordinateImpl(38, 53, BigDecimal.valueOf(23)), 38.8897d,
        new CoordinateImpl(77, 0, BigDecimal.valueOf(32)), 77.0089d
    );

    convertToDecimalDegreesSmoke.entrySet().forEach(coordinateArgsDoubleEntry -> {

      try {
        assertThat(coordinateArgsDoubleEntry.getKey().convertToDecimalDegrees()).isEqualTo(coordinateArgsDoubleEntry.getValue());
      } catch (AssertionError e) {
        throw new AssertionError("Failed at " + coordinateArgsDoubleEntry.getKey() + " expected " + coordinateArgsDoubleEntry.getValue(), e);
      }
    });

  }

  // Test only implementation of the abstract class for method tests
  static class CoordinateImpl extends Coordinate {

    public CoordinateImpl(Integer degrees, Integer minutes, BigDecimal seconds) {
      super(degrees, minutes, seconds);
    }

    @Override
    public String getDisplayString() {
      return this.toString();
    }
  }

}