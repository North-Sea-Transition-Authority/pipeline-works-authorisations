package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import jakarta.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.MedianLineAgreementView;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;

@Service
public class PadMedianLineAgreementService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadMedianLineAgreementService.class);

  private final PadMedianLineAgreementRepository padMedianLineAgreementRepository;
  private final MedianLineAgreementValidator medianLineAgreementValidator;
  private final MedianLineCrossingFileService medianLineCrossingFileService;
  private final EntityCopyingService entityCopyingService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public PadMedianLineAgreementService(
      PadMedianLineAgreementRepository padMedianLineAgreementRepository,
      MedianLineAgreementValidator medianLineAgreementValidator,
      MedianLineCrossingFileService medianLineCrossingFileService,
      EntityCopyingService entityCopyingService,
      PadFileManagementService padFileManagementService
  ) {
    this.padMedianLineAgreementRepository = padMedianLineAgreementRepository;
    this.medianLineAgreementValidator = medianLineAgreementValidator;
    this.medianLineCrossingFileService = medianLineCrossingFileService;
    this.entityCopyingService = entityCopyingService;
    this.padFileManagementService = padFileManagementService;
  }

  public PadMedianLineAgreement getMedianLineAgreement(PwaApplicationDetail pwaApplicationDetail) {
    var agreementIfOptionalEmpty = new PadMedianLineAgreement();
    agreementIfOptionalEmpty.setPwaApplicationDetail(pwaApplicationDetail);
    return padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(agreementIfOptionalEmpty);
  }

  @Transactional
  public void save(PadMedianLineAgreement padMedianLineAgreement) {
    padMedianLineAgreementRepository.save(padMedianLineAgreement);
  }

  public void mapEntityToForm(PadMedianLineAgreement padMedianLineAgreement, MedianLineAgreementsForm form) {
    form.setAgreementStatus(padMedianLineAgreement.getAgreementStatus());
    if (padMedianLineAgreement.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_ONGOING) {
      form.setNegotiatorNameIfOngoing(padMedianLineAgreement.getNegotiatorName());
      form.setNegotiatorEmailIfOngoing(padMedianLineAgreement.getNegotiatorEmail());
    } else if (padMedianLineAgreement.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_COMPLETED) {
      form.setNegotiatorNameIfCompleted(padMedianLineAgreement.getNegotiatorName());
      form.setNegotiatorEmailIfCompleted(padMedianLineAgreement.getNegotiatorEmail());
    }
  }

  @Transactional
  public void saveEntityUsingForm(PadMedianLineAgreement padMedianLineAgreement, MedianLineAgreementsForm form) {
    padMedianLineAgreement.setAgreementStatus(form.getAgreementStatus());
    if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_ONGOING) {
      padMedianLineAgreement.setNegotiatorName(form.getNegotiatorNameIfOngoing());
      padMedianLineAgreement.setNegotiatorEmail(form.getNegotiatorEmailIfOngoing());
    } else if (form.getAgreementStatus() == MedianLineStatus.NEGOTIATIONS_COMPLETED) {
      padMedianLineAgreement.setNegotiatorName(form.getNegotiatorNameIfCompleted());
      padMedianLineAgreement.setNegotiatorEmail(form.getNegotiatorEmailIfCompleted());
    } else if (form.getAgreementStatus() == MedianLineStatus.NOT_CROSSED) {
      padMedianLineAgreement.setNegotiatorName(null);
      padMedianLineAgreement.setNegotiatorEmail(null);
    }
    padMedianLineAgreementRepository.save(padMedianLineAgreement);
  }

  public MedianLineAgreementView getMedianLineCrossingView(PwaApplicationDetail pwaApplicationDetail) {

    var medianLineAgreement = getMedianLineAgreement(pwaApplicationDetail);

    var fileViews = padFileManagementService.getUploadedFileViews(
        pwaApplicationDetail,
        FileDocumentType.MEDIAN_LINE_CROSSING
    );

    return new MedianLineAgreementView(
        medianLineAgreement.getAgreementStatus(),
        medianLineAgreement.getNegotiatorName(),
        medianLineAgreement.getNegotiatorEmail(),
        fileViews
    );

  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return isMedianLineAgreementFormComplete(detail) && medianLineCrossingFileService.isComplete(detail);

  }

  public boolean isMedianLineAgreementFormComplete(PwaApplicationDetail detail) {
    var medianLineAgreement = getMedianLineAgreement(detail);
    var form = new MedianLineAgreementsForm();
    mapEntityToForm(medianLineAgreement, form);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    validate(form, bindingResult, ValidationType.FULL, detail);
    return !bindingResult.hasErrors();
  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.FULL)) {
      medianLineAgreementValidator.validate(form, bindingResult, FullValidation.class);
    } else {
      medianLineAgreementValidator.validate(form, bindingResult);
    }
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return BooleanUtils.isTrue(pwaApplicationDetail.getMedianLineCrossed());
  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.MEDIAN_LINE_CROSSING);

    entityCopyingService.duplicateEntityAndSetParent(
        () -> padMedianLineAgreementRepository.findByPwaApplicationDetail(fromDetail)
            .orElseThrow(() -> new PwaEntityNotFoundException("Expected to find Median line agreement. pad_id:" + fromDetail.getId())),
        toDetail,
        PadMedianLineAgreement.class
    );
  }
}
