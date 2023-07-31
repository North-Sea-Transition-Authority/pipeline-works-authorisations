package uk.co.ogauthority.pwa.service.mailmerge;

import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.DEPCON_TERMS;
import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.HUOO_TERMS;
import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.VARIATION_TERM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.termsandconditions.service.TermsAndConditionsService;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

@Service
public class TermsAndConditionsMailMergeResolver implements DocumentSourceMailMergeResolver {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final TermsAndConditionsService termsAndConditionsService;
  private final List<Class> supportedClasses = List.of(PwaApplication.class, TemplateDocumentSource.class);
  private final List<MailMergeFieldMnem> termsFields = List.of(VARIATION_TERM, HUOO_TERMS, DEPCON_TERMS);

  public TermsAndConditionsMailMergeResolver(PwaApplicationDetailService pwaApplicationDetailService,
                                             TermsAndConditionsService termsAndConditionsService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.termsAndConditionsService = termsAndConditionsService;
  }

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return supportedClasses.contains(documentSource.getClass());
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(DocumentSource documentSource) {
    return termsFields
        .stream()
        .filter(field -> field.documentSpecIsSupported(documentSource.getDocumentSpec()))
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource) {
    if (documentSource instanceof PwaApplication) {
      return resolveForApplication((PwaApplication) documentSource);
    } else {
      return resolveForTemplate((TemplateDocumentSource) documentSource);
    }
  }

  public Map<String, String> resolveForApplication(PwaApplication documentSource) {
    var resolvedFields = new HashMap<String, String>();
    var termsAndConditions = termsAndConditionsService.findByMasterPwa(documentSource.getMasterPwa());
    if (termsAndConditions.isPresent()) {
      for (var field : getAvailableMailMergeFields(documentSource)) {
        switch (field) {
          case VARIATION_TERM:
            resolvedFields.put(VARIATION_TERM.name(), String.valueOf(termsAndConditions.get().getVariationTerm()));
            break;
          case HUOO_TERMS:
            resolvedFields.put(HUOO_TERMS.name(), termsAndConditionsService.formatHuooTerms(termsAndConditions.get()));
            break;
          case DEPCON_TERMS:
            resolvedFields.put(DEPCON_TERMS.name(),
                String.valueOf(
                    "Paragraph " +
                        termsAndConditions.get().getDepconParagraph() +
                        " Schedule " +
                        termsAndConditions.get().getDepconSchedule()));
            break;
          default:
            break;
        }
      }
    } else {
      for (var field : getAvailableMailMergeFields(documentSource)) {
        resolvedFields.put(field.name(), "??" + field.name() + "??");
      }
    }
    return resolvedFields;
  }

  public Map<String, String> resolveForTemplate(TemplateDocumentSource documentSource) {
    var resolvedFields = new HashMap<String, String>();
    for (var field : getAvailableMailMergeFields(documentSource)) {
      switch (field) {
        case VARIATION_TERM:
          resolvedFields.put(VARIATION_TERM.name(), "??" + field.name() + "??");
          break;
        case HUOO_TERMS:
          resolvedFields.put(HUOO_TERMS.name(), "?? X,Y & Z??");
          break;
        case DEPCON_TERMS:
          resolvedFields.put(DEPCON_TERMS.name(), "??Paragraph X Schedule Y??");
          break;
        default:
          break;
      }
    }
    return resolvedFields;
  }
}
