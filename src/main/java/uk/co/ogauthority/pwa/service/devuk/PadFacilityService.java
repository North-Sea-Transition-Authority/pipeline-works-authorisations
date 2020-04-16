package uk.co.ogauthority.pwa.service.devuk;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.repository.devuk.PadFacilityRepository;

@Service
public class PadFacilityService {

  private final PadFacilityRepository padFacilityRepository;

  @Autowired
  public PadFacilityService(PadFacilityRepository padFacilityRepository) {
    this.padFacilityRepository = padFacilityRepository;
  }

  public List<PadFacility> getFacilities(PwaApplicationDetail pwaApplicationDetail) {
    return padFacilityRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  private PadFacility createFromDevukFacility(PwaApplicationDetail pwaApplicationDetail, DevukFacility devukFacility) {
    var facility = new PadFacility();
    facility.setFacility(devukFacility);
    facility.setPwaApplicationDetail(pwaApplicationDetail);
    return facility;
  }

  @Transactional
  public void setFacilities(PwaApplicationDetail pwaApplicationDetail, LocationDetailsForm form) {
    getFacilities(pwaApplicationDetail).forEach(padFacilityRepository::delete);
    List<DevukFacility> facilities;
    if (form.getWithinSafetyZone() == HseSafetyZone.PARTIALLY && form.getFacilitiesIfPartially().size() > 0) {
      facilities = form.getFacilitiesIfPartially();
    } else if (form.getWithinSafetyZone() == HseSafetyZone.YES && form.getFacilitiesIfYes().size() > 0) {
      facilities = form.getFacilitiesIfYes();
    } else {
      return;
    }
    facilities.forEach(facility -> {
      var created = createFromDevukFacility(pwaApplicationDetail, facility);
      padFacilityRepository.save(created);
    });
  }

  public void mapFacilitiesToForm(List<PadFacility> facilities, LocationDetailsForm locationDetailsForm) {
    if (locationDetailsForm.getWithinSafetyZone() != null) {
      var devukFacilities = facilities.stream()
          .filter(PadFacility::isLinkedToDevukFacility)
          .map(PadFacility::getFacility)
          .collect(Collectors.toList());

      switch (locationDetailsForm.getWithinSafetyZone()) {
        case PARTIALLY:
          locationDetailsForm.setFacilitiesIfPartially(devukFacilities);
          break;
        case YES:
          locationDetailsForm.setFacilitiesIfYes(devukFacilities);
          break;
        default:
          break;
      }
    }
  }
}
