package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentView;

@RunWith(MockitoJUnitRunner.class)
public class PickableIdentLocationOptionTest {

  private static final String POINT_A = "A";
  private static final String POINT_B = "B";
  private static final String POINT_C = "C";
  private static final String POINT_D = "D";

  @Mock
  private IdentView ident1View;

  @Mock
  private IdentView ident2View;

  @Mock
  private IdentView ident3View;


  @Before
  public void setup() {
    setupIdentViewMock(ident1View, POINT_A, POINT_B, 1);
    setupIdentViewMock(ident2View, POINT_B, POINT_C, 2);
    setupIdentViewMock(ident3View, POINT_C, POINT_D, 3);
  }

  private void setupIdentViewMock(IdentView mockIdentView,
                                  String fromLocation,
                                  String toLocation,
                                  int identNumber) {
    when(mockIdentView.getFromLocation()).thenReturn(fromLocation);
    when(mockIdentView.getToLocation()).thenReturn(toLocation);
    when(mockIdentView.getIdentNumber()).thenReturn(identNumber);

  }

  @Test
  public void createSortedPickableIdentLocationOptionList() {

    var options = PickableIdentLocationOption.createSortedPickableIdentLocationOptionList(
        List.of(ident1View, ident2View, ident3View));

    assertThat(options).containsExactly(
        new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_A),
        new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_B),
        new PickableIdentLocationOption(2, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_B),
        new PickableIdentLocationOption(2, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_C),
        new PickableIdentLocationOption(3, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_C),
        new PickableIdentLocationOption(3, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_D)
    );
  }
}