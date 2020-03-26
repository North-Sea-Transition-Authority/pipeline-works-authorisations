package uk.co.ogauthority.pwa.service.fileupload;

import org.springframework.web.multipart.MultipartFile;

public interface VirusCheckService {

  boolean hasVirus(MultipartFile file);

}