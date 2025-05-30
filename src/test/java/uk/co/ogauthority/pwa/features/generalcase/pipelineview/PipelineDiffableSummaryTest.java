package uk.co.ogauthority.pwa.features.generalcase.pipelineview;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawing;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingSummaryView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineDiffableSummaryTest {

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

  @BeforeEach
  void setUp() {

    when(pipelineHeaderView.getPipelineName()).thenReturn(PIPELINE_NAME);
    when(pipelineHeaderView.getPipelineId()).thenReturn(PIPELINE_ID.asInt());

    IdentViewTestUtil.setupSingleCoreIdentViewMock(startIdent, POINT_1, POINT_2, 1);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(midIdent, POINT_2, POINT_3, 2);
    IdentViewTestUtil.setupSingleCoreIdentViewMock(endIdent, POINT_3, POINT_4, 3);
  }

  @Test
  void from_mapsPipelineHeaderInfo_andContainsAllIdents() {

    var result = PipelineDiffableSummary.from(pipelineHeaderView, List.of(startIdent, midIdent, endIdent), new PipelineDrawingSummaryView(new PadTechnicalDrawing(), List.of()));

    assertThat(result.getIdentViews()).hasSize(3);
    assertThat(result.getPipelineHeaderView().getPipelineName()).isEqualTo(PIPELINE_NAME);
    assertThat(result.getPipelineId()).isEqualTo(PIPELINE_ID);
  }

  @Test
  void from_processesIdentsInOrder() {

    var result = PipelineDiffableSummary.from(pipelineHeaderView, List.of(startIdent, midIdent, endIdent), new PipelineDrawingSummaryView(new PadTechnicalDrawing(), List.of()));

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
  void equals_hashcode(){

    EqualsVerifier.forClass(PipelineDiffableSummary.class)
        .verify();

  }


}