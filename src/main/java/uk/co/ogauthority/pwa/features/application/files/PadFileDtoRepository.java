package uk.co.ogauthority.pwa.features.application.files;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFileDtoRepository {
  List<PadFile> findAllByAppDetailAndFilePurposeAndIdNotIn(PwaApplicationDetail detail,
                                                           ApplicationDetailFilePurpose purpose,
                                                           Iterable<Integer> padFileIdsToExclude);

  List<PadFile> findAllCurrentFilesByAppDetailAndFilePurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                              ApplicationDetailFilePurpose purpose,
                                                                              ApplicationFileLinkStatus applicationFileLinkStatus);

}
