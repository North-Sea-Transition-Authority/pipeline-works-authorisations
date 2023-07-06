package uk.co.ogauthority.pwa.service.mailmerge;

import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.DEPCON_TERMS;
import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.HUOO_TERMS;
import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.VARIATION_TERM;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;

@Service
public class TermsAndConditionsMailMergeResolver implements DocumentSourceMailMergeResolver {
  private List<Class> supportedClasses = List.of(PwaApplication.class, TemplateDocumentSource.class);

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return supportedClasses.contains(documentSource.getClass());
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(DocumentSource documentSource) {
    var fields = List.of(VARIATION_TERM, HUOO_TERMS, DEPCON_TERMS);
    return fields
        .stream()
        .filter(field -> field.documentSpecIsSupported(documentSource.getDocumentSpec()))
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource) {
    return Collections.emptyMap();
  }
}
