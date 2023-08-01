package uk.co.ogauthority.pwa.features.termsandconditions.service;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.termsandconditions.controller.TermsAndConditionsManagementController;
import uk.co.ogauthority.pwa.features.termsandconditions.model.PwaTermsAndConditions;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsForm;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsManagementViewItem;
import uk.co.ogauthority.pwa.features.termsandconditions.model.TermsAndConditionsPwaView;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsPwaViewRepository;
import uk.co.ogauthority.pwa.features.termsandconditions.repository.TermsAndConditionsRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class TermsAndConditionsService {

  private static final int PAGE_SIZE = 10;
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

  public void saveForm(TermsAndConditionsForm form, Person person) {
    termsAndConditionsRepository.save(convertFormToEntity(form, person));
  }

  public BindingResult validateForm(TermsAndConditionsForm form, BindingResult bindingResult) {
    termsAndConditionsValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public TermsAndConditionsForm getTermsAndConditionsForm(Integer masterPwaId) {
    var masterPwa = masterPwaService.getMasterPwaById(masterPwaId);

    return findByMasterPwa(masterPwa).map(this::convertEntityToForm).orElse(new TermsAndConditionsForm());
  }

  public Optional<PwaTermsAndConditions> findByMasterPwa(MasterPwa masterPwa) {
    return termsAndConditionsRepository.findPwaTermsAndConditionsByMasterPwa(masterPwa);
  }

  public Map<String, String> getPwasForSelector() {
    return StreamSupport.stream(termsAndConditionsPwaViewRepository.findAll().spliterator(), false)
        .collect(StreamUtils.toLinkedHashMap(
            e -> String.valueOf(e.getPwaId()),
            TermsAndConditionsPwaView::getConsentReference)
        );
  }

  public PageView<TermsAndConditionsManagementViewItem> getPwaManagementScreenPageView(int pageNumber, String filter) {
    var route = ReverseRouter.route(on(TermsAndConditionsManagementController.class)
        .renderTermsAndConditionsManagement(null, pageNumber, null));

    var pwaPageViewMap = masterPwaService.searchConsentedDetailsByReference(filter).stream()
        .collect(Collectors.toMap(MasterPwaDetail::getMasterPwa, MasterPwaDetail::getReference));

    return PageView.fromPage(
        termsAndConditionsRepository.findAllByMasterPwaIn(getTermsAndConditionsRequest(pageNumber), pwaPageViewMap.keySet()),
        route,
        pwaTermsAndConditions -> new TermsAndConditionsManagementViewItem(
            pwaTermsAndConditions,
            pwaPageViewMap.get(pwaTermsAndConditions.getMasterPwa())
        )
    );
  }

  private Pageable getTermsAndConditionsRequest(int pageNumber) {
    return PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("createdTimestamp").descending());
  }

  private PwaTermsAndConditions convertFormToEntity(TermsAndConditionsForm form, Person person) {
    var masterPwa = masterPwaService.getMasterPwaById(form.getPwaId());
    var pwaTermsAndConditions = findByMasterPwa(masterPwa).orElse(new PwaTermsAndConditions());

    return pwaTermsAndConditions
        .setMasterPwa(masterPwa)
        .setVariationTerm(form.getVariationTerm())
        .setHuooTermOne(form.getHuooTermOne())
        .setHuooTermTwo(form.getHuooTermTwo())
        .setHuooTermThree(form.getHuooTermThree())
        .setDepconParagraph(form.getDepconParagraph())
        .setDepconSchedule(form.getDepconSchedule())
        .setCreatedBy(person.getId())
        .setCreatedTimestamp(Instant.now());
  }

  private TermsAndConditionsForm convertEntityToForm(PwaTermsAndConditions pwaTermsAndConditions) {
    return new TermsAndConditionsForm()
        .setPwaId(pwaTermsAndConditions.getMasterPwa().getId())
        .setVariationTerm(pwaTermsAndConditions.getVariationTerm())
        .setHuooTermOne(pwaTermsAndConditions.getHuooTermOne())
        .setHuooTermTwo(pwaTermsAndConditions.getHuooTermTwo())
        .setHuooTermThree(pwaTermsAndConditions.getHuooTermThree())
        .setDepconParagraph(pwaTermsAndConditions.getDepconParagraph())
        .setDepconSchedule(pwaTermsAndConditions.getDepconSchedule());
  }
}