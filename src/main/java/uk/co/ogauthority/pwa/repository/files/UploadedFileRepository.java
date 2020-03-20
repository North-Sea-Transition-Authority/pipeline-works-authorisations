package uk.co.ogauthority.pwa.repository.files;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;

@Repository
public interface UploadedFileRepository extends CrudRepository<UploadedFile, String> {

}
