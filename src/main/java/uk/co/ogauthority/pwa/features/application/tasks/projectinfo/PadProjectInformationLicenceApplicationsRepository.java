package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PadProjectInformationLicenceApplicationsRepository
    extends CrudRepository<PadProjectInformationLicenceApplication, Integer> {
  List<PadProjectInformationLicenceApplication> findAllByPadProjectInformation(PadProjectInformation padProjectInformation);

  void deleteAllByPadProjectInformation(PadProjectInformation projectInformation);

}
