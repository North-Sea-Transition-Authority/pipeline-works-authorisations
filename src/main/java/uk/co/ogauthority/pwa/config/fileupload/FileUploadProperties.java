package uk.co.ogauthority.pwa.config.fileupload;


import fi.solita.clamav.ClamAVClient;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotBlank;
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

  @NotBlank
  private String allowedExtensions;

  private String fileNameFilter;


  public long getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(long maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public String getAllowedExtensions() {
    return allowedExtensions;
  }

  public void setAllowedExtensions(String allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }

  public String getFileNameFilter() {
    return fileNameFilter;
  }

  public void setFileNameFilter(String fileNameFilter) {
    this.fileNameFilter = fileNameFilter;
  }

  public List<String> getAllowedExtensionsList() {
    return Arrays.asList(allowedExtensions.split(",[ ]*"));
  }

  @Bean
  public ClamAVClient clamAvClient(@Value("#{'${clamav.host:}'.split(';')}") String clamavHost,
                                   @Value("#{'${clamav.port:}'.split(';')}") int clamavPort,
                                   @Value("#{'${clamav.timeout:}'.split(';')}") int clamavTimeout) {
    return new ClamAVClient(clamavHost, clamavPort, clamavTimeout);
  }
}
