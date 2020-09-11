package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;

@Repository
public interface PadFileDtoRepository {

  List<UploadedFileView> findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                 ApplicationFilePurpose purpose,
                                                                                 ApplicationFileLinkStatus linkStatus);

  UploadedFileView findAsFileViewByAppDetailAndFileIdAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                 String fileId,
                                                                                 ApplicationFilePurpose purpose,
                                                                                 ApplicationFileLinkStatus linkStatus);

  List<PadFile> findAllByAppDetailAndFilePurposeAndIdNotIn(PwaApplicationDetail detail,
                                                           ApplicationFilePurpose purpose,
                                                           Iterable<Integer> padFileIdsToExclude);

  List<PadFile> findAllCurrentFilesByAppDetailAndFilePurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                              ApplicationFilePurpose purpose,
                                                                              ApplicationFileLinkStatus applicationFileLinkStatus);

}
