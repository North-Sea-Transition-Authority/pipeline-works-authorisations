package uk.co.ogauthority.pwa.service.documents;

import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.documents.instances.DocumentInstanceService;
import uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateService;

@Service
public class DocumentService {

  private final DocumentTemplateService documentTemplateService;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  public DocumentService(DocumentTemplateService documentTemplateService,
                         DocumentInstanceService documentInstanceService) {
    this.documentTemplateService = documentTemplateService;
    this.documentInstanceService = documentInstanceService;
  }

  /**
   * Create a document instance based on a document template.
   * @param application document is being created for
   * @param templateMnem identifier to allow use of correct template
   * @param creatingUser person creating document
   *
   */
  @Transactional
  public void createDocumentInstance(PwaApplication application,
                                     DocumentTemplateMnem templateMnem,
                                     Person creatingUser) {

    var documentDto = documentTemplateService.populateDocumentDtoFromTemplateMnem(templateMnem);

    documentInstanceService.createFromDocumentDto(application, documentDto, creatingUser);

  }

  /**
   * Remove all clauses linked to a document instance before re-inserting them based on the latest template.
   * @param pwaApplication that document is being reloaded for
   * @param templateMnem identifier of document based on template used
   * @param reloadingPerson person reloading document
   */
  @Transactional
  public void reloadDocumentInstance(PwaApplication pwaApplication,
                                     DocumentTemplateMnem templateMnem,
                                     Person reloadingPerson) {

    documentInstanceService.clearClauses(pwaApplication, templateMnem);

    createDocumentInstance(pwaApplication, templateMnem, reloadingPerson);

  }

  public Optional<DocumentInstance> getDocumentInstance(PwaApplication pwaApplication,
                                                        DocumentTemplateMnem templateMnem) {
    return documentInstanceService.getDocumentInstance(pwaApplication, templateMnem);
  }

  public DocumentView getDocumentViewForInstance(DocumentInstance instance) {

    return documentInstanceService.getDocumentView(instance);

  }

}
