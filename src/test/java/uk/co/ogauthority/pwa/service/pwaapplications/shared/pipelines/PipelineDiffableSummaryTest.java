package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;

@RunWith(MockitoJUnitRunner.class)
public class PipelineDiffableSummaryTest {

  private final String PIPELINE_NAME = "PIPELINE";
  private final PipelineId PIPELINE_ID = new PipelineId(1);

  private final String POINT_1 = IdentViewTestUtil.POINT_1;
  private final String POINT_2 = IdentViewTestUtil.POINT_2;
  private final String POINT_3 = IdentViewTestUtil.POINT_3;
  private final String POINT_4 = IdentViewTestUtil.POINT_4;

  @Mock
  private PipelineHeaderView pipelineHeaderView;

  @Mock
  private IdentView startIdent;

  @Mock
  private IdentView midIdent;

  @Mock
  private IdentView endIdent;

  @Before
  public void setUp() {

    when(pipelineHeaderView.getPipelineName()).thenReturn(PIPELINE_NAME);
    when(pipelineHeaderView.getPipelineId()).thenReturn(PIPELINE_ID.asInt());

    IdentViewTestUtil.setupSingleCoreIdentViewMock(startIdent, POINT_1, POINT_2, 1);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(midIdent, POINT_2, POINT_3, 2);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(endIdent, POINT_3, POINT_4, 3);
  }

  @Test
  public void from_mapsPipelineHeaderInfo_andContainsAllIdents() {

    var result = PipelineDiffableSummary.from(pipelineHeaderView, List.of(startIdent, midIdent, endIdent));

    assertThat(result.getIdentViews()).hasSize(3);
    assertThat(result.getPipelineHeaderView().getPipelineName()).isEqualTo(PIPELINE_NAME);
    assertThat(result.getPipelineId()).isEqualTo(PIPELINE_ID);
  }

  @Test
  public void from_processesIdentsInOrder() {

    var result = PipelineDiffableSummary.from(pipelineHeaderView, List.of(startIdent, midIdent, endIdent));

    assertThat(result.getIdentViews()).hasSize(3);

    // startIdent
    assertThat(result.getIdentViews().get(0)).satisfies(identDiffableView -> {
      assertThat(identDiffableView.getFromLocation()).isEqualTo(POINT_1);
      assertThat(identDiffableView.getToLocation()).isEqualTo(POINT_2);
      assertThat(identDiffableView.getIdentNumber()).isEqualTo(1);
      assertThat(identDiffableView.getConnectedToPrevious()).isFalse();
      assertThat(identDiffableView.getConnectedToNext()).isTrue();
    });

    // midIdent
    assertThat(result.getIdentViews().get(1)).satisfies(identDiffableView -> {
      assertThat(identDiffableView.getFromLocation()).isEqualTo(POINT_2);
      assertThat(identDiffableView.getToLocation()).isEqualTo(POINT_3);
      assertThat(identDiffableView.getIdentNumber()).isEqualTo(2);
      assertThat(identDiffableView.getConnectedToPrevious()).isTrue();
      assertThat(identDiffableView.getConnectedToNext()).isTrue();
    });

    // endIdent
    assertThat(result.getIdentViews().get(2)).satisfies(identDiffableView -> {
      assertThat(identDiffableView.getFromLocation()).isEqualTo(POINT_3);
      assertThat(identDiffableView.getToLocation()).isEqualTo(POINT_4);
      assertThat(identDiffableView.getIdentNumber()).isEqualTo(3);
      assertThat(identDiffableView.getConnectedToPrevious()).isTrue();
      assertThat(identDiffableView.getConnectedToNext()).isFalse();
    });

  }

  @Test
  public void equals_hashcode(){

    EqualsVerifier.forClass(PipelineDiffableSummary.class)
        .verify();

  }


}