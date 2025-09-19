package uk.co.ogauthority.pwa.service.images;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.micrometer.core.instrument.Timer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.StreamUtils;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.testutils.TimerMetricTestUtils;

@ExtendWith(MockitoExtension.class)
class ImageScalingServiceTest {

  @Mock
  private MetricsProvider metricsProvider;

  @Mock
  private Appender appender;

  @Captor
  private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

  private ImageScalingService imageScalingService;

  private Timer timer;

  private UUID fileId = UUID.randomUUID();

  @BeforeEach
  void setUp() throws Exception {

    timer = TimerMetricTestUtils.setupTimerMetric(
        ImageScalingService.class, "pwa.imageScalingTimer", appender);
    when(metricsProvider.getImageScalingTimer()).thenReturn(timer);

    imageScalingService = new ImageScalingService(Scalr.Method.ULTRA_QUALITY, 800, 950, 950, metricsProvider);

  }

  @Test
  void scaleImage_small_portrait_noTransform() throws IOException {

    var imageRes = new ClassPathResource("test-image-small-portrait.png");

    var byteArray = StreamUtils.copyToByteArray(imageRes.getInputStream());
    var inputStreamResource = new InputStreamResource(new ByteArrayInputStream(byteArray));

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(fileId);
    uploadedFile.setContentType("image/png");
    uploadedFile.setName("filename.png");

    var baos = imageScalingService.scaleImage(uploadedFile, inputStreamResource);

    BufferedImage originalImage = ImageIO.read(imageRes.getInputStream());
    BufferedImage scaledImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

    // no change between original and 'scaled' image
    assertThat(originalImage.getHeight()).isEqualTo(scaledImage.getHeight());
    assertThat(originalImage.getWidth()).isEqualTo(scaledImage.getWidth());

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "File 'filename.png' scaled. UF_ID = %s Downsized = false Rotated = false".formatted(fileId));

  }

  @Test
  void scaleImage_small_landscape_rotated() throws IOException, SQLException {

    var imageRes = new ClassPathResource("test-image-small-landscape.jpg");

    var byteArray = StreamUtils.copyToByteArray(imageRes.getInputStream());
    var inputStreamResource = new InputStreamResource(new ByteArrayInputStream(byteArray));

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(fileId);
    uploadedFile.setContentType("image/jpg");
    uploadedFile.setName("filename.jpg");

    var baos = imageScalingService.scaleImage(uploadedFile, inputStreamResource);

    BufferedImage originalImage = ImageIO.read(imageRes.getInputStream());
    BufferedImage scaledImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

    // width and height are swapped on the scaled image
    assertThat(originalImage.getHeight()).isEqualTo(scaledImage.getWidth());
    assertThat(originalImage.getWidth()).isEqualTo(scaledImage.getHeight());

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "File 'filename.jpg' scaled. UF_ID = %s Downsized = false Rotated = true".formatted(fileId));

  }

  @Test
  void scaleImage_large_landscape_rotatedAndResized() throws IOException, SQLException {

    var imageRes = new ClassPathResource("test-image-large-landscape.jpg");

    var byteArray = StreamUtils.copyToByteArray(imageRes.getInputStream());
    var inputStreamResource = new InputStreamResource(new ByteArrayInputStream(byteArray));

    var uploadedFile = new UploadedFile();
    uploadedFile.setId(fileId);
    uploadedFile.setContentType("image/jpg");
    uploadedFile.setName("filename.jpg");
    uploadedFile.setContentLength(byteArray.length);

    var baos = imageScalingService.scaleImage(uploadedFile, inputStreamResource);

    BufferedImage originalImage = ImageIO.read(imageRes.getInputStream());
    BufferedImage scaledImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

    assertThat(originalImage.getHeight()).isEqualTo(1014);
    assertThat(originalImage.getWidth()).isEqualTo(1600);

    // scaled down to 950px max, height is now more than width due to rotation
    assertThat(scaledImage.getHeight()).isEqualTo(950);
    assertThat(scaledImage.getWidth()).isLessThan(950);

    TimerMetricTestUtils.assertTimeLogged(loggingEventCaptor, appender, "File 'filename.jpg' scaled. UF_ID = %s Downsized = true Rotated = true".formatted(fileId));

  }

}