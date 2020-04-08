package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadBlockCrossingFile;

public interface PadBlockCrossingFileRepository extends CrudRepository<PadBlockCrossingFile, Integer> {

  List<PadBlockCrossingFile> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadBlockCrossingFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                     String fileId);

}
