package uk.co.ogauthority.pwa.service.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.images.ImageScalingException;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;

@Service
public class ImageScalingService {

  private final Scalr.Method scalingMethod;
  private final int thresholdKb;
  private final int scaledHeightPx;
  private final int scaledWidthPx;

  @Autowired
  public ImageScalingService(@Value("${pwa.image-scaling.method}") Scalr.Method scalingMethod,
                             @Value("${pwa.image-scaling.threshold-kb}") int thresholdKb,
                             @Value("${pwa.image-scaling.scaled-height-px}") int scaledHeightPx,
                             @Value("${pwa.image-scaling.scaled-width-px}") int scaledWidthPx) {
    this.scalingMethod = scalingMethod;
    this.thresholdKb = thresholdKb;
    this.scaledHeightPx = scaledHeightPx;
    this.scaledWidthPx = scaledWidthPx;
  }

  public ByteArrayOutputStream scaleImage(UploadedFile uploadedFile) {

    try (var baos = new ByteArrayOutputStream()) {

      BufferedImage resultImage;

      int sizeInKb = (int) uploadedFile.getFileData().length() / 1024;

      // read the uploaded file blob into a BufferedImage
      var stream = uploadedFile.getFileData().getBinaryStream();
      resultImage = ImageIO.read(stream);
      stream.close();

      // if the image meets our threshold size, downsize it
      if (sizeInKb >= thresholdKb) {
        resultImage = Scalr.resize(resultImage, scalingMethod, scaledWidthPx, scaledHeightPx);
      }

      // if the image is landscape, rotate it 90 degrees
      if (resultImage.getWidth() > resultImage.getHeight()) {
        resultImage = Scalr.rotate(resultImage, Scalr.Rotation.CW_90);
      }

      // write the modified image to the output stream to return, using the image type indicated by the file's content type
      ImageIO.write(resultImage, uploadedFile.getContentType().replace("image/", ""), baos);
      resultImage.flush();

      resultImage = null;

      return baos;

    } catch (IOException | SQLException e) {
      throw new ImageScalingException(
          String.format("Failed to scale uploaded image for file with id [%s]", uploadedFile.getFileId()), e);
    }

  }

}
