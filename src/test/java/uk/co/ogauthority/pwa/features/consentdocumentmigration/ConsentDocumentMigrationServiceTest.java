package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.config.DevtoolsProperties;
import uk.co.ogauthority.pwa.features.filemanagement.ConsentDocumentFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.s3.PwaS3FileService;
import uk.co.ogauthority.pwa.features.filemanagement.s3.S3File;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;

@ActiveProfiles("devtools")
@ExtendWith(MockitoExtension.class)
class ConsentDocumentMigrationServiceTest {

  @Mock
  private PwaS3FileService pwaS3FileService;

  @Mock
  private FileUploadProperties fileUploadProperties;

  private final DevtoolsProperties devtoolsProperties = new DevtoolsProperties("pwa-migration", "csv-key");

  @Mock
  private DocumentMigrationRecordRepository documentMigrationRecordRepository;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private FileService fileService;

  @Mock
  private ConsentDocumentFileManagementService consentDocumentFileManagementService;

  private ConsentDocumentMigrationService consentDocumentMigrationService;

  @Captor
  private ArgumentCaptor<DocumentMigrationRecord> documentMigrationRecordCaptor;

  private FileUploadProperties.S3 s3Properties;

  @BeforeEach
  void setUp() {
    consentDocumentMigrationService = new ConsentDocumentMigrationService(
        pwaS3FileService,
        fileUploadProperties,
        devtoolsProperties,
        documentMigrationRecordRepository,
        pwaConsentService,
        fileService,
        consentDocumentFileManagementService
    );

    s3Properties = new FileUploadProperties.S3(
        "key",
        "token",
        "endpoint",
        "eu-west-2",
        "pwa-dev",
        true,
        new FileUploadProperties.S3.Proxy("host", 1234)
    );
  }

  @Test
  void verify() throws S3Exception {
    when(fileUploadProperties.s3()).thenReturn(s3Properties);

    when(pwaS3FileService.downloadFile(devtoolsProperties.migrationS3Bucket(), devtoolsProperties.migrationCsvFileKey()))
        .thenReturn(new ByteArrayInputStream(new byte[1]));

    assertThatNoException().isThrownBy(() -> consentDocumentMigrationService.verify());

    Mockito.verify(pwaS3FileService).verifyBucketOrThrow(s3Properties.defaultBucket());
    Mockito.verify(pwaS3FileService).verifyBucketOrThrow(devtoolsProperties.migrationS3Bucket());
  }

