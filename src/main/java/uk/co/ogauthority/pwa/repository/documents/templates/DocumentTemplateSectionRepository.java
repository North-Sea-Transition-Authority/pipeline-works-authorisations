package uk.co.ogauthority.pwa.repository.documents.templates;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.enums.documents.DocumentTemplateSectionStatus;

@Repository
public interface DocumentTemplateSectionRepository extends CrudRepository<DocumentTemplateSection, Integer> {

  @EntityGraph(attributePaths = { "documentTemplate" })
  List<DocumentTemplateSection> getAllByDocumentTemplate_MnemAndStatusIs(DocumentTemplateMnem mnem,
                                                                         DocumentTemplateSectionStatus sectionStatus);

}
