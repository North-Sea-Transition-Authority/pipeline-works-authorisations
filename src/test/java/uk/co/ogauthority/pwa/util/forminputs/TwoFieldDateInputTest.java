package uk.co.ogauthority.pwa.util.forminputs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@ExtendWith(MockitoExtension.class)
class TwoFieldDateInputTest {

  private TwoFieldDateInput twoFieldDateInput;

  @BeforeEach
  void setup() {
    twoFieldDateInput = new TwoFieldDateInput();
  }

  @Test
  void createDate_whenNullValues() {
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }

  @Test
  void createDate_whenInvalidTextDateComponents() {
    twoFieldDateInput.setMonth("testMonth");
    twoFieldDateInput.setYear("testYear");
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }

  @Test
  void createDate_whenValidDateComponents_defaultsToFirstDayOfMonth() {
    twoFieldDateInput.setMonth("2");
    twoFieldDateInput.setYear("2020");
    assertThat(twoFieldDateInput.createDate()).contains(LocalDate.of(2020, 2, 1));
  }

  @Test
  void createDate_whenInValidNumberDateComponents_invalidLargeMonth() {
    twoFieldDateInput.setMonth("13");
    twoFieldDateInput.setYear("2020");
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }

  @Test
  void createDate_whenInValidNumberDateComponents_invalidSmallMonth() {
    twoFieldDateInput.setMonth("0");
    twoFieldDateInput.setYear("2020");
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }


  @Test
  void isAfter_whenInvalidDate() {
    assertThat(twoFieldDateInput.isAfter(LocalDate.now())).isFalse();
  }

  @Test
  void isAfter_whenValidDate_andDateIsSameYearMonth() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isAfter(LocalDate.now()))
        .isFalse();
  }


  @Test
  void isAfter_whenValidDate_andDateIsMonthsBefore() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isAfter(LocalDate.now().plus(1, ChronoUnit.MONTHS)))
        .isFalse();
  }

  @Test
  void isAfter_whenValidDate_andDateIsMonthsAfter() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isAfter(LocalDate.now().minus(1, ChronoUnit.MONTHS)))
        .isTrue();
  }


  @Test
  void isBefore_whenInvalidDate() {
    assertThat(twoFieldDateInput.isBefore(LocalDate.now())).isFalse();
  }

  @Test
  void isBefore_whenValidDate_andDateIsSameYearMonth() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isBefore(LocalDate.now()))
        .isFalse();
  }


  @Test
  void isBefore_whenValidDate_andDateIsMonthsBefore() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isBefore(LocalDate.now().plus(1, ChronoUnit.MONTHS)))
        .isTrue();
  }

  @Test
  void isBefore_whenValidDate_andDateIsMonthsAfter() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isBefore(LocalDate.now().minus(1, ChronoUnit.MONTHS)))
        .isFalse();
  }


  @Test
  void isInSameMonth_whenInvalidDate() {
    assertThat(twoFieldDateInput.isInSameMonth(LocalDate.now())).isFalse();
  }

  @Test
  void isInSameMonth_whenValidDate_andDateIsSameYearMonth() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isInSameMonth(LocalDate.now()))
        .isTrue();
  }


  @Test
  void isInSameMonth_whenValidDate_andDateIsMonthsBefore() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isInSameMonth(LocalDate.now().plus(1, ChronoUnit.MONTHS)))
        .isFalse();
  }

  @Test
  void isInSameMonth_whenValidDate_andDateIsMonthsAfter() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isInSameMonth(LocalDate.now().minus(1, ChronoUnit.MONTHS)))
        .isFalse();
  }
}