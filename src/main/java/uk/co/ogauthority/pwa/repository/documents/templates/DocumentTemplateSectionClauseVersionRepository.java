package uk.co.ogauthority.pwa.repository.documents.templates;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;

@Repository
public interface DocumentTemplateSectionClauseVersionRepository extends CrudRepository<DocumentTemplateSectionClauseVersion, Integer> {

  @EntityGraph(attributePaths = {
      "documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentTemplateSectionClause.documentTemplateSection.documentTemplate",
  })
  List<DocumentTemplateSectionClauseVersion> getAllByDocumentTemplateSectionClause_DocumentTemplateSectionInAndTipFlagIsTrueAndStatusIs(
      Collection<DocumentTemplateSection> section,
      SectionClauseVersionStatus status);

  @EntityGraph(attributePaths = {
      "documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentTemplateSectionClause.documentTemplateSection.documentTemplate",
  })
  Optional<DocumentTemplateSectionClauseVersion> findByDocumentTemplateSectionClause_IdAndTipFlagIsTrue(Integer id);

  @EntityGraph(attributePaths = {
      "documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentTemplateSectionClause.documentTemplateSection.documentTemplate",
  })
  List<DocumentTemplateSectionClauseVersion>
      findByDocumentTemplateSectionClause_DocumentTemplateSectionAndParentDocumentTemplateSectionClause(
      DocumentTemplateSection documentTemplateSection,
      DocumentTemplateSectionClause parentDocumentTemplateSectionClause
  );

  @EntityGraph(attributePaths = {
      "documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentTemplateSectionClause.documentTemplateSection.documentTemplate",
  })
  List<DocumentTemplateSectionClauseVersion>
      findByDocumentTemplateSectionClause_DocumentTemplateSectionAndParentDocumentTemplateSectionClauseIn(
      DocumentTemplateSection documentTemplateSection,
      Collection<DocumentTemplateSectionClause> parentDocumentTemplateSectionClauses
  );

}
