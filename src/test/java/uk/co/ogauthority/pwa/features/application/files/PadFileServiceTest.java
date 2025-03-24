package uk.co.ogauthority.pwa.features.application.files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
public class PadFileServiceTest {

  private final String FILE_ID = String.valueOf(UUID.randomUUID());

  @Mock
  private PadFileRepository padFileRepository;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PadFileService padFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadFile file;

  @BeforeEach
  public void setUp() {

    padFileService = new PadFileService(padFileRepository, entityCopyingService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    file = new PadFile();
    file.setFileId(UUID.fromString(FILE_ID));
    file.setPurpose(ApplicationDetailFilePurpose.PIPELINE_DRAWINGS);
    file.setPwaApplicationDetail(pwaApplicationDetail);
  }


  @Test
  public void processFileDeletion_verifyServiceInteractions() {

    padFileService.processFileDeletion(file);

    verify(padFileRepository, times(1)).delete(file);

  }

  @Test
  public void cleanupFiles_filesToKeep() {

    var file4 = new PadFile();
    file4.setPurpose(ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    file4.setId(4);

    var file5 = new PadFile();
    file5.setPurpose(ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    file5.setId(5);

    when(padFileRepository.findAllByAppDetailAndFilePurposeAndIdNotIn(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, List.of(1, 2, 3)))
        .thenReturn(List.of(file4, file5));

    padFileService.cleanupFiles(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, List.of(1, 2, 3));

    verify(padFileRepository, times(1)).deleteAll(List.of(file4, file5));

  }

  @Test
  public void cleanupFiles_noFilesToKeep() {

    var file1 = new PadFile();
    file1.setPurpose(ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    file1.setId(1);

    var file2 = new PadFile();
    file2.setPurpose(ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    file2.setId(2);

    var file3 = new PadFile();
    file3.setPurpose(ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS);
    file3.setId(3);

    when(padFileRepository.findAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS))
        .thenReturn(List.of(file1, file2, file3));

    padFileService.cleanupFiles(pwaApplicationDetail, ApplicationDetailFilePurpose.DEPOSIT_DRAWINGS, List.of());

    verify(padFileRepository, times(1)).deleteAll(List.of(file1, file2, file3));

  }

  @Test
  public void copyPadFilesToPwaApplicationDetail_serviceInteractions() {
    var newDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 20, 21);

    var copiedFiles = padFileService.copyPadFilesToPwaApplicationDetail(
        pwaApplicationDetail,
        newDetail,
        ApplicationDetailFilePurpose.PIPELINE_DRAWINGS,
        ApplicationFileLinkStatus.FULL);

    verify(entityCopyingService, times(1)).duplicateEntitiesAndSetParent(
        any(),
        eq(newDetail),
        eq(PadFile.class)
    );


  }
}