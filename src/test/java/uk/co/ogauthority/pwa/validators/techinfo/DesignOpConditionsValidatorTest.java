package uk.co.ogauthority.pwa.validators.techinfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.util.forminputs.minmax.MinMaxInputValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PadDesignOpConditionsValidator;

@RunWith(MockitoJUnitRunner.class)
public class DesignOpConditionsValidatorTest {

  private MinMaxInputValidator minMaxInputValidator;
  private PadDesignOpConditionsValidator validator;

  @Before
  public void setUp() {
    minMaxInputValidator = new MinMaxInputValidator();
    validator = new PadDesignOpConditionsValidator(minMaxInputValidator);
  }


  @Test
  public void validate_form_empty() {
  }

  @Test
  public void validate_form_valid() {
  }






}