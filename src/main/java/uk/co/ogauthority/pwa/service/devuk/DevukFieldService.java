package uk.co.ogauthority.pwa.service.devuk;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.devuk.DevukFieldId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectionView;
import uk.co.ogauthority.pwa.repository.devuk.DevukFieldRepository;

@Service
public class DevukFieldService {

  private final DevukFieldRepository devukFieldRepository;

  @Autowired
  public DevukFieldService(DevukFieldRepository devukFieldRepository) {
    this.devukFieldRepository = devukFieldRepository;
  }

  public List<DevukField> getByOrganisationUnitWithStatusCodes(PortalOrganisationUnit organisationUnit, List<Integer> statusCodes) {
    return devukFieldRepository.findAllByOperatorOuIdAndStatusIn(organisationUnit.getOuId(), statusCodes);
  }

  public List<DevukField> getByStatusCodes(List<Integer> statusCodes) {
    return devukFieldRepository.findAllByStatusIn(statusCodes);
  }

  public DevukField findById(int id) {
    return devukFieldRepository.findById(id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Couldn't find DEVUK field with ID: " + id));
  }

  public List<DevukField> findByDevukFieldIds(Collection<DevukFieldId> devukFieldIds) {
    return IterableUtils.toList(
        devukFieldRepository.findAllById(
            devukFieldIds
                .stream()
                .map(DevukFieldId::asInt)
                .collect(Collectors.toSet())
        )
    );
  }

  public SearchSelectionView getLinkedAndManualFieldEntries(List<String> fieldIds) {
    return new SearchSelectionView<>(fieldIds,
        pickedFieldString -> findById(Integer.parseInt(pickedFieldString)));
  }

}
