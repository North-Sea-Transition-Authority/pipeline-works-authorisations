package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadBlockCrossingFile;

@Repository
public interface PadBlockCrossingFileRepository extends CrudRepository<PadBlockCrossingFile, Integer> {

  List<PadBlockCrossingFile> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadBlockCrossingFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                     String fileId);

  int countAllByPwaApplicationDetailAndFileLinkStatus(PwaApplicationDetail pwaApplicationDetail,
                                                      ApplicationFileLinkStatus status);

}
