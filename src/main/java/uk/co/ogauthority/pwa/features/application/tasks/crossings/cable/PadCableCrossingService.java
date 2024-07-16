package uk.co.ogauthority.pwa.features.application.tasks.crossings.cable;

import com.google.common.annotations.VisibleForTesting;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class PadCableCrossingService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadCableCrossingService.class);

  private final PadCableCrossingRepository padCableCrossingRepository;
  private final CableCrossingFileService cableCrossingFileService;
  private final EntityCopyingService entityCopyingService;
  private final PadFileService padFileService;

  @Autowired
  public PadCableCrossingService(
      PadCableCrossingRepository padCableCrossingRepository,
      CableCrossingFileService cableCrossingFileService,
      EntityCopyingService entityCopyingService, PadFileService padFileService) {
    this.padCableCrossingRepository = padCableCrossingRepository;
    this.cableCrossingFileService = cableCrossingFileService;
    this.entityCopyingService = entityCopyingService;
    this.padFileService = padFileService;
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
    var cableCount = padCableCrossingRepository.countAllByPwaApplicationDetail(detail);
    return cableCrossingFileService.isComplete(detail) && cableCount > 0;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return BooleanUtils.isTrue(pwaApplicationDetail.getCablesCrossed());
  }

  @Override
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    var cableCount = padCableCrossingRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
    return List.of(
        new TaskInfo("CABLE", (long) cableCount)
    );
  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    var copiedCableCrossingEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padCableCrossingRepository.findAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadCableCrossing.class
    );

    padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        ApplicationDetailFilePurpose.CABLE_CROSSINGS,
        ApplicationFileLinkStatus.FULL);

  }
}
