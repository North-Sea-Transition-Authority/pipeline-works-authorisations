package uk.co.ogauthority.pwa.service.mailmerge;

import static uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem.PAGE_BREAK;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.documents.templates.TemplateDocumentSource;

@Service
public class PageBreakMailMergeResolver implements DocumentSourceMailMergeResolver {

  private final Set<Class<? extends DocumentSource>> supportedClasses = Set.of(PwaApplication.class, TemplateDocumentSource.class);
  private final List<MailMergeFieldMnem> mmFields = List.of(PAGE_BREAK);

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return supportedClasses.contains(documentSource.getClass());
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(DocumentSource documentSource) {
    return mmFields;
  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource) {
    return mmFields.stream()
        .collect(Collectors.toMap(MailMergeFieldMnem::name, MailMergeFieldMnem::asMailMergeTag));
  }
}
