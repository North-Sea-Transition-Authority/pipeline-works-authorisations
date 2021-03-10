package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ConsentReview;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.appprocessing.prepareconsent.ConsentReviewStatus;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ConsentReviewCaseHistoryItemService implements CaseHistoryItemService {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final ConsentReviewService consentReviewService;
  private final PersonService personService;

  @Autowired
  public ConsentReviewCaseHistoryItemService(PwaApplicationDetailService pwaApplicationDetailService,
                                             ConsentReviewService consentReviewService,
                                             PersonService personService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.consentReviewService = consentReviewService;
    this.personService = personService;
  }

  @Override
  public List<CaseHistoryItemView> getCaseHistoryItemViews(PwaApplication pwaApplication) {

    var appDetails = pwaApplicationDetailService.getAllDetailsForApplication(pwaApplication);

    var consentReviews = consentReviewService.findByPwaApplicationDetails(appDetails)
        .stream()
        .sorted(Comparator.comparing(ConsentReview::getStartTimestamp))
        .collect(Collectors.toList());

    var reviewerPersonIds = consentReviews.stream()
        .map(ConsentReview::getEndedByPersonId)
        .collect(Collectors.toSet());

    var reviewPersonIdToPersonMap = personService.findAllByIdIn(reviewerPersonIds).stream()
        .collect(Collectors.toMap(Person::getId, Function.identity()));

    return consentReviews.stream()
        .map(consentReview -> {

          var itemBuilder = new CaseHistoryItemView.Builder(
              "Consent review", consentReview.getStartTimestamp(), consentReview.getStartedByPersonId())
              .setPersonLabelText("Started by")
              .setPersonEmailLabel("Contact email");

          if (consentReview.getStatus() == ConsentReviewStatus.RETURNED) {

            var reviewerPerson = reviewPersonIdToPersonMap.get(consentReview.getEndedByPersonId());

            itemBuilder.addDataItem("Returned on", DateUtils.formatDateTime(consentReview.getEndTimestamp()));
            itemBuilder.addDataItem("Returned by", reviewerPerson.getFullName());
            itemBuilder.addDataItem("Contact email", reviewerPerson.getEmailAddress());

            itemBuilder.addDataItemRow();
            itemBuilder.addDataItem("Return reason", consentReview.getEndedReason());

          }

          if (consentReview.getStatus() == ConsentReviewStatus.APPROVED) {

            var reviewerPerson = reviewPersonIdToPersonMap.get(consentReview.getEndedByPersonId());

            itemBuilder.addDataItem("Approved on", DateUtils.formatDateTime(consentReview.getEndTimestamp()));
            itemBuilder.addDataItem("Approved by", reviewerPerson.getFullName());
            itemBuilder.addDataItem("Contact email", reviewerPerson.getEmailAddress());

          }

          return itemBuilder.build();

        })
        .collect(Collectors.toList());

  }

}
