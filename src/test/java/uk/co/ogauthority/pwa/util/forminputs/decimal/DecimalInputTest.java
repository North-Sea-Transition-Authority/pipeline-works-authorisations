package uk.co.ogauthority.pwa.util.forminputs.decimal;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DecimalInputTest {

  private DecimalInput decimalInput;

  @Before
  public void setup() {
    decimalInput = new DecimalInput();
  }

  @Test
  public void createBigDecimalOrNull_whenNullValue() {
    assertThat(decimalInput.createBigDecimalOrNull()).isNull();
  }

  @Test
  public void asBigDecimal_whenNullValue() {
    assertThat(decimalInput.asBigDecimal()).isEmpty();
  }

  @Test
  public void asBigDecimal_whenValueIsNumber() {
    decimalInput.setValue("56.9935");
    assertThat(decimalInput.asBigDecimal()).isPresent();
    assertThat(decimalInput.asBigDecimal()).isEqualTo(Optional.of(BigDecimal.valueOf(56.9935)));
  }

  @Test
  public void asBigDecimal_whenValueIsNonNumerical() {
    decimalInput.setValue("no number");
    assertThat(decimalInput.asBigDecimal()).isEmpty();
  }



}