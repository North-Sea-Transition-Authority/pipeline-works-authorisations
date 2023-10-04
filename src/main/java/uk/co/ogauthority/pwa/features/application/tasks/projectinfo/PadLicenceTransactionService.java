package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransaction;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransactionService;

@Service
public class PadLicenceTransactionService {

  private final PadProjectInformationLicenceApplicationRepository padLicenceApplicationRepository;

  private final PearsLicenceTransactionService pearsLicenceTransactionService;

  private final EntityManager entityManager;

  @Autowired
  public PadLicenceTransactionService(PadProjectInformationLicenceApplicationRepository padLicenceApplicationRepository,
                                      PearsLicenceTransactionService pearsLicenceTransactionService,
                                      EntityManager entityManager) {
    this.padLicenceApplicationRepository = padLicenceApplicationRepository;
    this.pearsLicenceTransactionService = pearsLicenceTransactionService;
    this.entityManager = entityManager;
  }

  @Transactional
  public void saveApplicationsToPad(PadProjectInformation padProjectInformation,
                                    ProjectInformationForm form) {

    padLicenceApplicationRepository.deleteAllByPadProjectInformation(padProjectInformation);
    var padApplications = new ArrayList<PadProjectInformationLicenceApplication>();
    if (form.getLicenceTransferPlanned() != null && form.getLicenceTransferPlanned()) {
      var ids = Arrays.stream(form.getPearsApplicationList())
          .map(Integer::valueOf)
          .collect(Collectors.toList());
      var applications = pearsLicenceTransactionService.getApplicationsByIds(ids);
      for (var application : applications) {
        padApplications.add(new PadProjectInformationLicenceApplication(
            padProjectInformation,
            application));
      }
      padLicenceApplicationRepository.saveAll(padApplications);
    }
  }

  public void mapApplicationsToForm(ProjectInformationForm form, PadProjectInformation projectInformation) {
    if (projectInformation.getId() != null) {
      form.setPearsApplicationList(padLicenceApplicationRepository.findAllByPadProjectInformation(projectInformation)
          .stream()
          .map(PadProjectInformationLicenceApplication::getPearsLicenceApplication)
          .map(PearsLicenceTransaction::getTransactionId)
          .map(String::valueOf)
          .toArray(String[]::new));
    }
  }

  public void copyApplicationsToPad(PadProjectInformation fromProjectInformation, PadProjectInformation toProjectInformation) {

    var oldReferences = padLicenceApplicationRepository.findAllByPadProjectInformation(fromProjectInformation);
    for (var reference : oldReferences) {
      entityManager.detach(reference);
      reference.setId(null);
      reference.setPadProjectInformation(toProjectInformation);
      entityManager.persist(reference);
    }
  }

  public List<String> getInformationSummary(PadProjectInformation projectInformation) {

    return padLicenceApplicationRepository.findAllByPadProjectInformation(projectInformation)
        .stream()
        .map(PadProjectInformationLicenceApplication::getPearsLicenceApplication)
        .map(PearsLicenceTransaction::getTransactionReference)
        .collect(Collectors.toList());
  }
}
