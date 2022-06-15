<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="publicNoticeDocumentFileView" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="coverLetter" type="java.lang.String" -->
<#-- @ftlvariable name="publicNoticeDocumentComments" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} update public notice document" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList/>
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Update public notice document</h2>

  <@fdsForm.htmlForm>

    <h3 class="govuk-heading-m"> Cover letter </h3>
    <@grid.gridRow>
      <@grid.twoThirdsColumn>
        <@multiLineText.multiLineText blockClass="public-notice__text">
            <p class="govuk-body"> ${coverLetter} </p>
        </@multiLineText.multiLineText>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>

    <#if publicNoticeDocumentComments?has_content>
      <h3 class="govuk-heading-m"> Case officer comments </h3>
      <@multiLineText.multiLineText blockClass="public-notice__text">
        <p class="govuk-body"> ${publicNoticeDocumentComments} </p>
      </@multiLineText.multiLineText>
    </#if>

    <h3 class="govuk-heading-m"> Public notice document download </h3>
    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <@fdsInsetText.insetText>
              <p class="govuk-body">
                Review Annexes A-D of the attached public notice. Sections that you must update are
                enclosed in square brackets and shown in red text.
                If any parties not shown within the table in Annex D are served a notice, their details, including email addresses,
                should be supplied to NSTA by updating Annex D.
              </p>

              <p class="govuk-body">
                Upload the updated public notice document and return it to the case officer.
              </p>
            </@fdsInsetText.insetText>
        </@grid.twoThirdsColumn>
    </@grid.gridRow>
      <@fdsCheckAnswers.checkAnswers>
        <@fdsCheckAnswers.checkAnswersRow keyText="File Name" actionUrl="" screenReaderActionText="" actionText="">
          <@fdsAction.link linkText=publicNoticeDocumentFileView.fileName linkUrl=springUrl(publicNoticeDocumentFileView.fileUrl)
          linkClass="govuk-link" linkScreenReaderText="Download ${publicNoticeDocumentFileView.fileName}" role=false start=false openInNewTab=true/>
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="File Description" actionUrl="" screenReaderActionText="" actionText="">
          ${publicNoticeDocumentFileView.fileDescription}
        </@fdsCheckAnswers.checkAnswersRow>
      </@fdsCheckAnswers.checkAnswers>
      <@fdsFieldset.fieldset legendHeading="Public notice document" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h3" hintText="Upload the updated public notice document (parts A-D)">
        <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl multiFile=false
        maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl dropzoneText="Drag and drop your document here" />
      </@fdsFieldset.fieldset>


    <@fdsAction.submitButtons primaryButtonText="Submit to case officer" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

  </@fdsForm.htmlForm>


</@defaultPage>
