package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadFileDtoRepository {

  List<UploadedFileView> findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                 ApplicationDetailFilePurpose purpose,
                                                                                 ApplicationFileLinkStatus linkStatus);

  UploadedFileView findAsFileViewByAppDetailAndFileIdAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                 String fileId,
                                                                                 ApplicationDetailFilePurpose purpose,
                                                                                 ApplicationFileLinkStatus linkStatus);

  List<PadFile> findAllByAppDetailAndFilePurposeAndIdNotIn(PwaApplicationDetail detail,
                                                           ApplicationDetailFilePurpose purpose,
                                                           Iterable<Integer> padFileIdsToExclude);

  List<PadFile> findAllCurrentFilesByAppDetailAndFilePurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                              ApplicationDetailFilePurpose purpose,
                                                                              ApplicationFileLinkStatus applicationFileLinkStatus);

}
