package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

  @Mock
  private DevtoolsProperties devtoolsProperties;

  @InjectMocks
  private ConsentDocumentMigrationService consentDocumentMigrationService;

  private FileUploadProperties.S3 s3Properties;

  @BeforeEach
  void setUp() {
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
    when(devtoolsProperties.migrationS3Bucket()).thenReturn("pwa-migration");

    assertThatNoException().isThrownBy(() -> consentDocumentMigrationService.verify());

    Mockito.verify(pwaS3FileService).verifyBucketOrThrow(s3Properties.defaultBucket());
    Mockito.verify(pwaS3FileService).verifyBucketOrThrow("pwa-migration");
  }

  @Test
  void verify_missingBucket() throws S3Exception {
    when(fileUploadProperties.s3()).thenReturn(s3Properties);

    doThrow(new S3Exception("")).when(pwaS3FileService).verifyBucketOrThrow(s3Properties.defaultBucket());

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> consentDocumentMigrationService.verify());
  }
}