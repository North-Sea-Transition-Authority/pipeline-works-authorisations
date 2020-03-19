package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;

@Service
public class PadProjectInformationService {

  private final PadProjectInformationRepository padProjectInformationRepository;

  @Autowired
  public PadProjectInformationService(
      PadProjectInformationRepository padProjectInformationRepository) {
    this.padProjectInformationRepository = padProjectInformationRepository;
  }

  public PadProjectInformation save(PadProjectInformation padProjectInformation) {
    return padProjectInformationRepository.save(padProjectInformation);
  }

  public PadProjectInformation getPadProjectInformationData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    projectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return projectInformation;
  }

  public void mapEntityToForm(PadProjectInformation padProjectInformation,
                                               ProjectInformationForm form) {
    form.setProjectOverview(padProjectInformation.getProjectOverview());
    form.setProjectName(padProjectInformation.getProjectName());
    form.setMethodOfPipelineDeployment(padProjectInformation.getMethodOfPipelineDeployment());
    if (padProjectInformation.getProposedStartTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
      form.setProposedStartDay(date.getDayOfMonth());
      form.setProposedStartMonth(date.getMonthValue());
      form.setProposedStartYear(date.getYear());
    }
    if (padProjectInformation.getMobilisationTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getMobilisationTimestamp(), ZoneId.systemDefault());
      form.setMobilisationDay(date.getDayOfMonth());
      form.setMobilisationMonth(date.getMonthValue());
      form.setMobilisationYear(date.getYear());
    }
    if (padProjectInformation.getEarliestCompletionTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getEarliestCompletionTimestamp(), ZoneId.systemDefault());
      form.setEarliestCompletionDay(date.getDayOfMonth());
      form.setEarliestCompletionMonth(date.getMonthValue());
      form.setEarliestCompletionYear(date.getYear());
    }
    if (padProjectInformation.getLatestCompletionTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getLatestCompletionTimestamp(), ZoneId.systemDefault());
      form.setLatestCompletionDay(date.getDayOfMonth());
      form.setLatestCompletionMonth(date.getMonthValue());
      form.setLatestCompletionYear(date.getYear());
    }
  }

  public void saveEntityUsingForm(PadProjectInformation padProjectInformation, ProjectInformationForm form) {
    padProjectInformation.setProjectOverview(form.getProjectOverview());
    padProjectInformation.setProjectName(form.getProjectName());
    padProjectInformation.setMethodOfPipelineDeployment(form.getMethodOfPipelineDeployment());

    // TODO: PWA-379
    try {
      var date = LocalDate.of(form.getProposedStartYear(), form.getProposedStartMonth(), form.getProposedStartDay());
      padProjectInformation.setProposedStartTimestamp(Instant.ofEpochSecond(
          date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC)
      ));
    } catch (Exception e) {
      padProjectInformation.setProposedStartTimestamp(null);
    }

    try {
      var date = LocalDate.of(form.getMobilisationDay(), form.getMobilisationMonth(), form.getMobilisationDay());
      padProjectInformation.setMobilisationTimestamp(Instant.ofEpochSecond(
          date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC)
      ));
    } catch (Exception e) {
      padProjectInformation.setMobilisationTimestamp(null);
    }

    try {
      var date = LocalDate.of(
          form.getEarliestCompletionYear(),
          form.getEarliestCompletionMonth(),
          form.getEarliestCompletionDay()
      );
      padProjectInformation.setEarliestCompletionTimestamp(Instant.ofEpochSecond(
          date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC)
      ));
    } catch (Exception e) {
      padProjectInformation.setEarliestCompletionTimestamp(null);
    }

    try {
      var date = LocalDate.of(
          form.getLatestCompletionYear(),
          form.getLatestCompletionMonth(),
          form.getLatestCompletionDay()
      );
      padProjectInformation.setLatestCompletionTimestamp(Instant.ofEpochSecond(
          date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC)
      ));
    } catch (Exception e) {
      padProjectInformation.setLatestCompletionTimestamp(null);
    }
    save(padProjectInformation);
  }
}
