package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;

@Repository
public interface PadFileDtoRepository {

  List<UploadedFileView> findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                 ApplicationFilePurpose purpose,
                                                                                 ApplicationFileLinkStatus linkStatus);

}
