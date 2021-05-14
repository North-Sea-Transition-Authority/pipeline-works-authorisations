package uk.co.ogauthority.pwa.repository.mailmerge;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;

@Repository
public interface MailMergeFieldRepository extends CrudRepository<MailMergeField, MailMergeFieldMnem> {

  List<MailMergeField> findAllByMnemIn(Collection<MailMergeFieldMnem> mnems);

}
