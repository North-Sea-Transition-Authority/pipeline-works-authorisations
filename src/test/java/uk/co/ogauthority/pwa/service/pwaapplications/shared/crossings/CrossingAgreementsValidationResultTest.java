package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsSection;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult;

@RunWith(MockitoJUnitRunner.class)
public class CrossingAgreementsValidationResultTest {

  @Test
  public void CrossingAgreementsValidationResult_whenNoValidSections(){
    var result = new CrossingAgreementsValidationResult(Set.of());

    assertThat(result.isCrossingAgreementsValid()).isFalse();

    for(CrossingAgreementsSection section : CrossingAgreementsSection.values()){
      assertThat(result.isSectionValid(section)).isFalse();
      assertThat(result.isSectionValid(section.name())).isFalse();
    }
  }


  @Test
  public void CrossingAgreementsValidationResult_whenSomeSectionIsValid(){
    var result = new CrossingAgreementsValidationResult(Set.of(CrossingAgreementsSection.BLOCK_CROSSINGS));

    assertThat(result.isCrossingAgreementsValid()).isFalse();

    assertThat(result.isSectionValid(CrossingAgreementsSection.BLOCK_CROSSINGS)).isTrue();
    assertThat(result.isSectionValid(CrossingAgreementsSection.BLOCK_CROSSINGS.name())).isTrue();
    //other sections not valid
    var otherSections = CrossingAgreementsSection.stream()
        .filter(o -> !o.equals(CrossingAgreementsSection.BLOCK_CROSSINGS))
        .collect(Collectors.toSet());

    for(CrossingAgreementsSection section : otherSections){
      assertThat(result.isSectionValid(section)).isFalse();
      assertThat(result.isSectionValid(section.name())).isFalse();
    }
  }

  @Test
  public void CrossingAgreementsValidationResult_whenAllSectionsValid(){
    var result = new CrossingAgreementsValidationResult(EnumSet.allOf(CrossingAgreementsSection.class));

    assertThat(result.isCrossingAgreementsValid()).isTrue();
    for(CrossingAgreementsSection section : CrossingAgreementsSection.values()){
      assertThat(result.isSectionValid(section)).isTrue();
      assertThat(result.isSectionValid(section.name())).isTrue();
    }
  }

}
