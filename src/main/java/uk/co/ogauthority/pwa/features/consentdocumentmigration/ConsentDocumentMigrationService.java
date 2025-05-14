package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.config.DevtoolsProperties;
import uk.co.ogauthority.pwa.features.filemanagement.s3.PwaS3FileService;
import uk.co.ogauthority.pwa.features.filemanagement.s3.S3File;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.util.StreamUtil;

@Service
@Profile("devtools")
public class ConsentDocumentMigrationService {

  private static final String EMPTY_ROW = ",,,,,,,,";

  private final PwaS3FileService pwaS3FileService;
  private final FileUploadProperties fileUploadProperties;
  private final DevtoolsProperties devtoolsProperties;
  private final DocumentMigrationRecordRepository documentMigrationRecordRepository;
  private final PwaConsentService pwaConsentService;

  public ConsentDocumentMigrationService(
      PwaS3FileService pwaS3FileService,
      FileUploadProperties fileUploadProperties,
      DevtoolsProperties devtoolsProperties,
      DocumentMigrationRecordRepository documentMigrationRecordRepository,
      PwaConsentService pwaConsentService
  ) {
    this.pwaS3FileService = pwaS3FileService;
    this.fileUploadProperties = fileUploadProperties;
    this.devtoolsProperties = devtoolsProperties;
    this.documentMigrationRecordRepository = documentMigrationRecordRepository;
    this.pwaConsentService = pwaConsentService;
  }

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

  @VisibleForTesting
  void verifyFiles(List<ConsentDocumentCsvRow> csvRows) throws S3Exception {
    var fileKeys = getFileKeys();

    var recordMap = documentMigrationRecordRepository.findAll().stream()
        .collect(StreamUtil.toLinkedHashMap(DocumentMigrationRecord::getFilename, Function.identity()));

    for (ConsentDocumentCsvRow row : csvRows) {
      var filename = mapRowToFilename(row).replace("/", "-");

      var docRecord = recordMap.getOrDefault(filename, new DocumentMigrationRecord());

      if (BooleanUtils.isNotTrue(docRecord.getMigrationSuccessful())) {
        docRecord.setFilename(filename);
        docRecord.setPwaReference(row.pwaReference());
        docRecord.setFieldName(row.field());
        docRecord.setConsentDoc(row.consentDocument());
        docRecord.setConsentDate(row.consentDate());
        docRecord.setConsentType(row.consentType());
        docRecord.setIncorrectPwaReference(row.incorrectLocation());
        docRecord.setAction(row.action());
        docRecord.setFileLocated(fileKeys.contains(filename));
        documentMigrationRecordRepository.save(docRecord);
      }
    }
  }

  private List<String> getFileKeys() throws S3Exception {
    return pwaS3FileService.listS3Files(devtoolsProperties.migrationS3Bucket()).stream()
        .map(S3File::key)
        .toList();
  }

  private String mapRowToFilename(ConsentDocumentCsvRow row) {
    return "%s (%s) %s Consent Document (%s).pdf".formatted(
        row.field(),
        row.pwaReference(),
        row.consentType(),
        row.consentDocument()
    ).replace("/", "-");
  }

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

  void migrate() throws S3Exception {
    var filesToMigrate = documentMigrationRecordRepository.findAllByMigrationSuccessfulIsFalseAndFileLocatedIsTrue();
  }
}
