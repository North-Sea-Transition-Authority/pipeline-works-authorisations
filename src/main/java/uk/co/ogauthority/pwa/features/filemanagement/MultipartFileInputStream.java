package uk.co.ogauthority.pwa.features.filemanagement;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileInputStream implements MultipartFile {

  private final String name;
  private final String contentType;
  private final long size;
  private final InputStream inputStream;

  public MultipartFileInputStream(String name, String contentType, long size, InputStream inputStream) throws IOException {
    this.name = name;
    this.contentType = contentType;
    this.size = size;
    // if inputStream is not marked supported, wrap it in a ByteArrayInputStream to ensure it is
    this.inputStream = inputStream.markSupported() ? inputStream : new ByteArrayInputStream(inputStream.readAllBytes());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOriginalFilename() {
    return name;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public byte[] getBytes() throws IOException {
    return FileCopyUtils.copyToByteArray(inputStream);
  }

  /**
   * Returns the input stream to be read. Resets the input stream so that it can be read again.
   *
   * @return the input stream to be read
   * @throws java.io.IOException if an I/O error occurs
   */
  @Override
  public InputStream getInputStream() throws IOException {
    inputStream.reset();
    return inputStream;
  }

  @Override
  public void transferTo(File dest) throws IOException, IllegalStateException {
    transferTo(dest.toPath());
  }
}
