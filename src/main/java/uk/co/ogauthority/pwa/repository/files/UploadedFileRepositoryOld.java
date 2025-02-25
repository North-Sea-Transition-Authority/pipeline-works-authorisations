package uk.co.ogauthority.pwa.repository.files;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFileOld;

@Repository
public interface UploadedFileRepositoryOld extends CrudRepository<UploadedFileOld, String> {

  List<UploadedFileOld> getAllByFileIdIn(Collection<String> fileIds);

}
