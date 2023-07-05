package uk.co.ogauthority.pwa.features.termsandconditions.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.termsandconditions.model.PwaTermsAndConditions;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsPwaView;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsPwaViewRepository;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsRepository;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class TermsAndConditionsService {

  private final TermsAndConditionsRepository termsAndConditionsRepository;
  private final TermsAndConditionsValidator termsAndConditionsValidator;
  private final MasterPwaService masterPwaService;
  private final TermsAndConditionsPwaViewRepository termsAndConditionsPwaViewRepository;

  public TermsAndConditionsService(TermsAndConditionsRepository termsAndConditionsRepository,
                                   TermsAndConditionsValidator termsAndConditionsValidator,
                                   MasterPwaService masterPwaService,
                                   TermsAndConditionsPwaViewRepository termsAndConditionsPwaViewRepository) {
    this.termsAndConditionsRepository = termsAndConditionsRepository;
    this.termsAndConditionsValidator = termsAndConditionsValidator;
    this.masterPwaService = masterPwaService;
    this.termsAndConditionsPwaViewRepository = termsAndConditionsPwaViewRepository;
  }

  public void saveForm(TermsAndConditionsForm form, int personId) {
    termsAndConditionsRepository.save(convertFormToEntity(form, personId));
  }

  public BindingResult validateForm(TermsAndConditionsForm form, BindingResult bindingResult) {
    termsAndConditionsValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public Map<String, String> getPwasForSelector() {
    var availablePwas = StreamSupport.stream(termsAndConditionsPwaViewRepository.findAll().spliterator(), false);
    return availablePwas.collect(StreamUtils
        .toLinkedHashMap(e -> String.valueOf(e.getPwaId()), TermsAndConditionsPwaView::getConsentReference));
  }

  private PwaTermsAndConditions convertFormToEntity(TermsAndConditionsForm form, int personId) {
    return new PwaTermsAndConditions()
        .setMasterPwa(masterPwaService.getMasterPwaById(form.getPwaId()))
        .setVariationTerm(form.getVariationTerm())
        .setHuooTerms(generateHuooTerms(form))
        .setDepconParagraph(form.getDepconParagraph())
        .setDepconSchedule(form.getDepconSchedule())
        .setCreatedBy(personId)
        .setCreatedTimestamp(Instant.now());
  }

  private String generateHuooTerms(TermsAndConditionsForm form) {
    Integer[] terms = {form.getHuooTermOne(), form.getHuooTermTwo(), form.getHuooTermThree()};
    Arrays.sort(terms);
    return String.format("%s, %s & %s", terms[0], terms[1], terms[2]);
  }

}