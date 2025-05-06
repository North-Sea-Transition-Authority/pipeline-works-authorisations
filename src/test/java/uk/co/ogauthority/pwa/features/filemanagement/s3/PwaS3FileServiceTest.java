package uk.co.ogauthority.pwa.features.filemanagement.s3;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.s3.S3Exception;

@ExtendWith(MockitoExtension.class)
class PwaS3FileServiceTest {

  @Mock
  private AmazonS3 s3;

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
}