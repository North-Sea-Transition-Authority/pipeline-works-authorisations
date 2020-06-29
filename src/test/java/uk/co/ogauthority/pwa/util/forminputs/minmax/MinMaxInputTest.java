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
  public void isEmpty_valid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(2), BigDecimal.valueOf(4));
    assertFalse(minMaxInput.isMinEmpty());
    assertFalse(minMaxInput.isMaxEmpty());
  }

  @Test
  public void isEmpty_invalid() {
    var minMaxInput = new MinMaxInput();
    assertTrue(minMaxInput.isMinEmpty());
    assertTrue(minMaxInput.isMaxEmpty());
  }


  @Test
  public void isMinSmallerOrEqualToMax_valid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(1), BigDecimal.valueOf(4));
    assertTrue(minMaxInput.minSmallerOrEqualToMax());
  }

  @Test
  public void isMinSmallerOrEqualToMax_invalid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(5), BigDecimal.valueOf(4));
    assertFalse(minMaxInput.minSmallerOrEqualToMax());
  }


  @Test
  public void hasValidDecimalPlaces_valid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(5.33), BigDecimal.valueOf(4.2));
    assertTrue(minMaxInput.minHasValidDecimalPlaces(2));
    assertTrue(minMaxInput.maxHasValidDecimalPlaces(1));
  }

  @Test
  public void hasValidDecimalPlaces_invalid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(5.333), BigDecimal.valueOf(4.22));
    assertFalse(minMaxInput.minHasValidDecimalPlaces(2));
    assertFalse(minMaxInput.maxHasValidDecimalPlaces(1));
  }


  @Test
  public void isPositive_valid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(3), BigDecimal.valueOf(5));
    assertTrue(minMaxInput.isMinPositive());
    assertTrue(minMaxInput.isMaxPositive());
  }

  @Test
  public void isPositive_invalid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(-3), BigDecimal.valueOf(-5));
    assertFalse(minMaxInput.isMinPositive());
    assertFalse(minMaxInput.isMaxPositive());
  }


  @Test
  public void isInteger_valid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(5), BigDecimal.valueOf(7));
    assertTrue(minMaxInput.isMinInteger());
    assertTrue(minMaxInput.isMaxInteger());
  }

  @Test
  public void isInteger_invalid() {
    var minMaxInput = new MinMaxInput(BigDecimal.valueOf(5.1), BigDecimal.valueOf(7.7));
    assertFalse(minMaxInput.isMinInteger());
    assertFalse(minMaxInput.isMaxInteger());
  }


}