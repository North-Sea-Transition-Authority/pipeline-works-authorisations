package uk.co.ogauthority.pwa.service.images;

import com.google.common.base.Stopwatch;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import org.imgscalr.AsyncScalr;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.exception.images.ImageScalingException;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFileOld;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;

@Service
public class ImageScalingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageScalingService.class);

  private final Scalr.Method scalingMethod;
  private final int thresholdKb;
  private final int scaledHeightPx;
  private final int scaledWidthPx;
  private final MetricsProvider metricsProvider;

  @Autowired
  public ImageScalingService(@Value("${pwa.image-scaling.method}") Scalr.Method scalingMethod,
                             @Value("${pwa.image-scaling.threshold-kb}") int thresholdKb,
                             @Value("${pwa.image-scaling.scaled-height-px}") int scaledHeightPx,
                             @Value("${pwa.image-scaling.scaled-width-px}") int scaledWidthPx,
                             MetricsProvider metricsProvider) {
    this.scalingMethod = scalingMethod;
    this.thresholdKb = thresholdKb;
    this.scaledHeightPx = scaledHeightPx;
    this.scaledWidthPx = scaledWidthPx;
    this.metricsProvider = metricsProvider;
  }

  public ByteArrayOutputStream scaleImage(UploadedFileOld uploadedFile) {

    var stopwatch = Stopwatch.createStarted();

    try (var baos = new ByteArrayOutputStream()) {

      BufferedImage resultImage;
      boolean downsized = false;
      boolean rotated = false;

      int sizeInKb = (int) uploadedFile.getFileData().length() / 1024;

      // read the uploaded file blob into a BufferedImage
      var stream = uploadedFile.getFileData().getBinaryStream();
      resultImage = ImageIO.read(stream);
      stream.close();

      // if the image meets our threshold size, downsize it
      if (sizeInKb >= thresholdKb) {
        var futureResizedImage = AsyncScalr.resize(resultImage, scalingMethod, scaledWidthPx, scaledHeightPx);
        resultImage = futureResizedImage.get();
        downsized = true;
      }

      // if the image is landscape, rotate it 90 degrees
      if (resultImage.getWidth() > resultImage.getHeight()) {
        var futureRotatedImage = AsyncScalr.rotate(resultImage, Scalr.Rotation.CW_90);
        resultImage = futureRotatedImage.get();
        rotated = true;
      }

      // write the modified image to the output stream to return, using the image type indicated by the file's content type
      ImageIO.write(resultImage, uploadedFile.getContentType().replace("image/", ""), baos);
      resultImage.flush();

      resultImage = null;

      var timerLogMessage = String.format("File '%s' scaled. UF_ID = %s Downsized = %s Rotated = %s",
          uploadedFile.getFileName(), uploadedFile.getFileId(), downsized, rotated);
      MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getImageScalingTimer(), timerLogMessage);

      return baos;

    } catch (IOException | SQLException | ExecutionException | InterruptedException e) {

      // sonarcloud says that interrupted exceptions should be rethrown or the thread should be re-interrupted to avoid delaying
      // thread shutdown and losing the information that the thread was interrupted.
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }

      throw new ImageScalingException(
          String.format("Failed to scale uploaded image for file with id [%s]", uploadedFile.getFileId()), e);

    }

  }

}
