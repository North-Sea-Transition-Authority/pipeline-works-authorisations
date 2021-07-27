package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadFastTrack;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;

@Service
@Profile("development")
public class FastTrackGeneratorService {

  private final PadFastTrackService padFastTrackService;

  @Autowired
  public FastTrackGeneratorService(PadFastTrackService padFastTrackService) {
    this.padFastTrackService = padFastTrackService;
  }



  public void generateFastTrack(PwaApplicationDetail pwaApplicationDetail) {

    var fastTrack = new PadFastTrack();
    setFastTrackData(pwaApplicationDetail, fastTrack);
    padFastTrackService.save(fastTrack);
  }


  private void setFastTrackData(PwaApplicationDetail pwaApplicationDetail,
                                PadFastTrack fastTrack) {

    fastTrack.setPwaApplicationDetail(pwaApplicationDetail);
    fastTrack.setAvoidEnvironmentalDisaster(true);
    fastTrack.setEnvironmentalDisasterReason("My reason for selecting avoiding environmental disaster");
  }




}
