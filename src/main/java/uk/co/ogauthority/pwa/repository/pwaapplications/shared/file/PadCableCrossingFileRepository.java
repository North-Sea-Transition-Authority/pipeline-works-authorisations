package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossingFile;

@Repository
public interface PadCableCrossingFileRepository extends CrudRepository<PadCableCrossingFile, Integer> {

  List<PadCableCrossingFile> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadCableCrossingFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                          String fileId);

}
