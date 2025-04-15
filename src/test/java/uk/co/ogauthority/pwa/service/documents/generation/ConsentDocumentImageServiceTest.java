package uk.co.ogauthority.pwa.service.documents.generation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.service.enums.documents.DocumentImageMethod;

@ExtendWith(MockitoExtension.class)
class ConsentDocumentImageServiceTest {

  @Mock
  private FileService fileService;

  private ConsentDocumentImageService base64ImageService;

  private UploadedFile uploadedFile1;
  private UploadedFile uploadedFile2;

  private InputStreamResource fileStream1;
  private InputStreamResource fileStream2;

  private byte[] fileData1;
  private byte[] fileData2;

  @BeforeEach
  void setUp() {
    base64ImageService = new ConsentDocumentImageService(fileService, DocumentImageMethod.BASE_64);

    uploadedFile1 = new UploadedFile();
    uploadedFile1.setId(UUID.randomUUID());
    uploadedFile2 = new UploadedFile();
    uploadedFile2.setId(UUID.randomUUID());

    fileStream1 = mock(InputStreamResource.class);
    fileStream2 = mock(InputStreamResource.class);

    fileData1 = "file1".getBytes();
    fileData2 = "file2".getBytes();
  }

  @Test
  void convertFileToImageSource_base64() throws IOException {
    when(fileService.download(uploadedFile1)).thenReturn(ResponseEntity.ok(fileStream1));

    when(fileStream1.getContentAsByteArray()).thenReturn(fileData1);

    String fileEncoded = Base64.encodeBase64String(fileData1);

    assertThat(base64ImageService.convertFileToImageSource(uploadedFile1))
        .isEqualTo(DocumentImageMethod.BASE_64.getUriPrefix() + fileEncoded);
  }

  @Test
  void convertFilesToImageSourceMap_base64() throws IOException {
    when(fileService.download(uploadedFile1)).thenReturn(ResponseEntity.ok(fileStream1));
    when(fileService.download(uploadedFile2)).thenReturn(ResponseEntity.ok(fileStream2));

    when(fileStream1.getContentAsByteArray()).thenReturn(fileData1);
    when(fileStream2.getContentAsByteArray()).thenReturn(fileData2);

    String file1Encoded = Base64.encodeBase64String(fileData1);
    String file2Encoded = Base64.encodeBase64String(fileData2);

    assertThat(base64ImageService.convertFilesToImageSourceMap(List.of(uploadedFile1, uploadedFile2)))
        .containsOnly(
            entry(String.valueOf(uploadedFile1.getId()), DocumentImageMethod.BASE_64.getUriPrefix() + file1Encoded),
            entry(String.valueOf(uploadedFile2.getId()), DocumentImageMethod.BASE_64.getUriPrefix() + file2Encoded)
        );
  }

}