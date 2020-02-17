package uk.co.ogauthority.pwa.service.pwaapplications;

import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Service
public class PwaApplicationDetailService {

  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;

  @Autowired
  public PwaApplicationDetailService(PwaApplicationDetailRepository pwaApplicationDetailRepository) {
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
  }

  /**
   * Execute a caller-defined function if the tip detail for a specific PWA application is in Draft status and accessible to the user.
   * @param pwaApplicationId for application being queried
   * @param user attempting to access the application
   * @param function to execute if application is in the right state and user has access to it
   * @return result of function
   */
  public ModelAndView withDraftTipDetail(Integer pwaApplicationId,
                                         AuthenticatedUserAccount user,
                                         Function<PwaApplicationDetail, ModelAndView> function) {
    PwaApplicationDetail detail = getTipDetailWithStatus(pwaApplicationId, PwaApplicationStatus.DRAFT);
    return function.apply(detail);
  }

  /**
   * Get the tip detail for a PwaApplication if that detail's status matches the one passed-in.
   */
  public PwaApplicationDetail getTipDetailWithStatus(Integer pwaApplicationId, PwaApplicationStatus status) {
    return pwaApplicationDetailRepository.findByPwaApplicationIdAndStatusAndTipFlagIsTrue(pwaApplicationId, status)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find tip PwaApplicationDetail for PwaApplication ID: %s and status: %s",
                pwaApplicationId,
                status.name())));
  }

}
