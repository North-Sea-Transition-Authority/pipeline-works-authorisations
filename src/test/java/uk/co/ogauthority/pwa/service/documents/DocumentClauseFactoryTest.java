package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;

@RunWith(MockitoJUnitRunner.class)
public class DocumentClauseFactoryTest {

  private DocumentClauseFactory documentClauseFactory;

  @Before
  public void setUp() throws Exception {

    documentClauseFactory = new DocumentClauseFactory();

  }

  @Test
  public void createClause_template() {

    var clause = documentClauseFactory.createSectionClause(PwaDocumentType.TEMPLATE);

    assertThat(clause).isInstanceOf(DocumentTemplateSectionClause.class);

  }

  @Test
  public void createClause_instance() {

    var clause = documentClauseFactory.createSectionClause(PwaDocumentType.INSTANCE);

    assertThat(clause).isInstanceOf(DocumentInstanceSectionClause.class);

  }

  @Test
  public void createClauseVersion_template() {

    var clauseVersion = documentClauseFactory.createSectionClauseVersion(PwaDocumentType.TEMPLATE);

    assertThat(clauseVersion).isInstanceOf(DocumentTemplateSectionClauseVersion.class);

  }

  @Test
  public void createClauseVersion_instance() {

    var clauseVersion = documentClauseFactory.createSectionClauseVersion(PwaDocumentType.INSTANCE);

    assertThat(clauseVersion).isInstanceOf(DocumentInstanceSectionClauseVersion.class);

  }

}