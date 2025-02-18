package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class DefinePipelineHuooSectionsFormTest {

  private PickableIdentLocationOption firstIdentLocation;

  @BeforeEach
  void setup(){
    firstIdentLocation = new PickableIdentLocationOption(1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, "POINT 1");
  }

  @Test
  void resetSectionPoints_when0Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    assertThrows(IllegalArgumentException.class, () ->
      form.resetSectionPoints(0, firstIdentLocation));
  }

  @Test
  void resetSectionPoints_whenLessThanZero0Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    assertThrows(IllegalArgumentException.class, () ->
      form.resetSectionPoints(-1, firstIdentLocation));
  }

  @Test
  void resetSectionPoints_when1Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    form.resetSectionPoints(1,  firstIdentLocation);

    assertThat(form.getPipelineSectionPoints()).hasSize(1);
    assertThat(form.getPipelineSectionPoints().get(0).getPointIncludedInSection()).isTrue();
    assertThat(form.getPipelineSectionPoints().get(0).getPickedPipelineIdentString()).isEqualTo(firstIdentLocation.getPickableString());
  }

  @Test
  void resetSectionPoints_when3Sections() {
    var form = new DefinePipelineHuooSectionsForm();
    form.resetSectionPoints(3,  firstIdentLocation);

    assertThat(form.getPipelineSectionPoints()).hasSize(3);
    assertThat(form.getPipelineSectionPoints().get(0).getPointIncludedInSection()).isTrue();
    assertThat(form.getPipelineSectionPoints().get(0).getPickedPipelineIdentString()).isEqualTo(firstIdentLocation.getPickableString());

    assertThat(form.getPipelineSectionPoints().get(1).getPointIncludedInSection()).isNull();
    assertThat(form.getPipelineSectionPoints().get(1).getPickedPipelineIdentString()).isNull();

    assertThat(form.getPipelineSectionPoints().get(2).getPointIncludedInSection()).isNull();
    assertThat(form.getPipelineSectionPoints().get(2).getPickedPipelineIdentString()).isNull();

  }
}