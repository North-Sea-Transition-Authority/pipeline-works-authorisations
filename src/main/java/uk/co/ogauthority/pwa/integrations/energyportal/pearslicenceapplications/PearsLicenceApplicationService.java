package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationLicenceApplicationsRepository;

@Service
public class PearsLicenceApplicationService {

  private final PearsLicenceApplicationRepository applicationRepository;

  private final PadProjectInformationLicenceApplicationsRepository padLicenseRepository;

  public PearsLicenceApplicationService(PearsLicenceApplicationRepository applicationRepository,
                                        PadProjectInformationLicenceApplicationsRepository padLicenseRepository) {
    this.applicationRepository = applicationRepository;
    this.padLicenseRepository = padLicenseRepository;
  }

  public List<PearsLicenceApplications> getLicencesByName(String name) {
    return applicationRepository.findAllByApplicationReferenceContainingIgnoreCase(name);
  }

  public List<PearsLicenceApplications> getLicencesByIds(List<Integer> ids) {
    return applicationRepository.findAllByApplicationIdIn(ids);
  }

}
