package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCableCrossing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddCableCrossingForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadCableCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;

@Service
public class PadCableCrossingService implements ApplicationFormSectionService {

  private final PadCableCrossingRepository padCableCrossingRepository;
  private final CableCrossingFileService cableCrossingFileService;

  @Autowired
  public PadCableCrossingService(
      PadCableCrossingRepository padCableCrossingRepository,
      CableCrossingFileService cableCrossingFileService) {
    this.padCableCrossingRepository = padCableCrossingRepository;
    this.cableCrossingFileService = cableCrossingFileService;
  }

  public PadCableCrossing getCableCrossing(PwaApplicationDetail detail, Integer id) {
    return padCableCrossingRepository.findByPwaApplicationDetailAndId(detail, id)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Unable to find cable crossing %d for detail %d", id, detail.getId())));
  }

  public List<CableCrossingView> getCableCrossingViews(PwaApplicationDetail pwaApplicationDetail) {
    return padCableCrossingRepository.findAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .sorted(PadCableCrossing::compareTo)
        .map(CableCrossingView::new)
        .collect(Collectors.toList());
  }

  @VisibleForTesting
  public void setCrossingInformationFromForm(PadCableCrossing cableCrossing, AddCableCrossingForm form) {
    cableCrossing.setCableName(form.getCableName());
    cableCrossing.setCableOwner(form.getCableOwner());
    cableCrossing.setLocation(form.getLocation());
  }

  public PadCableCrossing createCableCrossing(PwaApplicationDetail pwaApplicationDetail, AddCableCrossingForm form) {
    var cableCrossing = new PadCableCrossing();
    cableCrossing.setPwaApplicationDetail(pwaApplicationDetail);
    setCrossingInformationFromForm(cableCrossing, form);
    return padCableCrossingRepository.save(cableCrossing);
  }

  public PadCableCrossing updateCableCrossing(PwaApplicationDetail pwaApplicationDetail, Integer crossingId,
                                              AddCableCrossingForm form) {
    var cableCrossing = getCableCrossing(pwaApplicationDetail, crossingId);
    setCrossingInformationFromForm(cableCrossing, form);
    return padCableCrossingRepository.save(cableCrossing);
  }

  public void removeCableCrossing(PwaApplicationDetail pwaApplicationDetail, Integer crossingId) {
    var cableCrossing = getCableCrossing(pwaApplicationDetail, crossingId);
    padCableCrossingRepository.delete(cableCrossing);
  }

  public void mapCrossingToForm(PadCableCrossing cableCrossing, AddCableCrossingForm form) {
    form.setCableName(cableCrossing.getCableName());
    form.setCableOwner(cableCrossing.getCableOwner());
    form.setLocation(cableCrossing.getLocation());
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return cableCrossingFileService.isComplete(detail);
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }
}
