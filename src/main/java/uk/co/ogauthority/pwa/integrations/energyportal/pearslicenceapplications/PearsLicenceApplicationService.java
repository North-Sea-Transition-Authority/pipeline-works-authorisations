package uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationLicenceApplicationRepository;

@Service
public class PearsLicenceApplicationService {

  private final PearsLicenceApplicationRepository applicationRepository;

  private final PadProjectInformationLicenceApplicationRepository padLicenseRepository;

  @Autowired
  public PearsLicenceApplicationService(PearsLicenceApplicationRepository applicationRepository,
                                        PadProjectInformationLicenceApplicationRepository padLicenseRepository) {
    this.applicationRepository = applicationRepository;
    this.padLicenseRepository = padLicenseRepository;
  }

  public List<PearsLicenceApplication> getApplicationsByName(String name) {
    return applicationRepository.findAllByApplicationReferenceContainingIgnoreCase(name);
  }

  public List<PearsLicenceApplication> getApplicationsByIds(List<Integer> ids) {
    return applicationRepository.findAllByApplicationIdIn(ids);
  }

}
