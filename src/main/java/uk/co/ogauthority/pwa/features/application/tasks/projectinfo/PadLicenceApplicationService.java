package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplications;

@Service
public class PadLicenceApplicationService {

  private final PadProjectInformationLicenceApplicationsRepository padLicenseApplicationRepository;

  private final PearsLicenceApplicationService pearsLicenceApplicationService;

  public PadLicenceApplicationService(PadProjectInformationLicenceApplicationsRepository padLicenseApplicationRepository,
                                      PearsLicenceApplicationService pearsLicenceApplicationService) {
    this.padLicenseApplicationRepository = padLicenseApplicationRepository;
    this.pearsLicenceApplicationService = pearsLicenceApplicationService;
  }

  @Transactional
  public void saveLicencesToApplication(PadProjectInformation padProjectInformation,
                                        ProjectInformationForm form) {

    padLicenseApplicationRepository.deleteAllByPadProjectInformation(padProjectInformation);
    if (form.getLicenceTransferPlanned()) {
      var ids = Arrays.stream(form.getLicenceList())
          .map(Integer::valueOf)
          .collect(Collectors.toList());
      var applications = pearsLicenceApplicationService.getLicencesByIds(ids);
      for (var application : applications) {
        padLicenseApplicationRepository.save(new PadProjectInformationLicenceApplications(
            padProjectInformation,
            application));
      }
    }
  }

  public void mapLicencesToForm(ProjectInformationForm form, PadProjectInformation projectInformation) {
    if (projectInformation.getId() != null) {
      var licenses = padLicenseApplicationRepository.findAllByPadProjectInformation(projectInformation)
          .stream()
          .map(PadProjectInformationLicenceApplications::getPearsLicenceApplications)
          .map(PearsLicenceApplications::getApplicationId)
          .map(String::valueOf)
          .toArray(String[]::new);
      form.setLicenceList(licenses);
    }
  }
}
