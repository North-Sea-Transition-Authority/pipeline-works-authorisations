package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvDecomForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;

@Service
public class PadEnvironmentalDecommissioningService {

  private final PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  @Autowired
  public PadEnvironmentalDecommissioningService(
      PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository) {
    this.padEnvironmentalDecommissioningRepository = padEnvironmentalDecommissioningRepository;
  }

  public PadEnvironmentalDecommissioning getEnvDecomData(PwaApplicationDetail pwaApplicationDetail) {
    var adminDetail = padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadEnvironmentalDecommissioning());
    adminDetail.setPwaApplicationDetail(pwaApplicationDetail);
    return adminDetail;
  }

  @Transactional
  public PadEnvironmentalDecommissioning save(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning) {
    return padEnvironmentalDecommissioningRepository.save(padEnvironmentalDecommissioning);
  }

  public void mapEntityToForm(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning, EnvDecomForm form) {
    form.setDecommissioningPlans(padEnvironmentalDecommissioning.getDecommissioningPlans());
    form.setEmtHasOutstandingPermits(padEnvironmentalDecommissioning.getEmtHasOutstandingPermits());
    form.setEmtHasSubmittedPermits(padEnvironmentalDecommissioning.getEmtHasSubmittedPermits());
    form.setPermitsSubmitted(padEnvironmentalDecommissioning.getPermitsSubmitted());
    form.setPermitsPendingSubmission(padEnvironmentalDecommissioning.getPermitsPendingSubmission());
    form.setTransboundaryEffect(padEnvironmentalDecommissioning.getTransboundaryEffect());
    form.setEnvironmentalConditions(padEnvironmentalDecommissioning.getEnvironmentalConditions());
    form.setDecommissioningConditions(padEnvironmentalDecommissioning.getDecommissioningConditions());
    if (padEnvironmentalDecommissioning.getEmtSubmissionTimestamp() != null) {
      var localDate = LocalDate.ofInstant(padEnvironmentalDecommissioning.getEmtSubmissionTimestamp(),
          ZoneId.systemDefault());
      form.setEmtSubmissionDay(localDate.getDayOfMonth());
      form.setEmtSubmissionMonth(localDate.getMonthValue());
      form.setEmtSubmissionYear(localDate.getYear());
    }
  }

  @Transactional
  public void saveEntityUsingForm(PadEnvironmentalDecommissioning padEnvironmentalDecommissioning, EnvDecomForm form) {
    padEnvironmentalDecommissioning.setDecommissioningPlans(form.getDecommissioningPlans());
    padEnvironmentalDecommissioning.setEmtHasOutstandingPermits(form.getEmtHasOutstandingPermits());
    padEnvironmentalDecommissioning.setEmtHasSubmittedPermits(form.getEmtHasSubmittedPermits());
    padEnvironmentalDecommissioning.setEnvironmentalConditions(form.getEnvironmentalConditions());
    padEnvironmentalDecommissioning.setDecommissioningConditions(form.getDecommissioningConditions());

    if (form.getEmtHasSubmittedPermits()) {
      padEnvironmentalDecommissioning.setPermitsSubmitted(form.getPermitsSubmitted());
    } else {
      padEnvironmentalDecommissioning.setPermitsSubmitted(null);
    }

    if (form.getEmtHasOutstandingPermits()) {
      padEnvironmentalDecommissioning.setPermitsPendingSubmission(form.getPermitsPendingSubmission());
      // TODO: PWA-379 - Prevent discard when date is invalid.
      try {
        var localDate = LocalDate.of(
            form.getEmtSubmissionYear(),
            form.getEmtSubmissionMonth(),
            form.getEmtSubmissionDay()
        );
        var instant = Instant.ofEpochSecond(localDate.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC));
        padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(instant);
      } catch (Exception e) {
        padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(null);
      }
    } else {
      padEnvironmentalDecommissioning.setPermitsPendingSubmission(null);
      padEnvironmentalDecommissioning.setEmtSubmissionTimestamp(null);
    }
    padEnvironmentalDecommissioning.setTransboundaryEffect(form.getTransboundaryEffect());
    save(padEnvironmentalDecommissioning);
  }

}
