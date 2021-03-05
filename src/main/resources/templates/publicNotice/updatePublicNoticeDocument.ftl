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
    <@multiLineText.multiLineText>
      <p class="govuk-body"> ${coverLetter} </p> 
    </@multiLineText.multiLineText>

    <#if publicNoticeDocumentComments?has_content>
      <h3 class="govuk-heading-m"> Case officer comments </h3>    
      <@multiLineText.multiLineText>
        <p class="govuk-body"> ${publicNoticeDocumentComments} </p> 
      </@multiLineText.multiLineText>
    </#if>
 
    <h3 class="govuk-heading-m"> Public notice document download </h3>
    <p class="govuk-body">
      <@fdsAction.link linkText=publicNoticeDocumentFileView.fileName linkUrl=springUrl(publicNoticeDocumentFileView.fileUrl) 
      linkClass="govuk-link" linkScreenReaderText="Download ${publicNoticeDocumentFileView.fileName}" role=false start=false openInNewTab=true/> 
    </p>

    <@grid.gridRow>
      <@grid.twoThirdsColumn>
        <@fdsFieldset.fieldset legendHeading="Public notice document" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h3" hintText="Upload the updated public notice document (parts A-D)">      
          <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl
          maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your document here" />       
        </@fdsFieldset.fieldset>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>

    <@fdsAction.submitButtons primaryButtonText="Submit to case officer" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

  </@fdsForm.htmlForm>


</@defaultPage>
