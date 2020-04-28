package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;

@Service
public class PadPipelineCrossingService {

  private final PadPipelineCrossingRepository padPipelineCrossingRepository;

  @Autowired
  public PadPipelineCrossingService(
      PadPipelineCrossingRepository padPipelineCrossingRepository) {
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
  }


}
