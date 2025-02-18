package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.IdentView;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PickableHuooPipelineIdentServiceTest {
  private static final PipelineId PIPELINE_ID = new PipelineId(1);

  private static final String POINT_A = "POINT A";
  private static final String POINT_B = "POINT B";
  private static final String POINT_C = "POINT C";
  private static final String POINT_D = "POINT D";

  @Mock
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  private PwaApplicationDetail pwaApplicationDetail;

  @Mock
  private IdentView ident1View;

  @Mock
  private IdentView ident2View;

  @Mock
  private IdentView ident3View;

  private PickableHuooPipelineIdentService pickableHuooPipelineIdentService;

  @BeforeEach
  void setUp() throws Exception {
    pickableHuooPipelineIdentService = new PickableHuooPipelineIdentService(pipelineAndIdentViewFactory);

   setupIdentViewMock(ident1View, POINT_A, POINT_B, 1);
   setupIdentViewMock(ident2View, POINT_B, POINT_C, 2);
   setupIdentViewMock(ident3View, POINT_C, POINT_D, 3);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);

  }

  @Test
  void getSortedPickableIdentLocationOptions_whenPipelineFound() {
    var identViewList = List.of(ident3View, ident2View, ident1View);
    when(pipelineAndIdentViewFactory.getPipelineSortedIdentViews(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(identViewList);

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
  void getSortedPickableIdentLocationOptions_whenPipelineNotFound() {

    var options = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

    assertThat(options).isEmpty();
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
  void generatePipelineSectionsFromForm_whenSectionConsistsOnlyOfOnePoint() {
    var identViewList = List.of(ident3View, ident2View, ident1View);
    when(pipelineAndIdentViewFactory.getPipelineSortedIdentViews(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(identViewList);

    var sortedIdentPoints = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

    var form = new DefinePipelineHuooSectionsForm();
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(sortedIdentPoints.get(0).getPickableString(), true),
        new PipelineSectionPointFormInput(sortedIdentPoints.get(0).getPickableString(), false),
        new PipelineSectionPointFormInput(sortedIdentPoints.get(1).getPickableString(), false)
    ));

    var pipelineSections = pickableHuooPipelineIdentService.generatePipelineSectionsFromForm(pwaApplicationDetail, PIPELINE_ID, form);
    assertThat(pipelineSections).containsExactly(
        PipelineSection.from(PIPELINE_ID, 1, PipelineIdentPoint.inclusivePoint(POINT_A), PipelineIdentPoint.inclusivePoint(POINT_A)),
        PipelineSection.from(PIPELINE_ID, 2, PipelineIdentPoint.exclusivePoint(POINT_A), PipelineIdentPoint.inclusivePoint(POINT_B)),
        PipelineSection.from(PIPELINE_ID, 3, PipelineIdentPoint.exclusivePoint(POINT_B), PipelineIdentPoint.inclusivePoint(POINT_D))
    );

  }

  @Test
  void generatePipelineSectionsFromForm_whenSectionCoversMultipleIdentLocation() {
    var identViewList = List.of(ident3View, ident2View, ident1View);
    when(pipelineAndIdentViewFactory.getPipelineSortedIdentViews(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(identViewList);

    var sortedIdentPoints = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID);

    var form = new DefinePipelineHuooSectionsForm();
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(sortedIdentPoints.get(0).getPickableString(), true),
        new PipelineSectionPointFormInput(sortedIdentPoints.get(2).getPickableString(), false),
        new PipelineSectionPointFormInput(sortedIdentPoints.get(3).getPickableString(), true)
    ));

    var pipelineSections = pickableHuooPipelineIdentService.generatePipelineSectionsFromForm(pwaApplicationDetail, PIPELINE_ID, form);
    assertThat(pipelineSections).containsExactly(
        PipelineSection.from(PIPELINE_ID, 1, PipelineIdentPoint.inclusivePoint(POINT_A), PipelineIdentPoint.inclusivePoint(POINT_B)),
        PipelineSection.from(PIPELINE_ID, 2, PipelineIdentPoint.exclusivePoint(POINT_B), PipelineIdentPoint.exclusivePoint(POINT_C)),
        PipelineSection.from(PIPELINE_ID, 3, PipelineIdentPoint.inclusivePoint(POINT_C), PipelineIdentPoint.inclusivePoint(POINT_D))
    );

  }

}