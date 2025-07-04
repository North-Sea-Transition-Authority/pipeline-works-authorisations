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

    return switch (documentType) {
      case INSTANCE -> new DocumentInstanceSectionClause();
      case TEMPLATE -> new DocumentTemplateSectionClause();
      case null -> throw new IllegalArgumentException("Null document type passed to createSectionClauseVersion");
    };

  }

  public SectionClauseVersion createSectionClauseVersion(PwaDocumentType documentType) {

    return switch (documentType) {
      case INSTANCE -> new DocumentInstanceSectionClauseVersion();
      case TEMPLATE -> new DocumentTemplateSectionClauseVersion();
      case null -> throw new IllegalArgumentException("Null document type passed to createSectionClauseVersion");
    };

  }

}
