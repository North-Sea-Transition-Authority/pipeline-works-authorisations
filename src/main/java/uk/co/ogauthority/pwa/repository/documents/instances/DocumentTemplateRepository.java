package uk.co.ogauthority.pwa.repository.documents.instances;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplate;

@Repository
public interface DocumentTemplateRepository extends CrudRepository<DocumentTemplate, Integer> {

}
