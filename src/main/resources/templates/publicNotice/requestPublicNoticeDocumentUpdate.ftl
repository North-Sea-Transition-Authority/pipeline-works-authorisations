<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="publicNoticeDocumentFileView" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} request update for public notice document" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList/>
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Request update for public notice document</h2>

  <@fdsForm.htmlForm>
     
    <h3 class="govuk-heading-m"> Public notice document download </h3>
    <p class="govuk-body">
      <@fdsAction.link linkText=publicNoticeDocumentFileView.fileName linkUrl=springUrl(publicNoticeDocumentFileView.fileUrl) 
      linkClass="govuk-link" linkScreenReaderText="Download ${publicNoticeDocumentFileView.fileName}" role=false start=false openInNewTab=true/> 
    </p>

    <@fdsTextarea.textarea path="form.comments" labelText="Comments on updates required" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>

    <@fdsAction.submitButtons primaryButtonText="Request update" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

  </@fdsForm.htmlForm>


</@defaultPage>
