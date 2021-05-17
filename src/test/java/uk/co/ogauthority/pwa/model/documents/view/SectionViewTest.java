package uk.co.ogauthority.pwa.model.documents.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.SectionType;

public class SectionViewTest {

  private SectionView sectionView;

  @Before
  public void setUp() throws Exception {

    sectionView = new SectionView();
    sectionView.setName("test");

  }

  @Test
  public void addAndRemoveClauseAllowed_openingParagraph() {

    sectionView.setSectionType(SectionType.OPENING_PARAGRAPH);

    assertThat(sectionView.addAndRemoveClauseAllowed()).isFalse();

  }

  @Test
  public void addAndRemoveClauseAllowed_notOpeningParagraph() {

    sectionView.setSectionType(SectionType.CLAUSE_LIST);

    assertThat(sectionView.addAndRemoveClauseAllowed()).isTrue();

  }

}