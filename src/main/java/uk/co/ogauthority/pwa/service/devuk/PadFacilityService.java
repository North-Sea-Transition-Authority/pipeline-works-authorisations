package uk.co.ogauthority.pwa.service.devuk;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.repository.devuk.PadFacilityRepository;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadFacilityService {

  private final PadFacilityRepository padFacilityRepository;
  private final DevukFacilityService devukFacilityService;

  @Autowired
  public PadFacilityService(PadFacilityRepository padFacilityRepository,
                            DevukFacilityService devukFacilityService) {
    this.padFacilityRepository = padFacilityRepository;
    this.devukFacilityService = devukFacilityService;
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
    List<String> facilities;
    if (form.getWithinSafetyZone() == HseSafetyZone.PARTIALLY && form.getFacilitiesIfPartially().size() > 0) {
      facilities = form.getFacilitiesIfPartially();
    } else if (form.getWithinSafetyZone() == HseSafetyZone.YES && form.getFacilitiesIfYes().size() > 0) {
      facilities = form.getFacilitiesIfYes();
    } else {
      return;
    }

    var linkedFacilityIds = facilities.stream()
        .filter(s -> !s.startsWith(SearchSelectable.FREE_TEXT_PREFIX))
        .collect(Collectors.toList());

    var manualFacilityIds = facilities.stream()
        .filter(s -> s.startsWith(SearchSelectable.FREE_TEXT_PREFIX))
        .collect(Collectors.toList());

    var facilityTest = devukFacilityService.getFacilitiesInIds(linkedFacilityIds);

    devukFacilityService.getFacilitiesInIds(linkedFacilityIds)
        .forEach(facility -> {
          var created = createFromDevukFacility(pwaApplicationDetail, facility);
          padFacilityRepository.save(created);
        });

    manualFacilityIds.forEach(s -> createFacilityFromManualEntry(pwaApplicationDetail, s));
  }

  private void createFacilityFromManualEntry(PwaApplicationDetail pwaApplicationDetail, String id) {
    var facility = new PadFacility();
    facility.setPwaApplicationDetail(pwaApplicationDetail);
    facility.setFacilityNameManualEntry(StringUtils.stripStart(id, SearchSelectable.FREE_TEXT_PREFIX));
    padFacilityRepository.save(facility);
  }

  public void mapFacilitiesToView(List<PadFacility> facilities, LocationDetailsForm locationDetailsForm,
                                         ModelAndView modelAndView) {
    if (locationDetailsForm.getWithinSafetyZone() != null) {
      var devukFacilities = facilities.stream()
          .filter(PadFacility::isLinkedToDevukFacility)
          .map(PadFacility::getFacility)
          .collect(StreamUtils.toLinkedHashMap(
              devukFacility -> String.valueOf(devukFacility.getId()),
              DevukFacility::getFacilityName));

      var manualFacilities = facilities.stream()
          .filter(padFacility -> !padFacility.isLinkedToDevukFacility())
          .collect(StreamUtils.toLinkedHashMap(
              devukFacility -> SearchSelectable.FREE_TEXT_PREFIX + devukFacility.getFacilityNameManualEntry(),
              PadFacility::getFacilityNameManualEntry
          ));

      var joinedFacilities = new HashMap<>();
      joinedFacilities.putAll(devukFacilities);
      joinedFacilities.putAll(manualFacilities);

      switch (locationDetailsForm.getWithinSafetyZone()) {
        case PARTIALLY:
          modelAndView.addObject("preselectedFacilitiesIfPartially", joinedFacilities);
          break;
        case YES:
          modelAndView.addObject("preselectedFacilitiesIfYes", joinedFacilities);
          break;
        default:
          break;
      }
    }
  }
}
