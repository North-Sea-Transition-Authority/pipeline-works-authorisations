package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplication;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;

@Service
public class PadLicenceApplicationService {

  private final PadProjectInformationLicenceApplicationsRepository padLicenceApplicationRepository;

  private final PearsLicenceApplicationService pearsLicenceApplicationService;

  public PadLicenceApplicationService(PadProjectInformationLicenceApplicationsRepository padLicenceApplicationRepository,
                                      PearsLicenceApplicationService pearsLicenceApplicationService) {
    this.padLicenceApplicationRepository = padLicenceApplicationRepository;
    this.pearsLicenceApplicationService = pearsLicenceApplicationService;
  }

  @Transactional
  public void saveApplicationToPad(PadProjectInformation padProjectInformation,
                                   ProjectInformationForm form) {

    padLicenceApplicationRepository.deleteAllByPadProjectInformation(padProjectInformation);
    if (form.getLicenceTransferPlanned()) {
      var ids = Arrays.stream(form.getPearsApplicationList())
          .map(Integer::valueOf)
          .collect(Collectors.toList());
      var applications = pearsLicenceApplicationService.getApplicationByIds(ids);
      for (var application : applications) {
        padLicenceApplicationRepository.save(new PadProjectInformationLicenceApplication(
            padProjectInformation,
            application));
      }
    }
  }

  public void mapApplicationsToForm(ProjectInformationForm form, PadProjectInformation projectInformation) {
    if (projectInformation.getId() != null) {
      form.setPearsApplicationList(padLicenceApplicationRepository.findAllByPadProjectInformation(projectInformation)
          .stream()
          .map(PadProjectInformationLicenceApplication::getPearsLicenceApplications)
          .map(PearsLicenceApplication::getApplicationId)
          .map(String::valueOf)
          .toArray(String[]::new));
    }
  }
}
