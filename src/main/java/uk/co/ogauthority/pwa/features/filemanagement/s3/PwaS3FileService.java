package uk.co.ogauthority.pwa.features.filemanagement.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

  public List<S3File> listS3Files(String bucket) throws S3Exception {
    try {
      ArrayList<S3ObjectSummary> files = new ArrayList<>();
      var isTruncated = true;
      String continuationToken = null;

      while (isTruncated)  {
        var request = new ListObjectsV2Request()
            .withBucketName(bucket);

        if (continuationToken != null) {
          request.withContinuationToken(continuationToken);
        }

        var listObjectsV2Result = s3.listObjectsV2(request);

        files.addAll(listObjectsV2Result.getObjectSummaries());

        isTruncated = listObjectsV2Result.isTruncated();
        continuationToken = listObjectsV2Result.getNextContinuationToken();
      }

      return files.stream()
          .map(S3File::from)
          .toList();

    } catch (AmazonClientException e) {
      throw new S3Exception("Failed to retrieve S3 files", e);
    }
  }
}
