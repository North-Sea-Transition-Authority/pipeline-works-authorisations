package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingFile;

@Repository
public interface PadPipelineCrossingFileRepository extends CrudRepository<PadPipelineCrossingFile, Integer> {

  List<PadPipelineCrossingFile> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadPipelineCrossingFile> findByPwaApplicationDetailAndFileId(PwaApplicationDetail pwaApplicationDetail,
                                                                        String fileId);

  int countAllByPwaApplicationDetailAndFileLinkStatus(PwaApplicationDetail pwaApplicationDetail,
                                                      ApplicationFileLinkStatus fileLinkStatus);

}
