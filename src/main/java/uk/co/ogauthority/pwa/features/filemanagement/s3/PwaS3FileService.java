package uk.co.ogauthority.pwa.features.filemanagement.s3;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;

@Service
public class PwaS3FileService {

  private final AmazonS3 s3;

  public PwaS3FileService(AmazonS3 s3) {
    this.s3 = s3;
  }

  public void verifyBucketOrThrow(String bucket) throws S3Exception {
    if (!s3.doesBucketExistV2(bucket)) {
      throw new S3Exception("Bucket %s does not exist".formatted(bucket));
    }
  }
}
