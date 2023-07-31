package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.Arrays;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplications;

@Service
public class PadLicenseApplicationService {

  private final PadProjectInformationLicenseReferencesRepository padLicenseRepository;

  private final PearsLicenceApplicationService pearsLicenceService;

  public PadLicenseApplicationService(PadProjectInformationLicenseReferencesRepository padLicenseRepository,
                                      PearsLicenceApplicationService pearsLicenceService) {
    this.padLicenseRepository = padLicenseRepository;
    this.pearsLicenceService = pearsLicenceService;
  }

  @Transactional
  public void saveLicensesToApplication(PadProjectInformation padProjectInformation,
                                        ProjectInformationForm form) {

    padLicenseRepository.deleteAllByPadProjectInformation(padProjectInformation);
    if (form.getLicenceTransferPlanned()) {
      var ids = Arrays.stream(form.getLicenceList())
          .map(Integer::valueOf)
          .collect(Collectors.toList());
      var applications = pearsLicenceService.getLicencesByIds(ids);
      for (var application : applications) {
        padLicenseRepository.save(new PadProjectInformationLicenseReferences(
            padProjectInformation,
            application));
      }
    }
  }

  public void mapLicensesToForm(ProjectInformationForm form, PadProjectInformation projectInformation) {
    if (projectInformation.getId() != null) {
      var licenses = padLicenseRepository.findAllByPadProjectInformation(projectInformation)
          .stream()
          .map(PadProjectInformationLicenseReferences::getPearsLicence)
          .map(PearsLicenceApplications::getApplicationId)
          .map(String::valueOf)
          .toArray(String[]::new);
      form.setLicenceList(licenses);
    }
  }
}
