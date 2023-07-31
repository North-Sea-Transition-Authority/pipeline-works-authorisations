package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PadProjectInformationLicenceApplicationsRepository extends CrudRepository<PadProjectInformationLicenceApplications, Integer> {
  List<PadProjectInformationLicenceApplications> findAllByPadProjectInformation(PadProjectInformation padProjectInformation);

  void deleteAllByPadProjectInformation(PadProjectInformation projectInformation);

}
