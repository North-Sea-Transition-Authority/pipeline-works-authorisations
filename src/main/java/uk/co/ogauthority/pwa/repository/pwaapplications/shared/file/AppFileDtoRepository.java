package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Repository
public interface AppFileDtoRepository {

  List<UploadedFileView> findAllAsFileViewByAppAndPurposeAndFileLinkStatus(PwaApplication application,
                                                                           AppFilePurpose purpose,
                                                                           ApplicationFileLinkStatus linkStatus);

  UploadedFileView findAsFileViewByAppAndFileIdAndPurposeAndFileLinkStatus(PwaApplication application,
                                                                           String fileId,
                                                                           AppFilePurpose purpose,
                                                                           ApplicationFileLinkStatus linkStatus);

  List<AppFile> findAllByAppAndFilePurposeAndIdNotIn(PwaApplication application,
                                                     AppFilePurpose purpose,
                                                     Iterable<Integer> appFileIdsToExclude);

  List<AppFile> findAllCurrentFilesByAppAndFilePurposeAndFileLinkStatus(PwaApplication application,
                                                                        AppFilePurpose purpose,
                                                                        ApplicationFileLinkStatus applicationFileLinkStatus);

}
