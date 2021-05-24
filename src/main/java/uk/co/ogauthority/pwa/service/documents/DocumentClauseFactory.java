package uk.co.ogauthority.pwa.service.documents;

import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.SectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;

@Service
public class DocumentClauseFactory {

  public SectionClause createSectionClause(PwaDocumentType documentType) {

    if (documentType == PwaDocumentType.INSTANCE) {
      return new DocumentInstanceSectionClause();
    } else if (documentType == PwaDocumentType.TEMPLATE) {
      return new DocumentTemplateSectionClause();
    } else {
      throw new RuntimeException(String.format("Unexpected document type [%s] passed to createSectionClause", documentType.name()));
    }

  }

  public SectionClauseVersion createSectionClauseVersion(PwaDocumentType documentType) {

    if (documentType == PwaDocumentType.INSTANCE) {
      return new DocumentInstanceSectionClauseVersion();
    } else if (documentType == PwaDocumentType.TEMPLATE) {
      return new DocumentTemplateSectionClauseVersion();
    } else {
      throw new RuntimeException(String.format("Unexpected document type [%s] passed to createSectionClauseVersion", documentType.name()));
    }

  }

}
