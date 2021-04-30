<#-- @ftlvariable name="publicNoticeDocumentFileView" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="coverLetter" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} view public notice" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">View public notice</h2>

  <h3 class="govuk-heading-m"> Cover letter </h3>    
  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <@multiLineText.multiLineText blockClass="public-notice__text">
          <p class="govuk-body"> ${coverLetter} </p>
      </@multiLineText.multiLineText>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>

  <h3 class="govuk-heading-m"> Public notice document download </h3>
  <p class="govuk-body">
    <@fdsAction.link linkText=publicNoticeDocumentFileView.fileName linkUrl=springUrl(publicNoticeDocumentFileView.fileUrl) 
    linkClass="govuk-link" linkScreenReaderText="Download document ${publicNoticeDocumentFileView.fileName}" role=false start=false openInNewTab=true/>
  </p>

  <@fdsAction.link linkText="Go back" linkUrl=springUrl(cancelUrl) linkClass="govuk-link govuk-!-font-size-19" linkScreenReaderText="Go back to previous page" role=false start=false/> 

</@defaultPage>
