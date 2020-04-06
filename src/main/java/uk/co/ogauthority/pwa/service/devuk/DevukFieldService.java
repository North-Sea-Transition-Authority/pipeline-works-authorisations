package uk.co.ogauthority.pwa.service.devuk;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.repository.devuk.DevukFieldRepository;

@Service
public class DevukFieldService {

  private final DevukFieldRepository devukFieldRepository;

  @Autowired
  public DevukFieldService(DevukFieldRepository devukFieldRepository) {
    this.devukFieldRepository = devukFieldRepository;
  }

  public List<DevukField> getByOrganisationUnitWithStatusCodes(PortalOrganisationUnit organisationUnit, List<Integer> statusCodes) {
    return devukFieldRepository.findAllByOrganisationUnitAndStatusIn(organisationUnit, statusCodes);
  }

  public DevukField findById(int id) {
    return devukFieldRepository.findById(id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Couldn't find DEVUK field with ID: " + id));
  }

}
