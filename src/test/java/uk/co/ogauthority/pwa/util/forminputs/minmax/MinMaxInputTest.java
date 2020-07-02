package uk.co.ogauthority.pwa.util.forminputs.minmax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MinMaxInputTest {



  @Test
  public void isNumeric_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(2), String.valueOf(4));
    assertTrue(minMaxInput.isMinNumeric());
    assertTrue(minMaxInput.isMaxNumeric());
  }

  @Test
  public void isNumeric_invalid() {
    var minMaxInput = new MinMaxInput();
    assertFalse(minMaxInput.isMinNumeric());
    assertFalse(minMaxInput.isMaxNumeric());
  }


  @Test
  public void isMinSmallerOrEqualToMax_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(1), String.valueOf(4));
    assertTrue(minMaxInput.minSmallerOrEqualToMax());
  }

  @Test
  public void isMinSmallerOrEqualToMax_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5), String.valueOf(4));
    assertFalse(minMaxInput.minSmallerOrEqualToMax());
  }


  @Test
  public void hasValidDecimalPlaces_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5.33), String.valueOf(4.2));
    assertTrue(minMaxInput.minHasValidDecimalPlaces(2));
    assertTrue(minMaxInput.maxHasValidDecimalPlaces(1));
  }

  @Test
  public void hasValidDecimalPlaces_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5.333), String.valueOf(4.22));
    assertFalse(minMaxInput.minHasValidDecimalPlaces(2));
    assertFalse(minMaxInput.maxHasValidDecimalPlaces(1));
  }


  @Test
  public void isPositive_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(3), String.valueOf(5));
    assertTrue(minMaxInput.isMinPositive());
    assertTrue(minMaxInput.isMaxPositive());
  }

  @Test
  public void isPositive_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(-3), String.valueOf(-5));
    assertFalse(minMaxInput.isMinPositive());
    assertFalse(minMaxInput.isMaxPositive());
  }


  @Test
  public void isInteger_valid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5), String.valueOf(7));
    assertTrue(minMaxInput.isMinInteger());
    assertTrue(minMaxInput.isMaxInteger());
  }

  @Test
  public void isInteger_invalid() {
    var minMaxInput = new MinMaxInput(String.valueOf(5.1), String.valueOf(7.7));
    assertFalse(minMaxInput.isMinInteger());
    assertFalse(minMaxInput.isMaxInteger());
  }


}