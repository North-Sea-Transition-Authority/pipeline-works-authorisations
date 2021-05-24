package uk.co.ogauthority.pwa.repository.documents.templates;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClause;

@Repository
public interface DocumentTemplateSectionClauseRepository extends CrudRepository<DocumentTemplateSectionClause, Integer> {



}
