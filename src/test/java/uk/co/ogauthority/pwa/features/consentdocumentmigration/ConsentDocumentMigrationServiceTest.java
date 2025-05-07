package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.config.DevtoolsProperties;
import uk.co.ogauthority.pwa.features.filemanagement.s3.PwaS3FileService;

@ActiveProfiles("devtools")
@ExtendWith(MockitoExtension.class)
class ConsentDocumentMigrationServiceTest {

  @Mock
  private PwaS3FileService pwaS3FileService;

  @Mock
  private FileUploadProperties fileUploadProperties;

  private final DevtoolsProperties devtoolsProperties = new DevtoolsProperties("pwa-migration", "csv-key");

  private ConsentDocumentMigrationService consentDocumentMigrationService;

  private FileUploadProperties.S3 s3Properties;

  @BeforeEach
  void setUp() {
    consentDocumentMigrationService = new ConsentDocumentMigrationService(pwaS3FileService, fileUploadProperties, devtoolsProperties);

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

    var csvInputStream = Mockito.mock(InputStream.class);

    when(pwaS3FileService.downloadFile(devtoolsProperties.migrationS3Bucket(), devtoolsProperties.migrationCsvFileKey()))
        .thenReturn(csvInputStream);

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
}