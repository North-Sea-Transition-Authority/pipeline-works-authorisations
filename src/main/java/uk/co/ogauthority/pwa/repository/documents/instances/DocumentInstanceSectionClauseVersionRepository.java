package uk.co.ogauthority.pwa.repository.documents.instances;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;

@Repository
public interface DocumentInstanceSectionClauseVersionRepository extends CrudRepository<DocumentInstanceSectionClauseVersion, Integer> {

  @EntityGraph(attributePaths = {
      "documentInstanceSectionClause.documentInstance.pwaApplication.masterPwa",
      "documentInstanceSectionClause.documentInstance.documentTemplate",
      "documentInstanceSectionClause.documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "documentInstanceSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentInstanceSectionClause.documentInstance.pwaApplication.masterPwa",
      "parentDocumentInstanceSectionClause.documentInstance.documentTemplate",
      "parentDocumentInstanceSectionClause.documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentInstanceSectionClause.documentTemplateSection.documentTemplate",
  })
  List<DocumentInstanceSectionClauseVersion> findAllByDocumentInstanceSectionClauseIn(
      Collection<DocumentInstanceSectionClause> clauses);

  @EntityGraph(attributePaths = {
      "documentInstanceSectionClause.documentInstance.pwaApplication.masterPwa",
      "documentInstanceSectionClause.documentInstance.documentTemplate",
      "documentInstanceSectionClause.documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "documentInstanceSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentInstanceSectionClause.documentInstance.pwaApplication.masterPwa",
      "parentDocumentInstanceSectionClause.documentInstance.documentTemplate",
      "parentDocumentInstanceSectionClause.documentTemplateSectionClause.documentTemplateSection.documentTemplate",
      "parentDocumentInstanceSectionClause.documentTemplateSection.documentTemplate",
  })
  Optional<DocumentInstanceSectionClauseVersion> findByDocumentInstanceSectionClause_IdAndTipFlagIsTrue(Integer clauseId);

  List<DocumentInstanceSectionClauseVersion> findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(
      DocumentInstance documentInstance,
      DocumentInstanceSectionClause parentClause);

  List<DocumentInstanceSectionClauseVersion> findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClauseIn(
      DocumentInstance documentInstance,
      Collection<DocumentInstanceSectionClause> parentClauses);


}
