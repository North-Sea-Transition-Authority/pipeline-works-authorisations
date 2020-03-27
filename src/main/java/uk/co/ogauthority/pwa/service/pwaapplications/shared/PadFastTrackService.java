package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.time.LocalDate;
import java.time.ZoneId;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadFastTrack;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadFastTrackRepository;

@Service
public class PadFastTrackService {

  private final PadFastTrackRepository padFastTrackRepository;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public PadFastTrackService(
      PadFastTrackRepository padFastTrackRepository,
      PadProjectInformationService padProjectInformationService) {
    this.padFastTrackRepository = padFastTrackRepository;
    this.padProjectInformationService = padProjectInformationService;
  }

  @Transactional
  public PadFastTrack save(PadFastTrack padFastTrack) {
    return padFastTrackRepository.save(padFastTrack);
  }

  public PadFastTrack getFastTrackForDraft(PwaApplicationDetail detail) {
    var noFastTrack = new PadFastTrack();
    noFastTrack.setPwaApplicationDetail(detail);
    return padFastTrackRepository.findByPwaApplicationDetail(detail)
        .orElse(noFastTrack);
  }

  public boolean isFastTrackRequired(PwaApplicationDetail detail) {
    // TODO: PWA-374 Add median line agreement impact
    var projectInformation = padProjectInformationService.getPadProjectInformationData(detail);
    if (projectInformation.getProposedStartTimestamp() != null) {
      var startDate = LocalDate.ofInstant(projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
      return startDate.isBefore(LocalDate.now().plus(detail.getPwaApplicationType().getMinPeriod()));
    }
    return false;
  }

  public void mapEntityToForm(PadFastTrack fastTrack, FastTrackForm form) {
    form.setAvoidEnvironmentalDisaster(fastTrack.getAvoidEnvironmentalDisaster());
    form.setEnvironmentalDisasterReason(fastTrack.getEnvironmentalDisasterReason());
    form.setSavingBarrels(fastTrack.getSavingBarrels());
    form.setSavingBarrelsReason(fastTrack.getSavingBarrelsReason());
    form.setProjectPlanning(fastTrack.getProjectPlanning());
    form.setProjectPlanningReason(fastTrack.getProjectPlanningReason());
    form.setHasOtherReason(fastTrack.getHasOtherReason());
    form.setOtherReason(fastTrack.getOtherReason());
  }

  @Transactional
  public void saveEntityUsingForm(PadFastTrack fastTrack, FastTrackForm form) {
    fastTrack.setAvoidEnvironmentalDisaster(form.getAvoidEnvironmentalDisaster());
    if (BooleanUtils.isTrue(form.getAvoidEnvironmentalDisaster())) {
      fastTrack.setEnvironmentalDisasterReason(form.getEnvironmentalDisasterReason());
    } else {
      fastTrack.setEnvironmentalDisasterReason(null);
    }

    fastTrack.setSavingBarrels(form.getSavingBarrels());
    if (BooleanUtils.isTrue(form.getSavingBarrels())) {
      fastTrack.setSavingBarrelsReason(form.getSavingBarrelsReason());
    } else {
      fastTrack.setSavingBarrelsReason(null);
    }

    fastTrack.setProjectPlanning(form.getProjectPlanning());
    if (BooleanUtils.isTrue(form.getProjectPlanning())) {
      fastTrack.setProjectPlanningReason(form.getProjectPlanningReason());
    } else {
      fastTrack.setProjectPlanningReason(null);
    }

    fastTrack.setHasOtherReason(form.getHasOtherReason());
    if (BooleanUtils.isTrue(form.getHasOtherReason())) {
      fastTrack.setOtherReason(form.getOtherReason());
    } else {
      fastTrack.setOtherReason(null);
    }
    save(fastTrack);
  }

}
