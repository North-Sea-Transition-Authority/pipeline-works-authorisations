package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadMedianLineCrossingFile;

@Repository
public interface PadMedianLineCrossingFileRepository extends CrudRepository<PadMedianLineCrossingFile, Integer> {

  List<PadMedianLineCrossingFile> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadMedianLineCrossingFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                          String fileId);

  int countAllByPwaApplicationDetailAndFileLinkStatus(PwaApplicationDetail pwaApplicationDetail,
                                                      ApplicationFileLinkStatus fileLinkStatus);

}
