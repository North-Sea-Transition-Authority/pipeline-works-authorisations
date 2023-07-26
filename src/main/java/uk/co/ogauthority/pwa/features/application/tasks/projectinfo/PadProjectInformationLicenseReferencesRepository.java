package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PadProjectInformationLicenseReferencesRepository  extends CrudRepository<PadProjectInformationLicenseReferences, Integer> {
  public List<PadProjectInformationLicenseReferences> findAllByPadProjectInformation(PadProjectInformation padProjectInformation);

}
