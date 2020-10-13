package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PickableHuooPipelineIdentServiceTest {
  private static final PipelineId PIPELINE_ID = new PipelineId(1);

  private static final String POINT_A = "POINT A";
  private static final String POINT_B = "POINT B";
  private static final String POINT_C = "POINT C";
  private static final String POINT_D = "POINT D";


  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadPipelineIdentService padPipelineIdentService;

  @Mock
  private PipelineDetailIdentService pipelineDetailIdentService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadPipeline padPipeline;

  @Mock
  private IdentView ident1View;

  @Mock
  private IdentView ident2View;

  @Mock
  private IdentView ident3View;

  private PickableHuooPipelineIdentService pickableHuooPipelineIdentService;

  @Before
  public void setUp() throws Exception {
    pickableHuooPipelineIdentService = new PickableHuooPipelineIdentService(
        padPipelineService,
        padPipelineIdentService,
        pipelineDetailIdentService
    );

   setupIdentViewMock(ident1View, POINT_A, POINT_B, 1);
   setupIdentViewMock(ident2View, POINT_B, POINT_C, 2);
   setupIdentViewMock(ident3View, POINT_C, POINT_D, 3);


    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);

    padPipeline = new PadPipeline(pwaApplicationDetail);
  }

  @Test
  public void getSortedPickableIdentLocationOptions_whenApplicationPipelineFound() {
    when(padPipelineService.findByPwaApplicationDetailAndPipelineId(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(Optional.of(padPipeline));

    var identViewlist = List.of(ident3View, ident2View, ident1View);

    when(padPipelineIdentService.getIdentViews(padPipeline))
        .thenReturn(identViewlist);

    var options = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

    assertThat(options).containsExactly(
        new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_A),
        new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_B),
        new PickableIdentLocationOption(2, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_B),
        new PickableIdentLocationOption(2, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_C),
        new PickableIdentLocationOption(3, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_C),
        new PickableIdentLocationOption(3, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_D)
    );

  }

  @Test
  public void getSortedPickableIdentLocationOptions_whenConsentedPipelineOnlyFound() {
    when(padPipelineService.findByPwaApplicationDetailAndPipelineId(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(Optional.empty());

    var identViewlist = List.of(ident3View, ident2View, ident1View);

    when(pipelineDetailIdentService.getSortedPipelineIdentViewsForPipeline(PIPELINE_ID))
        .thenReturn(identViewlist);

    var options = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

    assertThat(options).containsExactly(
        new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_A),
        new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_B),
        new PickableIdentLocationOption(2, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_B),
        new PickableIdentLocationOption(2, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_C),
        new PickableIdentLocationOption(3, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, POINT_C),
        new PickableIdentLocationOption(3, PickableIdentLocationOption.IdentPoint.TO_LOCATION, POINT_D)
    );

  }

  @Test
  public void getSortedPickableIdentLocationOptions_whenPipelineNotFound() {

    var options = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

    assertThat(options).isEmpty();
  }

  private void setupIdentViewMock(IdentView mockIdentView,
                                        String fromLocation,
                                        String toLocation,
                                        int identNumber                                       ) {
    when(mockIdentView.getFromLocation()).thenReturn(fromLocation);
    when(mockIdentView.getToLocation()).thenReturn(toLocation);
    when(mockIdentView.getIdentNumber()).thenReturn(identNumber);

  }
}