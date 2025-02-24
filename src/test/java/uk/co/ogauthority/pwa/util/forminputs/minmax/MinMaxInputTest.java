package uk.co.ogauthority.pwa.util.forminputs.minmax;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinMaxInputTest {


  @Test
  void isNumeric_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(2), String.valueOf(4));
    assertTrue(minMaxInput.isMinNumeric());
    assertTrue(minMaxInput.isMaxNumeric());
  }

  @Test
  void isNumeric_invalid() {
    var minMaxInput = new MinMaxInput();
    assertFalse(minMaxInput.isMinNumeric());
    assertFalse(minMaxInput.isMaxNumeric());
  }


  @Test
  void isMinSmallerOrEqualToMax_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(1), String.valueOf(4));
    assertTrue(minMaxInput.minSmallerOrEqualToMax());
  }

  @Test
  void isMinSmallerOrEqualToMax_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5), String.valueOf(4));
    assertFalse(minMaxInput.minSmallerOrEqualToMax());
  }


  @Test
  void hasValidDecimalPlaces_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5.33), String.valueOf(4.2));
    assertTrue(minMaxInput.minHasValidDecimalPlaces(2));
    assertTrue(minMaxInput.maxHasValidDecimalPlaces(1));
  }

  @Test
  void hasValidDecimalPlaces_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5.333), String.valueOf(4.22));
    assertFalse(minMaxInput.minHasValidDecimalPlaces(2));
    assertFalse(minMaxInput.maxHasValidDecimalPlaces(1));
  }


  @Test
  void isPositive_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(3), String.valueOf(5));
    assertTrue(minMaxInput.isMinPositive());
    assertTrue(minMaxInput.isMaxPositive());
  }

  @Test
  void isPositive_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(-3), String.valueOf(-5));
    assertFalse(minMaxInput.isMinPositive());
    assertFalse(minMaxInput.isMaxPositive());
  }


  @Test
  void isInteger_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5), String.valueOf(7));
    assertTrue(minMaxInput.isMinInteger());
    assertTrue(minMaxInput.isMaxInteger());
  }

  @Test
  void isInteger_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5.1), String.valueOf(7.7));
    assertFalse(minMaxInput.isMinInteger());
    assertFalse(minMaxInput.isMaxInteger());
  }


}