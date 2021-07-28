package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;

@Service
@Profile("development")
class FastTrackGeneratorService implements TestHarnessAppFormService {

  private final PadFastTrackService padFastTrackService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.FAST_TRACK;

  @Autowired
  public FastTrackGeneratorService(PadFastTrackService padFastTrackService) {
    this.padFastTrackService = padFastTrackService;
  }

  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm();
    var fastTrack = padFastTrackService.getFastTrackForDraft(appFormServiceParams.getApplicationDetail());
    padFastTrackService.saveEntityUsingForm(fastTrack, form);
  }


  private FastTrackForm createForm() {
    var form = new FastTrackForm();
    form.setAvoidEnvironmentalDisaster(true);
    form.setEnvironmentalDisasterReason("My reason for selecting avoiding environmental disaster");
    return form;
  }




}