  @Test
  void verify_missingBucket() throws S3Exception {
    when(fileUploadProperties.s3()).thenReturn(s3Properties);

    doThrow(new S3Exception("")).when(pwaS3FileService).verifyBucketOrThrow(s3Properties.defaultBucket());

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> consentDocumentMigrationService.verify());
  }

  @Test
  void verify_missingCsv() throws S3Exception {
    when(fileUploadProperties.s3()).thenReturn(s3Properties);

    doThrow(new S3Exception("")).when(pwaS3FileService).downloadFile(devtoolsProperties.migrationS3Bucket(), devtoolsProperties.migrationCsvFileKey());

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> consentDocumentMigrationService.verify());
  }

  @Test
  void verify_listFilesFail() throws S3Exception {
    when(fileUploadProperties.s3()).thenReturn(s3Properties);
    when(pwaS3FileService.downloadFile(devtoolsProperties.migrationS3Bucket(), devtoolsProperties.migrationCsvFileKey()))
        .thenReturn(new ByteArrayInputStream(new byte[1]));

    doThrow(new S3Exception("")).when(pwaS3FileService).listS3Files(devtoolsProperties.migrationS3Bucket());

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> consentDocumentMigrationService.verify());
  }

  @Test
  void verifyFiles() throws S3Exception {
    var csvRow1 = new ConsentDocumentCsvRow(
        "1-w-2",
        "Field 1",
        "1-w-2",
        "12/12/12",
        "PWA",
        "scanned",
        "",
        "",
        "upload"
    );

    var csvRow2 = new ConsentDocumentCsvRow(
        "2-w-3",
        "Field 2",
        "2-v-3",
        "11/12/13",
        "Variation",
        "scanned",
        "",
        "",
        "upload"
    );

    var csvRows = List.of(csvRow1, csvRow2);

    var documentRecord1 = new DocumentMigrationRecord();
    documentRecord1.setFilename("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf");
    documentRecord1.setPwaReference("1-w-2");
    documentRecord1.setFieldName("Field 1");
    documentRecord1.setConsentDoc("1-w-2");
    documentRecord1.setConsentDate("12/12/12");
    documentRecord1.setConsentType("PWA");
    documentRecord1.setIncorrectPwaReference("");
    documentRecord1.setAction("upload");
    documentRecord1.setFileLocated(true);

    var documentRecord2 = new DocumentMigrationRecord();
    documentRecord2.setFilename(null);
    documentRecord2.setPwaReference("2-w-3");
    documentRecord2.setFieldName("Field 2");
    documentRecord2.setConsentDoc("2-v-3");
    documentRecord2.setConsentDate("11/12/13");
    documentRecord2.setConsentType("Variation");
    documentRecord2.setIncorrectPwaReference("");
    documentRecord2.setAction("upload");
    documentRecord2.setFileLocated(false);

    var s3File1 = new S3File("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf", 1L);

    when(documentMigrationRecordRepository.findAll()).thenReturn(List.of());
    when(pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket())).thenReturn(List.of(s3File1));

    consentDocumentMigrationService.verifyFiles(csvRows);

    Mockito.verify(documentMigrationRecordRepository, times(2)).save(documentMigrationRecordCaptor.capture());

    assertThat(documentMigrationRecordCaptor.getAllValues()).usingRecursiveComparison().isEqualTo(List.of(documentRecord1, documentRecord2));
  }

  @Test
  void verifyFiles_documentRecordsExist() throws S3Exception {
    var csvRow1 = new ConsentDocumentCsvRow(
        "1-w-2",
        "Field 1",
        "1-w-2",
        "12/12/12",
        "PWA",
        "scanned",
        "",
        "",
        "upload"
    );

    var csvRow2 = new ConsentDocumentCsvRow(
        "2-w-3",
        "Field 2",
        "2-v-3",
        "11/12/13",
        "Variation",
        "scanned",
        "",
        "",
        "upload"
    );

    var csvRows = List.of(csvRow1, csvRow2);

    var documentRecord1 = new DocumentMigrationRecord();
    documentRecord1.setFilename("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf");
    documentRecord1.setPwaReference("1-w-2");
    documentRecord1.setFieldName("Field 1");
    documentRecord1.setConsentDoc("1-w-2");
    documentRecord1.setConsentDate("12/12/12");
    documentRecord1.setConsentType("PWA");
    documentRecord1.setAction("upload");
    documentRecord1.setFileLocated(true);

    var documentRecord2 = new DocumentMigrationRecord();
    documentRecord2.setFilename("Field 2 (2-w-3) Variation Consent Document (2-v-3).pdf");
    documentRecord2.setPwaReference("2-w-3");
    documentRecord2.setFieldName("Field 2");
    documentRecord2.setConsentDoc("2-v-3");
    documentRecord2.setConsentDate("11/12/13");
    documentRecord2.setConsentType("Variation");
    documentRecord2.setAction("upload");
    documentRecord2.setFileLocated(true);

    var s3File1 = new S3File("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf", 1L);
    var s3File2 = new S3File("Field 2 (2-w-3) Variation Consent Document (2-v-3).pdf", 1L);

    when(documentMigrationRecordRepository.findAll()).thenReturn(List.of(documentRecord1, documentRecord2));
    when(pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket())).thenReturn(List.of(s3File1, s3File2));

    consentDocumentMigrationService.verifyFiles(csvRows);

    Mockito.verify(documentMigrationRecordRepository).save(documentRecord1);
    Mockito.verify(documentMigrationRecordRepository).save(documentRecord2);
  }

  @Test
  void verifyFiles_documentsAlreadyMigrated() throws S3Exception {
    var csvRow1 = new ConsentDocumentCsvRow(
        "1-w-2",
        "Field 1",
        "1-w-2",
        "12/12/12",
        "PWA",
        "scanned",
        "",
        "",
        "upload"
    );

    var csvRow2 = new ConsentDocumentCsvRow(
        "2-w-3",
        "Field 2",
        "2-v-3",
        "11/12/13",
        "Variation",
        "scanned",
        "",
        "",
        "upload"
    );

    var csvRows = List.of(csvRow1, csvRow2);

    var documentRecord1 = new DocumentMigrationRecord();
    documentRecord1.setFilename("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf");
    documentRecord1.setConsentDoc("1-w-2");
    documentRecord1.setFileLocated(true);
    documentRecord1.setDestinationRecordExists(true);
    documentRecord1.setMigrationSuccessful(true);

    var documentRecord2 = new DocumentMigrationRecord();
    documentRecord2.setFilename("Field 2 (2-w-3) Variation Consent Document (2-v-3).pdf");
    documentRecord2.setConsentDoc("2-v-3");
    documentRecord2.setFileLocated(true);
    documentRecord2.setDestinationRecordExists(true);
    documentRecord2.setMigrationSuccessful(true);

    var s3File1 = new S3File("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf", 1L);
    var s3File2 = new S3File("Field 2 (2-w-3) Variation Consent Document (2-v-3).pdf", 1L);

    when(documentMigrationRecordRepository.findAll()).thenReturn(List.of(documentRecord1, documentRecord2));
    when(pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket())).thenReturn(List.of(s3File1, s3File2));

    consentDocumentMigrationService.verifyFiles(csvRows);

    Mockito.verify(documentMigrationRecordRepository, never()).save(any());
  }

  @Test
  void verifyDestinations() {
    var docRecord = new DocumentMigrationRecord();
    docRecord.setConsentDoc("ref");

    when(documentMigrationRecordRepository.findAllByMigrationSuccessfulIsFalse()).thenReturn(List.of(docRecord));

    when(pwaConsentService.getConsentByReference("ref")).thenReturn(Optional.of(new PwaConsent()));

    consentDocumentMigrationService.verifyDestinations();

    Mockito.verify(documentMigrationRecordRepository).save(documentMigrationRecordCaptor.capture());

    assertThat(documentMigrationRecordCaptor.getValue().getDestinationRecordExists()).isTrue();
  }

  @Test
  void verifyDestinations_destinationRecordDoesNotExist() {
    var docRecord = new DocumentMigrationRecord();
    docRecord.setConsentDoc("ref");

    when(documentMigrationRecordRepository.findAllByMigrationSuccessfulIsFalse()).thenReturn(List.of(docRecord));

    when(pwaConsentService.getConsentByReference("ref")).thenReturn(Optional.empty());

    consentDocumentMigrationService.verifyDestinations();

    Mockito.verify(documentMigrationRecordRepository, never()).save(any());
  }

  @Test
  void migrate() throws S3Exception, IOException {
    String test = "1-w-2";

    var documentRecord1 = new DocumentMigrationRecord();
    documentRecord1.setFilename("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf");
    documentRecord1.setPwaReference("1-w-2");
    documentRecord1.setFieldName("Field 1");
    documentRecord1.setConsentDoc(test);
    documentRecord1.setConsentDate("12/12/12");
    documentRecord1.setConsentType("PWA");
    documentRecord1.setAction("upload");
    documentRecord1.setFileLocated(true);
    documentRecord1.setDestinationRecordExists(true);
    documentRecord1.setMigrationSuccessful(false);

    var documentRecord2 = new DocumentMigrationRecord();
    documentRecord2.setFilename("Field 2 (2-w-3) Variation Consent Document (2-v-3).pdf");
    documentRecord2.setPwaReference("2-w-3");
    documentRecord2.setFieldName("Field 2");
    documentRecord2.setConsentDoc("2-v-3");
    documentRecord2.setConsentDate("11/12/13");
    documentRecord2.setConsentType("Variation");
    documentRecord2.setAction("upload");
    documentRecord2.setFileLocated(true);
    documentRecord2.setDestinationRecordExists(false);
    documentRecord2.setMigrationSuccessful(false);

    when(documentMigrationRecordRepository.findAllByMigrationSuccessfulIsFalseAndFileLocatedIsTrue()).thenReturn(List.of(documentRecord1, documentRecord2));

    var s3File1 = new S3File("Field 1 (1-w-2) PWA Consent Document (1-w-2).pdf", 1L);
    var s3File2 = new S3File("Field 2 (2-w-3) Variation Consent Document (2-v-3).pdf", 1L);

    when(pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket())).thenReturn(List.of(s3File1, s3File2));

    var pwaConsent1 = new PwaConsent();
    pwaConsent1.setReference(documentRecord1.getConsentDoc());
    var pwaConsent2 = new PwaConsent();
    pwaConsent2.setReference(documentRecord2.getConsentDoc());

    when(pwaConsentService.getConsentByReference(test)).thenReturn(Optional.of(pwaConsent1));
    when(pwaConsentService.getConsentByReference(documentRecord2.getConsentDoc())).thenReturn(Optional.empty());
    when(pwaConsentService.createLegacyConsent(documentRecord2)).thenReturn(pwaConsent2);

    when(pwaS3FileService.downloadFile(devtoolsProperties.migrationS3Bucket(), documentRecord1.getFilename()))
        .thenReturn(new ByteArrayInputStream(new byte[1]));

    when(pwaS3FileService.downloadFile(devtoolsProperties.migrationS3Bucket(), documentRecord2.getFilename()))
        .thenReturn(new ByteArrayInputStream(new byte[1]));

    var response = mock(FileUploadResponse.class);

    when(fileService.upload(any())).thenReturn(response);
    when(response.getError()).thenReturn(null);

    consentDocumentMigrationService.migrate();

    Mockito.verify(fileService, times(2)).upload(any());
    Mockito.verify(consentDocumentFileManagementService).saveConsentDocument(any(), eq(pwaConsent1));
    Mockito.verify(consentDocumentFileManagementService).saveConsentDocument(any(), eq(pwaConsent2));
    Mockito.verify(documentMigrationRecordRepository, times(3)).save(any());
  }
}