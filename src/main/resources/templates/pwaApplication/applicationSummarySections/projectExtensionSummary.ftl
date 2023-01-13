<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="permissionFile" type="uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="fastTrackDetails">${sectionDisplayText}</h2>
  <@fdsCheckAnswers.checkAnswers>
    <@fdsCheckAnswers.checkAnswersRow keyText="Project extension permission" actionUrl="" screenReaderActionText="" actionText="">
      <#if permissionFile?has_content>
        <@fdsAction.link linkText=permissionFile.fileName linkUrl=springUrl(permissionFile.fileUrl) linkClass="govuk-link" linkScreenReaderText="Download ${permissionFile.fileName}" role=false start=false openInNewTab=true/> </br>
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${permissionFile.fileDescription!}</@multiLineText.multiLineText>
      <#else>
        No project layout diagram has been added to this application.
      </#if>
    </@fdsCheckAnswers.checkAnswersRow>
  </@fdsCheckAnswers.checkAnswers>
</div>