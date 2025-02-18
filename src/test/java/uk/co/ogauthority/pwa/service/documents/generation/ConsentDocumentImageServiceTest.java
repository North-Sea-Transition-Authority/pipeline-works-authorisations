package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.service.enums.documents.DocumentImageMethod;
import uk.co.ogauthority.pwa.service.fileupload.FileUploadService;

@ExtendWith(MockitoExtension.class)
class ConsentDocumentImageServiceTest {

  @Mock
  private FileUploadService fileUploadService;

  private ConsentDocumentImageService base64ImageService;
  private ConsentDocumentImageService tempFileImageService;

  private UploadedFile uploadedFile1;
  private UploadedFile uploadedFile2;

  private File file1;
  private File file2;

  @BeforeEach
  void setUp() throws SQLException {

    base64ImageService = new ConsentDocumentImageService(fileUploadService, DocumentImageMethod.BASE_64);
    tempFileImageService = new ConsentDocumentImageService(fileUploadService, DocumentImageMethod.TEMP_FILE);

    uploadedFile1 = new UploadedFile("id1", "name1");
    uploadedFile1.setScaledImageData(new SerialBlob(new byte[2]));
    uploadedFile1.setContentType("image/jpg");

    uploadedFile2 = new UploadedFile("id2", "name2");
    uploadedFile2.setFileData(new SerialBlob(new byte[1]));
    uploadedFile2.setContentType("image/png");

    when(fileUploadService.getFilesByIds(any())).thenReturn(List.of(uploadedFile1, uploadedFile2));

    file1 = new File(uploadedFile1.getFileName() + uploadedFile1.getContentType().replace("image/", ""));
    file2 = new File(uploadedFile2.getFileName() + uploadedFile2.getContentType().replace("image/", ""));

  }

  @Test
  void convertFilesToImageSourceMap_base64() throws SQLException {

    var fileIdToBaseUriMap = base64ImageService.convertFilesToImageSourceMap(Set.of("id1", "id2"));

    String file1Encoded = Base64.encodeBase64String(uploadedFile1.getScaledImageData().getBytes(1,
        (int) uploadedFile1.getScaledImageData().length()));

    String file2Encoded = Base64.encodeBase64String(uploadedFile2.getFileData().getBytes(1,
        (int) uploadedFile2.getFileData().length()));

    assertThat(fileIdToBaseUriMap)
        .containsOnly(
            entry("id1", DocumentImageMethod.BASE_64.getUriPrefix() + file1Encoded),
            entry("id2", DocumentImageMethod.BASE_64.getUriPrefix() + file2Encoded)
        );

  }

  @Test
  void convertFilesToImageSourceMap_tempFile() {

    when(fileUploadService.createTempFile(uploadedFile1)).thenReturn(file1);
    when(fileUploadService.createTempFile(uploadedFile2)).thenReturn(file2);

    var fileIdToFileUriMap = tempFileImageService.convertFilesToImageSourceMap(Set.of("id1", "id2"));

    verify(fileUploadService, times(1)).createTempFile(uploadedFile1);
    verify(fileUploadService, times(1)).createTempFile(uploadedFile2);

    assertThat(fileIdToFileUriMap)
        .containsOnly(
            entry("id1", DocumentImageMethod.TEMP_FILE.getUriPrefix() + file1.getAbsolutePath().replace("\\", "/")),
            entry("id2", DocumentImageMethod.TEMP_FILE.getUriPrefix() + file2.getAbsolutePath().replace("\\", "/"))
        );

  }

}