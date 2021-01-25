package uk.co.ogauthority.pwa.service.images;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import org.imgscalr.Scalr;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;

public class ImageScalingServiceTest {

  private ImageScalingService imageScalingService;

  @Before
  public void setUp() throws Exception {
    imageScalingService = new ImageScalingService(Scalr.Method.ULTRA_QUALITY, 800, 950, 950);
  }

  @Test
  public void scaleImage_small_portrait_noTransform() throws IOException, SQLException {

    var imageRes = new ClassPathResource("test-image-small-portrait.png");

    var byteArray = StreamUtils.copyToByteArray(imageRes.getInputStream());
    var blob = new SerialBlob(byteArray);

    var uploadedFile = new UploadedFile();
    uploadedFile.setFileId("id1");
    uploadedFile.setFileData(blob);
    uploadedFile.setContentType("image/png");

    var baos = imageScalingService.scaleImage(uploadedFile);

    BufferedImage originalImage = ImageIO.read(imageRes.getInputStream());
    BufferedImage scaledImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

    // no change between original and 'scaled' image
    assertThat(originalImage.getHeight()).isEqualTo(scaledImage.getHeight());
    assertThat(originalImage.getWidth()).isEqualTo(scaledImage.getWidth());

  }

  @Test
  public void scaleImage_small_landscape_rotated() throws IOException, SQLException {

    var imageRes = new ClassPathResource("test-image-small-landscape.jpg");

    var byteArray = StreamUtils.copyToByteArray(imageRes.getInputStream());
    var blob = new SerialBlob(byteArray);

    var uploadedFile = new UploadedFile();
    uploadedFile.setFileId("id1");
    uploadedFile.setFileData(blob);
    uploadedFile.setContentType("image/jpg");

    var baos = imageScalingService.scaleImage(uploadedFile);

    BufferedImage originalImage = ImageIO.read(imageRes.getInputStream());
    BufferedImage scaledImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

    // width and height are swapped on the scaled image
    assertThat(originalImage.getHeight()).isEqualTo(scaledImage.getWidth());
    assertThat(originalImage.getWidth()).isEqualTo(scaledImage.getHeight());

  }

  @Test
  public void scaleImage_large_landscape_rotatedAndResized() throws IOException, SQLException {

    var imageRes = new ClassPathResource("test-image-large-landscape.jpg");

    var byteArray = StreamUtils.copyToByteArray(imageRes.getInputStream());
    var blob = new SerialBlob(byteArray);

    var uploadedFile = new UploadedFile();
    uploadedFile.setFileId("id1");
    uploadedFile.setFileData(blob);
    uploadedFile.setContentType("image/jpg");

    var baos = imageScalingService.scaleImage(uploadedFile);

    BufferedImage originalImage = ImageIO.read(imageRes.getInputStream());
    BufferedImage scaledImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));

    assertThat(originalImage.getHeight()).isEqualTo(1014);
    assertThat(originalImage.getWidth()).isEqualTo(1600);

    // scaled down to 950px max, height is now more than width due to rotation
    assertThat(scaledImage.getHeight()).isEqualTo(950);
    assertThat(scaledImage.getWidth()).isLessThan(950);

  }

}