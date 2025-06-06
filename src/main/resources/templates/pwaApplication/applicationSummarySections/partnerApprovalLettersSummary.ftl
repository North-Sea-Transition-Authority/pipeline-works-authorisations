<#include '../../pwaLayoutImports.ftl'>
<#include 'appSummaryUtils.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="partnerLettersView" type="uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerLettersView" -->
<#-- @ftlvariable name="partnerLettersUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerApprovalLettersUrlFactory" -->


<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="partnerLettersDetails">${sectionDisplayText}</h2>

    <@partnerLettersDetails partnerLettersView/>

</div>

<#macro partnerLettersDetails partnerLettersView>

  <@fdsCheckAnswers.checkAnswers>

    <@fdsCheckAnswers.checkAnswersRow keyText="Do you need to provide partner approval letters?" actionUrl="" screenReaderActionText="" actionText="">
      <#if partnerLettersView.partnerLettersRequired?has_content>
         <@showYesNoForBool partnerLettersView.partnerLettersRequired/>
      </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if partnerLettersView.partnerLettersRequired?has_content && partnerLettersView.partnerLettersRequired>
      <@fdsCheckAnswers.checkAnswersRow keyText="Confirmation of providing all required partner approval letters" actionUrl="" screenReaderActionText="" actionText="">
        <#if partnerLettersView.partnerLettersConfirmed?has_content>
         <@showConfirmedForBool partnerLettersView.partnerLettersConfirmed/>
        <#else>
          Not provided
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

  </@fdsCheckAnswers.checkAnswers>


  <#if partnerLettersView.partnerLettersRequired?has_content && partnerLettersView.partnerLettersRequired && partnerLettersView.uploadedLetterFileViews?has_content>
    <h3 class="govuk-heading-m"> Uploaded letters </h3>
    <@pwaFiles.uploadedFileList downloadUrl=springUrl(partnerLettersUrlFactory.getDocumentDownloadUrl()) existingFiles=partnerLettersView.uploadedLetterFileViews />
  <#elseif partnerLettersView.partnerLettersRequired?has_content && partnerLettersView.partnerLettersRequired>
    <@fdsInsetText.insetText>
      No partner approval letters have been added to this application.
    </@fdsInsetText.insetText>
  </#if>


</#macro>

