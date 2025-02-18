package uk.co.ogauthority.pwa.service.documents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;

@ExtendWith(MockitoExtension.class)
class DocumentClauseFactoryTest {

  private DocumentClauseFactory documentClauseFactory;

  @BeforeEach
  void setUp() throws Exception {

    documentClauseFactory = new DocumentClauseFactory();

  }

  @Test
  void createClause_template() {

    var clause = documentClauseFactory.createSectionClause(PwaDocumentType.TEMPLATE);

    assertThat(clause).isInstanceOf(DocumentTemplateSectionClause.class);

  }

  @Test
  void createClause_instance() {

    var clause = documentClauseFactory.createSectionClause(PwaDocumentType.INSTANCE);

    assertThat(clause).isInstanceOf(DocumentInstanceSectionClause.class);

  }

  @Test
  void createClauseVersion_template() {

    var clauseVersion = documentClauseFactory.createSectionClauseVersion(PwaDocumentType.TEMPLATE);

    assertThat(clauseVersion).isInstanceOf(DocumentTemplateSectionClauseVersion.class);

  }

  @Test
  void createClauseVersion_instance() {

    var clauseVersion = documentClauseFactory.createSectionClauseVersion(PwaDocumentType.INSTANCE);

    assertThat(clauseVersion).isInstanceOf(DocumentInstanceSectionClauseVersion.class);

  }

}