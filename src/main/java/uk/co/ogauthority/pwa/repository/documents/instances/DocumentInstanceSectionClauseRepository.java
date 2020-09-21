package uk.co.ogauthority.pwa.repository.documents.instances;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;

@Repository
public interface DocumentInstanceSectionClauseRepository extends CrudRepository<DocumentInstanceSectionClause, Integer> {

  @EntityGraph(attributePaths = { "documentInstance", "documentTemplateSectionClause" })
  List<DocumentInstanceSectionClause> findAllByDocumentInstance(DocumentInstance documentInstance);

}
