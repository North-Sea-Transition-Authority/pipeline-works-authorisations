package uk.co.ogauthority.pwa.service.appprocessing.casehistory;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.appprocessing.casenotes.CaseNoteController;
import uk.co.ogauthority.pwa.model.entity.appprocessing.casenotes.CaseNote;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class CaseHistoryItemViewFactory {

  public static CaseHistoryItemView create(CaseNote caseNote, List<UploadedFileView> fileViews) {

    var dataItems = new LinkedHashMap<String, String>();
    dataItems.put("Note text", caseNote.getNoteText());

    var pwaApplication = caseNote.getPwaApplication();

    return new CaseHistoryItemView(
        "Case note",
        caseNote.getDateTime(),
        "Created by",
        caseNote.getPersonId(),
        dataItems,
        fileViews,
        ReverseRouter.route(on(CaseNoteController.class)
            .handleDownload(pwaApplication.getApplicationType(), pwaApplication.getId(), null, null)
        ));

  }

}
