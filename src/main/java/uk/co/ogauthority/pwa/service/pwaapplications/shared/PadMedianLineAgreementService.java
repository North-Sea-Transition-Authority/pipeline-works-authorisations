package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.MedianLineAgreementsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadMedianLineAgreementRepository;

@Service
public class PadMedianLineAgreementService {

  private final PadMedianLineAgreementRepository padMedianLineAgreementRepository;

  @Autowired
  public PadMedianLineAgreementService(
      PadMedianLineAgreementRepository padMedianLineAgreementRepository) {
    this.padMedianLineAgreementRepository = padMedianLineAgreementRepository;
  }

  public PadMedianLineAgreement getMedianLineAgreementForDraft(PwaApplicationDetail pwaApplicationDetail) {
    var agreementIfOptionalEmpty = new PadMedianLineAgreement();
    agreementIfOptionalEmpty.setPwaApplicationDetail(pwaApplicationDetail);
    return padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(agreementIfOptionalEmpty);
  }

  public void save(PadMedianLineAgreement padMedianLineAgreement) {
    padMedianLineAgreementRepository.save(padMedianLineAgreement);
  }

  public void mapEntityToForm(PadMedianLineAgreement padMedianLineAgreement, MedianLineAgreementsForm form) {
    form.setAgreementStatus(padMedianLineAgreement.getAgreementStatus());
    if (padMedianLineAgreement.getAgreementStatus().equals(MedianLineStatus.NEGOTIATIONS_ONGOING)) {
      form.setNegotiatorNameIfOngoing(padMedianLineAgreement.getNegotiatorName());
      form.setNegotiatorEmailIfOngoing(padMedianLineAgreement.getNegotiatorEmail());
    } else if (padMedianLineAgreement.getAgreementStatus().equals(MedianLineStatus.NEGOTIATIONS_COMPLETED)) {
      form.setNegotiatorNameIfCompleted(padMedianLineAgreement.getNegotiatorName());
      form.setNegotiatorEmailIfCompleted(padMedianLineAgreement.getNegotiatorEmail());
    }
  }

  public void saveEntityUsingForm(PadMedianLineAgreement padMedianLineAgreement, MedianLineAgreementsForm form) {
    padMedianLineAgreement.setAgreementStatus(form.getAgreementStatus());
    if (form.getAgreementStatus().equals(MedianLineStatus.NEGOTIATIONS_ONGOING)) {
      padMedianLineAgreement.setNegotiatorName(form.getNegotiatorNameIfOngoing());
      padMedianLineAgreement.setNegotiatorEmail(form.getNegotiatorEmailIfOngoing());
    } else if (form.getAgreementStatus().equals(MedianLineStatus.NEGOTIATIONS_COMPLETED)) {
      padMedianLineAgreement.setNegotiatorName(form.getNegotiatorNameIfCompleted());
      padMedianLineAgreement.setNegotiatorEmail(form.getNegotiatorEmailIfCompleted());
    }
    padMedianLineAgreementRepository.save(padMedianLineAgreement);
  }

}
