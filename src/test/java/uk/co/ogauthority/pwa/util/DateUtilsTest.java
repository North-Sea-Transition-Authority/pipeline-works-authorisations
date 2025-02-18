package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

  @Test
  void isOnOrAfter_comparisonIsBefore_true() {

    var thisDate = LocalDate.of(2030, 1, 1);
    var beforeThisDate = thisDate.minus(1, ChronoUnit.DAYS);

    boolean isOnOrAfter = DateUtils.isOnOrAfter(thisDate, beforeThisDate);

    assertThat(isOnOrAfter).isTrue();

  }

  @Test
  void isOnOrAfter_comparisonIsOn_true() {

    var thisDate = LocalDate.of(2030, 1, 1);

    boolean isOnOrAfter = DateUtils.isOnOrAfter(thisDate, thisDate);

    assertThat(isOnOrAfter).isTrue();

  }

  @Test
  void isOnOrAfter_comparisonIsAfter_false() {

    var thisDate = LocalDate.of(2030, 1, 1);
    var afterThisDate = thisDate.plus(1, ChronoUnit.DAYS);

    boolean isOnOrAfter = DateUtils.isOnOrAfter(thisDate, afterThisDate);

    assertThat(isOnOrAfter).isFalse();

  }

}