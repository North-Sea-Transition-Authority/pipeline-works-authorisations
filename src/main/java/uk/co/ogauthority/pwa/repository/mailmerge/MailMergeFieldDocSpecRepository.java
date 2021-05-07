package uk.co.ogauthority.pwa.repository.mailmerge;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeFieldDocSpec;

@Repository
public interface MailMergeFieldDocSpecRepository extends CrudRepository<MailMergeFieldDocSpec, Integer> {

  List<MailMergeFieldDocSpec> getAllByDocumentSpec(DocumentSpec documentSpec);

}
