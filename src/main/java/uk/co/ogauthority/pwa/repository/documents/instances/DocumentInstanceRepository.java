package uk.co.ogauthority.pwa.repository.documents.instances;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;

@Repository
public interface DocumentInstanceRepository extends CrudRepository<DocumentInstance, Integer> {

  Optional<DocumentInstance> findByPwaApplicationAndDocumentTemplate_Mnem(PwaApplication application,
                                                                          DocumentTemplateMnem templateMnem);

}
