package uk.co.ogauthority.pwa.model.entity.documents;

import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;

public interface SectionClause {

  Integer getId();

  DocumentTemplateSection getSection();

  void setSection(DocumentTemplateSection section);

}
