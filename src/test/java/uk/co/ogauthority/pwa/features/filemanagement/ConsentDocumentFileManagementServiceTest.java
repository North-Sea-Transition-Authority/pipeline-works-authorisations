package uk.co.ogauthority.pwa.features.filemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@ExtendWith(MockitoExtension.class)
class ConsentDocumentFileManagementServiceTest {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CONSENT_DOCUMENT;
  private static final String USAGE_TYPE = "PwaConsent";

  @Mock
  private FileManagementService fileManagementService;

  @Mock
  private FileService fileService;

  @InjectMocks
  private ConsentDocumentFileManagementService consentDocumentFileManagementService;

  @Test
  void saveConsentDocument() {
  var file = new UploadedFileForm();
  var consent = new PwaConsent();
  consent.setId(1);

  consentDocumentFileManagementService.saveConsentDocument(file, consent);

  verify(fileManagementService).saveFiles(List.of(file), String.valueOf(consent.getId()), USAGE_TYPE, FileDocumentType.CONSENT_DOCUMENT);
  }

  @Test
  void getUploadedConsentDocuments() {
    var consent = new PwaConsent();
    consent.setId(1);

    var uploadedFiles = Collections.<UploadedFile>emptyList();
    when(fileService.findAll(String.valueOf(consent.getId()), USAGE_TYPE,  DOCUMENT_TYPE.name()))
        .thenReturn(uploadedFiles);

    assertThat(consentDocumentFileManagementService.getUploadedConsentDocuments(consent))
        .isEqualTo(uploadedFiles);
  }
}