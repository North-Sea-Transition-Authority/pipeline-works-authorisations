package uk.co.ogauthority.pwa.features.application.files;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFileRepository extends CrudRepository<PadFile, Integer>, PadFileDtoRepository {

  List<PadFile> findAllByPwaApplicationDetailAndPurpose(PwaApplicationDetail detail, ApplicationDetailFilePurpose purpose);

  Optional<PadFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail detail, String fileId);

  List<PadFile> findAllByPwaApplicationDetailAndFileLinkStatus(PwaApplicationDetail detail, ApplicationFileLinkStatus status);

}
