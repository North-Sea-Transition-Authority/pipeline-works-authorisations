package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicenceService;

@Service
public class PadLicensesService {

  private final PadProjectInformationLicenseReferencesRepository padLicenseRepository;

  private final PearsLicenceService pearsLicenceService;

  public PadLicensesService(PadProjectInformationLicenseReferencesRepository padLicenseRepository,
                            PearsLicenceService pearsLicenceService) {
    this.padLicenseRepository = padLicenseRepository;
    this.pearsLicenceService = pearsLicenceService;
  }

  @Transactional
  public void saveLicensesToApplication(PadProjectInformation padProjectInformation,
                                        ProjectInformationForm form) {
    var licenses = form.getLicenceList();
    for (var license : licenses) {
      padLicenseRepository.save(new PadProjectInformationLicenseReferences(
          padProjectInformation,
          pearsLicenceService.getByMasterId(Integer.valueOf(license))));
    }
  }

  public void mapLicensesToForm(ProjectInformationForm form, PadProjectInformation projectInformation) {
    var licenses = padLicenseRepository.findAllByPadProjectInformation(projectInformation)
        .stream()
        .map(PadProjectInformationLicenseReferences::getPearsLicence)
        .map(PearsLicence::getMasterId)
        .map(String::valueOf)
        .toArray(String[]::new);
    form.setLicenceList(licenses);
  }
}
