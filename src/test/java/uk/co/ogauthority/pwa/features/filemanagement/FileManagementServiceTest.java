package uk.co.ogauthority.pwa.features.filemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@ExtendWith(MockitoExtension.class)
class FileManagementServiceTest {

  @Mock
  private FileService fileService;
  @InjectMocks
  private FileManagementService fileManagementService;

  @Captor
  private ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptor;

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.PROJECT_INFORMATION;
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final String FILE_DESCRIPTION = "file description";
  private static final String USAGE_TYPE = PwaApplicationDetail.class.getSimpleName();
  private static final String USAGE_ID = "1";

  @Test
  void saveFiles() {
    var documentForms = new ArrayList<UploadedFileForm>();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);

    when(fileService.findAll(Collections.singletonList(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    var form = new UploadedFileForm();
    form.setFileId(FILE_ID);
    form.setFileDescription(FILE_DESCRIPTION);
    documentForms.add(form);

    fileManagementService.saveFiles(documentForms, USAGE_ID, USAGE_TYPE, DOCUMENT_TYPE);

    verify(fileService).updateUsageAndDescription(
        eq(uploadedFile),
        fileUsageFunctionCaptor.capture(),
        eq(FILE_DESCRIPTION)
    );
    var fileUsage = fileUsageFunctionCaptor.getValue().apply(FileUsage.newBuilder());
    assertThat(fileUsage)
        .extracting(
            FileUsage::usageId,
            FileUsage::usageType,
            FileUsage::documentType
        )
        .containsExactly(
            USAGE_ID,
            USAGE_TYPE,
            DOCUMENT_TYPE.name()
        );
  }

  @Test
  void saveFiles_withFilesLinkedToAnotherApplication() {
    var fileForms = new ArrayList<UploadedFileForm>();

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(FILE_ID);
    uploadedFile.setUsageId(UUID.randomUUID().toString());

    when(fileService.findAll(Collections.singletonList(FILE_ID))).thenReturn(Collections.singletonList(uploadedFile));

    var form = new UploadedFileForm();
    form.setFileId(FILE_ID);
    form.setFileDescription(FILE_DESCRIPTION);
    fileForms.add(form);

    assertThatThrownBy(
        () -> fileManagementService.saveFiles(fileForms, USAGE_ID, USAGE_TYPE, DOCUMENT_TYPE))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("File %s does not exist for PwaApplicationDetail %s".formatted(FILE_ID, USAGE_ID));
  }

  @Test
  void throwIfFileDoesNotBelongToUsageType() {
    assertDoesNotThrow(() ->
        fileManagementService.throwIfFileDoesNotBelongToUsageType(new UploadedFile(), DOCUMENT_TYPE.name(), USAGE_ID, USAGE_TYPE)
    );
  }

  @Test
  void getFileUploadComponentAttributesBuilder() {
    var existingFiles = Collections.<UploadedFileForm>emptyList();

    var attributesBuilder = FileUploadComponentAttributes.newBuilder()
        .withMaximumSize(DataSize.ofMegabytes(50))
        .withAllowedExtensions(Collections.singleton("pdf"));
    when(fileService.getFileUploadAttributes()).thenReturn(attributesBuilder);

    var result = fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, DOCUMENT_TYPE).build();

    assertThat(result)
        .extracting(
            FileUploadComponentAttributes::uploadUrl,
            FileUploadComponentAttributes::downloadUrl,
            FileUploadComponentAttributes::deleteUrl
        )
        .containsOnlyNulls();

    assertThat(result)
        .extracting(
            FileUploadComponentAttributes::existingFiles
        ).isEqualTo(
            existingFiles
        );
  }

  @Test
  void getFileNotFoundException() {

    var result = fileManagementService.getFileNotFoundException(FILE_ID, USAGE_TYPE, USAGE_ID);

    assertThat(result)
        .extracting(
            ResponseStatusException::getStatusCode,
            ResponseStatusException::getReason
        )
        .containsExactly(
            HttpStatus.NOT_FOUND,
            "File %s does not exist for %s %s".formatted(FILE_ID, USAGE_TYPE, USAGE_ID)
        );
  }
}