package uk.co.ogauthority.pwa.repository.documents.templates;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;

@Repository
public interface DocumentTemplateSectionClauseVersionRepository extends CrudRepository<DocumentTemplateSectionClauseVersion, Integer> {

  @EntityGraph(attributePaths = {
      "documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentTemplateSectionClause.documentTemplateSection.documentTemplate",
  })
  List<DocumentTemplateSectionClauseVersion> getAllByDocumentTemplateSectionClause_DocumentTemplateSectionInAndTipFlagIsTrue(
      Collection<DocumentTemplateSection> section);

}
