package uk.co.ogauthority.pwa.config.fileupload;


import fi.solita.clamav.ClamAVClient;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * This class provide access to the application properties used for file uploading.
 */
@Configuration
@ConfigurationProperties(prefix = "fileupload")
@Validated
public class FileUploadProperties {
  @NotNull
  private long maxFileSize;

  @NotEmpty
  private List<String> allowedExtensions;
  private List<String> allowedImageExtensions;

  private String fileNameFilter;

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(long maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public String getFileNameFilter() {
    return fileNameFilter;
  }

  public void setFileNameFilter(String fileNameFilter) {
    this.fileNameFilter = fileNameFilter;
  }

  public void setAllowedExtensions(List<String> allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }

  public List<String> getAllowedExtensions() {
    return this.allowedExtensions;
  }

  public List<String> getAllowedImageExtensions() {
    return allowedImageExtensions;
  }

  public void setAllowedImageExtensions(List<String> allowedImageExtensions) {
    this.allowedImageExtensions = allowedImageExtensions;
  }

  @Bean
  public ClamAVClient clamAvClient(@Value("${clamav.host}") String clamavHost,
                                   @Value("${clamav.port}") int clamavPort,
                                   @Value("${clamav.timeout}") int clamavTimeout) {
    return new ClamAVClient(clamavHost, clamavPort, clamavTimeout);
  }
}
