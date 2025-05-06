package uk.co.ogauthority.pwa.features.consentdocumentmigration;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.configuration.FileUploadProperties;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.ogauthority.pwa.config.DevtoolsProperties;
import uk.co.ogauthority.pwa.features.filemanagement.s3.PwaS3FileService;

@Service
@Profile("devtools")
public class ConsentDocumentMigrationService {

  private final PwaS3FileService pwaS3FileService;
  private final FileUploadProperties fileUploadProperties;
  private final DevtoolsProperties devtoolsProperties;

  public ConsentDocumentMigrationService(
      PwaS3FileService pwaS3FileService,
      FileUploadProperties fileUploadProperties,
      DevtoolsProperties devtoolsProperties
  ) {
    this.pwaS3FileService = pwaS3FileService;
    this.fileUploadProperties = fileUploadProperties;
    this.devtoolsProperties = devtoolsProperties;
  }

  void verify() throws S3Exception {
    verifyBuckets();
  }

  private void verifyBuckets() throws S3Exception {
    pwaS3FileService.verifyBucketOrThrow(fileUploadProperties.s3().defaultBucket());
    pwaS3FileService.verifyBucketOrThrow(devtoolsProperties.migrationS3Bucket());
  }

}
