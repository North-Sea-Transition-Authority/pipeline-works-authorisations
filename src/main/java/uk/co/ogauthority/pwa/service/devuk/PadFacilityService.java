package uk.co.ogauthority.pwa.service.devuk;

import java.util.HashMap;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.entity.devuk.PadFacility;
import uk.co.ogauthority.pwa.model.entity.enums.HseSafetyZone;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.location.LocationDetailsForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.model.search.SearchSelectionView;
import uk.co.ogauthority.pwa.repository.devuk.PadFacilityRepository;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadFacilityService {

  private final PadFacilityRepository padFacilityRepository;
  private final DevukFacilityService devukFacilityService;
  private final SearchSelectorService searchSelectorService;

  @Autowired
  public PadFacilityService(PadFacilityRepository padFacilityRepository,
                            DevukFacilityService devukFacilityService,
                            SearchSelectorService searchSelectorService) {
    this.padFacilityRepository = padFacilityRepository;
    this.devukFacilityService = devukFacilityService;
    this.searchSelectorService = searchSelectorService;
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

    if (form.getWithinSafetyZone() == HseSafetyZone.PARTIALLY
        && form.getPartiallyWithinSafetyZoneForm().getFacilities().size() > 0) {
      facilities = form.getPartiallyWithinSafetyZoneForm().getFacilities();

    } else if (form.getWithinSafetyZone() == HseSafetyZone.YES
        && form.getCompletelyWithinSafetyZoneForm().getFacilities().size() > 0) {
      facilities = form.getCompletelyWithinSafetyZoneForm().getFacilities();

    } else {
      return;
    }

    var selectionView = new SearchSelectionView<>(facilities, s -> s);

    devukFacilityService.getFacilitiesInIds(selectionView.getLinkedEntries())
        .forEach(facility -> {
          var created = createFromDevukFacility(pwaApplicationDetail, facility);
          padFacilityRepository.save(created);
        });

    selectionView.getManualEntries()
        .forEach(s -> createFacilityFromManualEntry(pwaApplicationDetail, s));
  }

  private void createFacilityFromManualEntry(PwaApplicationDetail pwaApplicationDetail, String id) {
    var facility = new PadFacility();
    facility.setPwaApplicationDetail(pwaApplicationDetail);
    facility.setFacilityNameManualEntry(searchSelectorService.removePrefix(id));
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
