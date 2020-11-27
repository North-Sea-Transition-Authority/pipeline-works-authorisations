package uk.co.ogauthority.pwa.repository.files;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;

@Repository
public interface UploadedFileRepository extends CrudRepository<UploadedFile, String> {

  List<UploadedFile> getAllByFileIdIn(Collection<String> fileIds);

}
