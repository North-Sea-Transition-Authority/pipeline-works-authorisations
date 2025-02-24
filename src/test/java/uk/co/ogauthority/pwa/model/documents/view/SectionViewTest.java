package uk.co.ogauthority.pwa.model.documents.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;

class SectionViewTest {

  private SectionView sectionView;

  @BeforeEach
  void setUp() throws Exception {

    sectionView = new SectionView();
    sectionView.setName("test");

  }

  @Test
  void addAndRemoveClauseAllowed_openingParagraph() {

    sectionView.setSectionType(SectionType.OPENING_PARAGRAPH);

    assertThat(sectionView.addAndRemoveClauseAllowed()).isFalse();

  }

  @Test
  void addAndRemoveClauseAllowed_notOpeningParagraph() {

    sectionView.setSectionType(SectionType.CLAUSE_LIST);

    assertThat(sectionView.addAndRemoveClauseAllowed()).isTrue();

  }

}