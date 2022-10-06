<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="publicNoticeDocumentFileView" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="coverLetter" type="java.lang.String" -->
<#-- @ftlvariable name="publicNoticeDocumentComments" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} update public notice document" topNavigation=true fullWidthColumn=true breadcrumbs=true>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList/>
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Update public notice document</h2>
  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <@fdsForm.htmlForm>
        <h3 class="govuk-heading-m"> Cover letter </h3>
        <@multiLineText.multiLineText blockClass="public-notice__text">
          <p class="govuk-body"> ${coverLetter} </p>
        </@multiLineText.multiLineText>

        <#if publicNoticeDocumentComments?has_content>
          <h3 class="govuk-heading-m"> Case officer comments </h3>
          <@multiLineText.multiLineText blockClass="public-notice__text">
            <p class="govuk-body"> ${publicNoticeDocumentComments} </p>
          </@multiLineText.multiLineText>
        </#if>

        <h3 class="govuk-heading-m"> Public notice document download </h3>
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
        <@fdsSummaryList.summaryList>
          <@fdsSummaryList.summaryListRowNoAction keyText="File name">
            <@fdsAction.link linkText=publicNoticeDocumentFileView.fileName linkUrl=springUrl(publicNoticeDocumentFileView.fileUrl)
            linkClass="govuk-link" linkScreenReaderText="${publicNoticeDocumentFileView.fileName}" role=false start=false openInNewTab=true/>
          </@fdsSummaryList.summaryListRowNoAction>
          <@fdsSummaryList.summaryListRowNoAction keyText="File description">
            ${publicNoticeDocumentFileView.fileDescription}
          </@fdsSummaryList.summaryListRowNoAction>
        </@fdsSummaryList.summaryList>
        <@fdsFieldset.fieldset legendHeading="Public notice document" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h3" hintText="Upload the updated public notice document (parts A-D)">
          <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl multiFile=false
          maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl dropzoneText="Drag and drop your document here" />
        </@fdsFieldset.fieldset>
        <@fdsAction.submitButtons primaryButtonText="Submit to case officer" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
      </@fdsForm.htmlForm>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>
</@defaultPage>
