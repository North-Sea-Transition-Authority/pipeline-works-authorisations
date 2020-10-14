package uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;


@RunWith(MockitoJUnitRunner.class)
public class DefinePipelineHuooSectionsFormTest {

  private PickableIdentLocationOption firstIdentLocation;

  @Before
  public void setup(){
    firstIdentLocation = new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, "POINT 1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void resetSectionPoints_when0Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    form.resetSectionPoints(0,  firstIdentLocation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void resetSectionPoints_whenLessThanZero0Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    form.resetSectionPoints(-1,  firstIdentLocation);
  }

  @Test
  public void resetSectionPoints_when1Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    form.resetSectionPoints(1,  firstIdentLocation);
  }
}