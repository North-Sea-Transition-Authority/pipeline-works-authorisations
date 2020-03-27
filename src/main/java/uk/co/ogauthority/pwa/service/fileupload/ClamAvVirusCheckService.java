package uk.co.ogauthority.pwa.service.fileupload;

import fi.solita.clamav.ClamAVClient;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.exception.VirusCheckException;


@Service
public class ClamAvVirusCheckService implements VirusCheckService {

  private final ClamAVClient client;

  @Autowired
  public ClamAvVirusCheckService(ClamAVClient client) {
    this.client = client;
  }

  @Override
  public boolean hasVirus(MultipartFile file) {
    byte[] scanResponse;

    try (InputStream inputStream = file.getInputStream()) {

      scanResponse = client.scan(inputStream);

      return !ClamAVClient.isCleanReply(scanResponse);

    } catch (IOException e) {
      throw new VirusCheckException("Failed to virus scan file", e);
    }
  }
}
