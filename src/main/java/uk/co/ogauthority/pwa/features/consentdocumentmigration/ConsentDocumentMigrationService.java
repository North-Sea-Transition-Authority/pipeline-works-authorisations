package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import com.google.common.annotations.VisibleForTesting;
import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.config.DevtoolsProperties;
import uk.co.ogauthority.pwa.features.filemanagement.ConsentDocumentFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.MultipartFileInputStream;
import uk.co.ogauthority.pwa.features.filemanagement.s3.PwaS3FileService;
import uk.co.ogauthority.pwa.features.filemanagement.s3.S3File;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.util.StreamUtil;

@Service
@Profile("devtools")
public class ConsentDocumentMigrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsentDocumentMigrationService.class);
  private static final String EMPTY_ROW = ",,,,,,,,";
  private static final String PDF_FILE_TYPE = "application/pdf";

  private final PwaS3FileService pwaS3FileService;
  private final FileUploadProperties fileUploadProperties;
  private final DevtoolsProperties devtoolsProperties;
  private final DocumentMigrationRecordRepository documentMigrationRecordRepository;
  private final PwaConsentService pwaConsentService;
  private final FileService fileService;
  private final ConsentDocumentFileManagementService consentDocumentFileManagementService;

  public ConsentDocumentMigrationService(
      PwaS3FileService pwaS3FileService,
      FileUploadProperties fileUploadProperties,
      DevtoolsProperties devtoolsProperties,
      DocumentMigrationRecordRepository documentMigrationRecordRepository,
      PwaConsentService pwaConsentService,
      FileService fileService,
      ConsentDocumentFileManagementService consentDocumentFileManagementService
  ) {
    this.pwaS3FileService = pwaS3FileService;
    this.fileUploadProperties = fileUploadProperties;
    this.devtoolsProperties = devtoolsProperties;
    this.documentMigrationRecordRepository = documentMigrationRecordRepository;
    this.pwaConsentService = pwaConsentService;
    this.fileService = fileService;
    this.consentDocumentFileManagementService = consentDocumentFileManagementService;
  }

  @Transactional
  void verify() throws S3Exception, IOException {
    verifyBuckets();
    var csvRows = downloadAndReadCsv();
    verifyFiles(csvRows);
    verifyDestinations();
  }

  private void verifyBuckets() throws S3Exception {
    pwaS3FileService.verifyBucketOrThrow(fileUploadProperties.s3().defaultBucket());
    pwaS3FileService.verifyBucketOrThrow(devtoolsProperties.migrationS3Bucket());
  }

  private List<ConsentDocumentCsvRow> downloadAndReadCsv() throws S3Exception, IOException {
    try (InputStream csv = pwaS3FileService.downloadFile(
        devtoolsProperties.migrationS3Bucket(),
        devtoolsProperties.migrationCsvFileKey()
    )) {
      return new BufferedReader(new InputStreamReader(csv)).lines()
          .skip(1) // skip the first row of the csv which contains the column headings
          .filter(line -> !line.equals(EMPTY_ROW))
          .map(this::mapCsvRow)
          .toList();

    } catch (S3Exception e) {
      throw new S3Exception("Failed to download CSV file", e);
    } catch (IOException e) {
      throw new IOException("Failed to read CSV file", e);
    }
  }

  private ConsentDocumentCsvRow mapCsvRow(String line) {
    var row = line.split(",");
    return new ConsentDocumentCsvRow(
        row[0],
        row[1],
        row[2],
        row[3],
        row[4],
        row[5],
        row[6],
        row[7],
        row[8]
    );
  }

  @Transactional
  @VisibleForTesting
  void verifyFiles(List<ConsentDocumentCsvRow> csvRows) throws S3Exception {
    var fileKeys = getFileKeys();

    var recordMap = documentMigrationRecordRepository.findAll().stream()
        .collect(StreamUtil.toLinkedHashMap(DocumentMigrationRecord::getConsentDoc, Function.identity()));

    for (ConsentDocumentCsvRow row : csvRows) {
      var docRecord = recordMap.getOrDefault(row.consentDocument(), new DocumentMigrationRecord());

      var fileName = getFilenameIfDocumentExists(fileKeys, row.consentDocument());

      if (!docRecord.getMigrationSuccessful()) {
        docRecord.setFilename(fileName);
        docRecord.setPwaReference(row.pwaReference());
        docRecord.setFieldName(row.field());
        docRecord.setConsentDoc(row.consentDocument());
        docRecord.setConsentDate(row.consentDate());
        docRecord.setConsentType(cleanseConsentType(row.consentType()));
        docRecord.setIncorrectPwaReference(row.incorrectLocation());
        docRecord.setAction(row.action());
        docRecord.setFileLocated(fileName != null);
        documentMigrationRecordRepository.save(docRecord);
      }
    }
  }

  private List<String> getFileKeys() throws S3Exception {
    return pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket()).stream()
        .map(S3File::key)
        .toList();
  }

  private String getFilenameIfDocumentExists(List<String> fileKeys, String consentDoc) {
    return fileKeys.stream()
        .filter(key -> key.contains(consentDoc.replace("/", "-")))
        .findFirst()
        .orElse(null);
  }

  private String cleanseConsentType(String consentType) {
    return consentType
        .replace("Consent", "")
        .trim();
  }

  @Transactional
  @VisibleForTesting
  void verifyDestinations() {
    var records = documentMigrationRecordRepository.findAllByMigrationSuccessfulIsFalse();

    for (var docRecord : records) {
      var consent = pwaConsentService.getConsentByReference(docRecord.getConsentDoc());

      if (consent.isPresent()) {
        docRecord.setDestinationRecordExists(true);
        documentMigrationRecordRepository.save(docRecord);
      }
    }
  }

  @Transactional
  void migrate() throws S3Exception, IOException {
    var filesToMigrate = documentMigrationRecordRepository.findAllByMigrationSuccessfulIsFalseAndFileLocatedIsTrue();
    var fileSizeMap = getS3FileSizeMap();

    for (var docRecord : filesToMigrate) {
      var consent = pwaConsentService.getConsentByReference(docRecord.getConsentDoc())
          .orElseGet(() -> generateDestinationRecord(docRecord));

      migrateFile(docRecord, fileSizeMap, consent);
    }
  }

  private Map<String, Long> getS3FileSizeMap() throws S3Exception {
    return pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket()).stream()
        .collect(StreamUtil.toLinkedHashMap(S3File::key, S3File::size));
  }

  private PwaConsent generateDestinationRecord(DocumentMigrationRecord documentMigrationRecord) {
    var consent = pwaConsentService.createLegacyConsent(documentMigrationRecord);
    documentMigrationRecord.setDestinationRecordExists(true);
    documentMigrationRecordRepository.save(documentMigrationRecord);
    return consent;
  }

  private void migrateFile(
      DocumentMigrationRecord documentMigrationRecord,
      Map<String, Long> fileSizeMap,
      PwaConsent pwaConsent
  ) throws S3Exception, IOException {
    var fileStream = getByteArrayInputStream(
        pwaS3FileService.downloadFile(devtoolsProperties.migrationS3Bucket(), documentMigrationRecord.getFilename())
    );

    if (!fileSizeMap.containsKey(documentMigrationRecord.getFilename())) {
      LOGGER.warn("File size for filename %s not found".formatted(documentMigrationRecord.getFilename()));
      return;
    }

    var multipartFileInputStream = new MultipartFileInputStream(
        documentMigrationRecord.getFilename(),
        PDF_FILE_TYPE,
        fileSizeMap.get(documentMigrationRecord.getFilename()),
        fileStream
    );

    var uploadResponse = fileService.upload(builder -> builder
        .withFileSource(FileSource.fromMultipartFile(multipartFileInputStream))
        .build()
    );

    if (uploadResponse.getError() != null) {
      LOGGER.warn("Error uploading file: %s %s".formatted(documentMigrationRecord.getFilename(), uploadResponse.getError()));
      return;
    }

    var uploadedFile = new UploadedFileForm();
    uploadedFile.setFileId(uploadResponse.getFileId());
    uploadedFile.setFileName(documentMigrationRecord.getFilename());
    uploadedFile.setFileDescription("");
    uploadedFile.setFileSize(uploadedFile.getUploadedFileSize());
    uploadedFile.setFileUploadedAt(Instant.now());

    consentDocumentFileManagementService.saveConsentDocument(uploadedFile, pwaConsent);

    documentMigrationRecord.setMigrationSuccessful(true);
    documentMigrationRecordRepository.save(documentMigrationRecord);
  }

  private ByteArrayInputStream getByteArrayInputStream(InputStream inputStream) throws IOException {
    return new ByteArrayInputStream(inputStream.readAllBytes());
  }
}
