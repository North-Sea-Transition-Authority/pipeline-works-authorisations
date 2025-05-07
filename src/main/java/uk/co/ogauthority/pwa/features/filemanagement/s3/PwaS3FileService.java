package uk.co.ogauthority.pwa.features.filemanagement.s3;

import com.amazonaws.services.s3.AmazonS3;
import java.io.InputStream;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.fivium.fileuploadlibrary.s3.S3FileService;

@Service
public class PwaS3FileService {

  private final AmazonS3 s3;
  private final S3FileService s3FileService;

  public PwaS3FileService(
      AmazonS3 s3,
      S3FileService s3FileService
  ) {
    this.s3 = s3;
    this.s3FileService = s3FileService;
  }

  public void verifyBucketOrThrow(String bucket) throws S3Exception {
    if (!s3.doesBucketExistV2(bucket)) {
      throw new S3Exception("Bucket %s does not exist".formatted(bucket));
    }
  }

  public InputStream downloadFile(String bucket, String key) throws S3Exception {
    return s3FileService.downloadFile(bucket, key);
  }
}
