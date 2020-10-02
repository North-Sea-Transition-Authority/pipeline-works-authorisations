<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="projectInfoView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.ProjectInformationView" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="projectInformation">${sectionDisplayText}</h2>

    <@projectInfoDetails projectInfoView/>

</div>

<#macro projectInfoDetails projectInfoView>

  <#local multiLineTextBlockClass = "govuk-summary-list" />

  <@fdsCheckAnswers.checkAnswers>

    <@fdsCheckAnswers.checkAnswersRow keyText="Name of project" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.projectName!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Proposed start of works date" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.proposedStartDate!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Overview of project" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.projectOverview!}</@multiLineText.multiLineText>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline installation method" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.methodOfPipelineDeployment!}</@multiLineText.multiLineText>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Date of mobilisation" actionUrl="" screenReaderActionText="" actionText="">
      ${projectInfoView.mobilisationDate!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Earliest completion date" actionUrl="" screenReaderActionText="" actionText="">
      ${projectInfoView.earliestCompletionDate!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Latest completion date" actionUrl="" screenReaderActionText="" actionText="">
      ${projectInfoView.latestCompletionDate!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Is a licence transfer planned?" actionUrl="" screenReaderActionText="" actionText="">
      <#if projectInfoView.licenceTransferPlanned?has_content>
        ${projectInfoView.licenceTransferPlanned?then('Yes', 'No')}
      </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if projectInfoView.licenceTransferPlanned?has_content && projectInfoView.licenceTransferPlanned>
      <@fdsCheckAnswers.checkAnswersRow keyText="Licence transfer date" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.licenceTransferDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if projectInfoView.licenceTransferPlanned?has_content && projectInfoView.licenceTransferPlanned>
      <@fdsCheckAnswers.checkAnswersRow keyText="Commercial agreement date" actionUrl="" screenReaderActionText="" actionText="">
         ${projectInfoView.commercialAgreementDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <@fdsCheckAnswers.checkAnswersRow keyText="Is the work to be completed using a campaign approach?" actionUrl="" screenReaderActionText="" actionText="">
      <#if projectInfoView.usingCampaignApproach?has_content>
        ${projectInfoView.usingCampaignApproach?then('Yes', 'No')}
      </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if projectInfoView.anyDepQuestionRequired>
      <#if projectInfoView.permDepQuestionRequired>
        <@fdsCheckAnswers.checkAnswersRow keyText="Are permanent deposits being made?" actionUrl="" screenReaderActionText="" actionText="">
          <#if projectInfoView.permanentDepositsMadeType?has_content>
            ${projectInfoView.permanentDepositsMadeType.getDisplayText()}
          </#if>
        </@fdsCheckAnswers.checkAnswersRow>

        <#if projectInfoView.permanentDepositsMadeType?has_content && projectInfoView.permanentDepositsMadeType == "LATER_APP">
          <@fdsCheckAnswers.checkAnswersRow keyText="Month and year that later application will be submitted" actionUrl="" screenReaderActionText="" actionText="">
            <#if projectInfoView.futureSubmissionDate?has_content>
              ${projectInfoView.futureSubmissionDate}
            </#if>
          </@fdsCheckAnswers.checkAnswersRow>
        </#if>
      </#if>

      <@fdsCheckAnswers.checkAnswersRow keyText="Are temporary deposits being made as part of this application?" actionUrl="" screenReaderActionText="" actionText="">
        <#if projectInfoView.temporaryDepositsMade?has_content>
          ${projectInfoView.temporaryDepositsMade?then('Yes', 'No')}
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>

      <#if projectInfoView.temporaryDepositsMade?has_content && projectInfoView.temporaryDepositsMade>
        <@fdsCheckAnswers.checkAnswersRow keyText="Description of temporary deposits" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.temporaryDepDescription!}</@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>
      </#if>
    </#if>

    <#if projectInfoView.fdpQuestionRequired>
      <@fdsCheckAnswers.checkAnswersRow keyText="Do you have an approved field development plan (FDP) for the fields?" actionUrl="" screenReaderActionText="" actionText="">
        <#if projectInfoView.fdpOptionSelected?has_content>
          ${projectInfoView.fdpOptionSelected?then('Yes', 'No')}
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>

      <#if projectInfoView.fdpOptionSelected?has_content && projectInfoView.fdpOptionSelected == false>
        <@fdsCheckAnswers.checkAnswersRow keyText="Reason for not having an FDP" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.fdpNotSelectedReason!}</@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>
      </#if>
    </#if>

    <@fdsCheckAnswers.checkAnswersRow keyText="Project layout diagram" actionUrl="" screenReaderActionText="" actionText="">
      <#if projectInfoView.layoutDiagramFileView?has_content>
        <@fdsAction.link linkText=projectInfoView.layoutDiagramFileView.fileName linkUrl=springUrl(projectInfoView.layoutDiagramFileView.fileUrl) linkClass="govuk-link" linkScreenReaderText="Download ${projectInfoView.layoutDiagramFileView.fileName}" role=false start=false openInNewTab=true/> </br>
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.layoutDiagramFileView.fileDescription!}</@multiLineText.multiLineText>
      <#else>
        No project layout diagram has been added to this application.
      </#if>
    </@fdsCheckAnswers.checkAnswersRow>


  </@fdsCheckAnswers.checkAnswers>


</#macro>

