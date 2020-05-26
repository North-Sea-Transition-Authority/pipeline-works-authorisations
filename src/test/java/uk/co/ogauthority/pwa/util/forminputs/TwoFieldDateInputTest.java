package uk.co.ogauthority.pwa.util.forminputs;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TwoFieldDateInputTest {

  private TwoFieldDateInput twoFieldDateInput;

  @Before
  public void setup() {
    twoFieldDateInput = new TwoFieldDateInput();
  }

  @Test
  public void createDate_whenNullValues() {
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }

  @Test
  public void createDate_whenInvalidTextDateComponents() {
    twoFieldDateInput.setMonth("testMonth");
    twoFieldDateInput.setYear("testYear");
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }

  @Test
  public void createDate_whenValidDateComponents_defaultsToFirstDayOfMonth() {
    twoFieldDateInput.setMonth("2");
    twoFieldDateInput.setYear("2020");
    assertThat(twoFieldDateInput.createDate()).contains(LocalDate.of(2020, 2, 1));
  }

  @Test
  public void createDate_whenInValidNumberDateComponents_invalidLargeMonth() {
    twoFieldDateInput.setMonth("13");
    twoFieldDateInput.setYear("2020");
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }

  @Test
  public void createDate_whenInValidNumberDateComponents_invalidSmallMonth() {
    twoFieldDateInput.setMonth("0");
    twoFieldDateInput.setYear("2020");
    assertThat(twoFieldDateInput.createDate()).isEmpty();
  }


  @Test
  public void isAfter_whenInvalidDate() {
    assertThat(twoFieldDateInput.isAfter(LocalDate.now())).isFalse();
  }

  @Test
  public void isAfter_whenValidDate_andDateIsSameYearMonth() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isAfter(LocalDate.now()))
        .isFalse();
  }


  @Test
  public void isAfter_whenValidDate_andDateIsMonthsBefore() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isAfter(LocalDate.now().plus(1, ChronoUnit.MONTHS)))
        .isFalse();
  }

  @Test
  public void isAfter_whenValidDate_andDateIsMonthsAfter() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isAfter(LocalDate.now().minus(1, ChronoUnit.MONTHS)))
        .isTrue();
  }


  @Test
  public void isBefore_whenInvalidDate() {
    assertThat(twoFieldDateInput.isBefore(LocalDate.now())).isFalse();
  }

  @Test
  public void isBefore_whenValidDate_andDateIsSameYearMonth() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isBefore(LocalDate.now()))
        .isFalse();
  }


  @Test
  public void isBefore_whenValidDate_andDateIsMonthsBefore() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isBefore(LocalDate.now().plus(1, ChronoUnit.MONTHS)))
        .isTrue();
  }

  @Test
  public void isBefore_whenValidDate_andDateIsMonthsAfter() {
    twoFieldDateInput.setYear(LocalDate.now().getYear());
    twoFieldDateInput.setMonth(LocalDate.now().getMonthValue());

    assertThat(twoFieldDateInput.isBefore(LocalDate.now().minus(1, ChronoUnit.MONTHS)))
        .isFalse();
  }

}