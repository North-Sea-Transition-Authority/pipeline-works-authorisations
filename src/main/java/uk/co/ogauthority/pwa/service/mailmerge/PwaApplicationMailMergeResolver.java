package uk.co.ogauthority.pwa.service.mailmerge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.mailmerge.MailMergeField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.documents.DocumentSource;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class PwaApplicationMailMergeResolver implements DocumentSourceMailMergeResolver {

  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public PwaApplicationMailMergeResolver(PwaApplicationDetailService pwaApplicationDetailService,
                                         PadProjectInformationService padProjectInformationService) {
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @Override
  public boolean supportsDocumentSource(DocumentSource documentSource) {
    return documentSource.getClass().equals(PwaApplication.class);
  }

  @Override
  public Map<String, String> resolveMergeFields(DocumentSource documentSource,
                                                Collection<MailMergeField> mailMergeFields) {

    var app = (PwaApplication) documentSource;
    var detail = pwaApplicationDetailService.getLatestSubmittedDetail(app)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format("No submitted detail for app with id [%s]", app.getId())));
    var projectInformation = padProjectInformationService.getPadProjectInformationData(detail);

    var map = new HashMap<String, String>();

    mailMergeFields.forEach(mailMergeField -> {

      if (mailMergeField.getMnem() == MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE) {
        map.put(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.name(),
            DateUtils.formatDate(projectInformation.getProposedStartTimestamp()));
      }

      if (mailMergeField.getMnem() == MailMergeFieldMnem.PROJECT_NAME) {
        map.put(MailMergeFieldMnem.PROJECT_NAME.name(), projectInformation.getProjectName());
      }

    });

    return map;

  }
}
