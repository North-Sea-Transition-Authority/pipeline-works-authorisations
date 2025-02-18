package uk.co.ogauthority.pwa.util.forminputs.decimal;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DecimalInputTest {

  private DecimalInput decimalInput;

  @BeforeEach
  void setup() {
    decimalInput = new DecimalInput();
  }

  @Test
  void createBigDecimalOrNull_whenNullValue() {
    assertThat(decimalInput.createBigDecimalOrNull()).isNull();
  }

  @Test
  void asBigDecimal_whenNullValue() {
    assertThat(decimalInput.asBigDecimal()).isEmpty();
  }

  @Test
  void asBigDecimal_whenValueIsNumber() {
    decimalInput.setValue("56.9935");
    assertThat(decimalInput.asBigDecimal()).isPresent();
    assertThat(decimalInput.asBigDecimal()).isEqualTo(Optional.of(BigDecimal.valueOf(56.9935)));
  }

  @Test
  void asBigDecimal_whenValueIsNonNumerical() {
    decimalInput.setValue("no number");
    assertThat(decimalInput.asBigDecimal()).isEmpty();
  }

  @Test
  void equals(){

    EqualsVerifier.forClass(DecimalInput.class)
      .suppress(Warning.NONFINAL_FIELDS)
      .verify();
  }

}