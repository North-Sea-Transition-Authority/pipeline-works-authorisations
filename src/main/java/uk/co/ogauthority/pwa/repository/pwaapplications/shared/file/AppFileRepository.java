package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Repository
public interface AppFileRepository extends CrudRepository<AppFile, Integer>, AppFileDtoRepository {

  List<AppFile> findAllByPwaApplicationAndPurpose(PwaApplication application, AppFilePurpose purpose);

  Optional<AppFile> findByPwaApplicationAndFileId(PwaApplication application, String fileId);

  List<AppFile> findAllByPwaApplicationAndPurposeAndFileIdIn(PwaApplication application,
                                                             AppFilePurpose purpose,
                                                             Collection<String> fileIds);

}
