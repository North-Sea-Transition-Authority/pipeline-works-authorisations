package uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrossingAgreementsValidationResultTest {

  @Test
  void CrossingAgreementsValidationResult_whenNoValidSections(){
    var result = new CrossingAgreementsValidationResult(Set.of());

    assertThat(result.isCrossingAgreementsValid()).isFalse();

    for(CrossingAgreementsSection section : CrossingAgreementsSection.values()){
      assertThat(result.isSectionValid(section)).isFalse();
      assertThat(result.isSectionValid(section.name())).isFalse();
    }
  }


  @Test
  void CrossingAgreementsValidationResult_whenSomeSectionIsValid(){
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
  void CrossingAgreementsValidationResult_whenAllSectionsValid(){
    var result = new CrossingAgreementsValidationResult(EnumSet.allOf(CrossingAgreementsSection.class));

    assertThat(result.isCrossingAgreementsValid()).isTrue();
    for(CrossingAgreementsSection section : CrossingAgreementsSection.values()){
      assertThat(result.isSectionValid(section)).isTrue();
      assertThat(result.isSectionValid(section.name())).isTrue();
    }
  }

}
