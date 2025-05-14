package uk.co.ogauthority.pwa.features.filemanagement.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;
import uk.co.fivium.fileuploadlibrary.s3.S3FileService;

@ExtendWith(MockitoExtension.class)
class PwaS3FileServiceTest {

  @Mock
  private AmazonS3 s3;

  @Mock
  private S3FileService s3FileService;

  @InjectMocks
  private PwaS3FileService pwaS3FileService;

  @Test
  void verifyBucketOrThrow() {
    when(s3.doesBucketExistV2("test")).thenReturn(true);

    assertThatNoException().isThrownBy(() -> pwaS3FileService.verifyBucketOrThrow("test"));
  }

  @Test
  void verifyBucketOrThrow_bucketDoesNotExist() {
    when(s3.doesBucketExistV2("test")).thenReturn(false);

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> pwaS3FileService.verifyBucketOrThrow("test"));
  }

  @Test
  void downloadFile() throws S3Exception {
    var inputStream = new ByteArrayInputStream(new byte[0]);

    when(s3FileService.downloadFile("bucket", "key")).thenReturn(inputStream);

    assertThat(pwaS3FileService.downloadFile("bucket", "key")).isEqualTo(inputStream);
  }

  @Test
  void downloadFile_downloadFailed() throws S3Exception {
    doThrow(new S3Exception("")).when(s3FileService).downloadFile("bucket", "key");

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> pwaS3FileService.downloadFile("bucket", "key"));
  }

  @Test
  void listFiles() throws S3Exception {
    var result = mock(ListObjectsV2Result.class);

    var objectSummary1 = new S3ObjectSummary();
    objectSummary1.setKey("key1");
    objectSummary1.setSize(1L);

    var objectSummary2 = new S3ObjectSummary();
    objectSummary2.setKey("key2");
    objectSummary2.setSize(1L);

    when(s3.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(result);
    when(result.isTruncated()).thenReturn(false);
    when(result.getObjectSummaries()).thenReturn(List.of(objectSummary1, objectSummary2));

    var s3File1 = new S3File("key1", 1L);
    var s3File2 = new S3File("key2", 1L);

    assertThat(pwaS3FileService.listS3Files("bucket")).usingRecursiveComparison().isEqualTo(List.of(s3File1, s3File2));
  }

  @Test
  void listFiles_error() {
    doThrow(new AmazonS3Exception("")).when(s3).listObjectsV2(any(ListObjectsV2Request.class));

    assertThatExceptionOfType(S3Exception.class).isThrownBy(() -> pwaS3FileService.listS3Files("bucket"));
  }
}