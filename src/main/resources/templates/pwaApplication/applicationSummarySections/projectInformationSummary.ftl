<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="projectInfoView" type="uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationView" -->
<#-- @ftlvariable name="requiredQuestions" type="java.util.Set< uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationQuestion>" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="projectInformation">${sectionDisplayText}</h2>

    <@projectInfoDetails projectInfoView/>

</div>

<#macro projectInfoDetails projectInfoView>

  <#local multiLineTextBlockClass = "govuk-summary-list" />

  <@fdsCheckAnswers.checkAnswers>

    <#if requiredQuestions?seq_contains("PROJECT_NAME")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Name of project" actionUrl="" screenReaderActionText="" actionText="">
          ${projectInfoView.projectName!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("PROPOSED_START_DATE")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Proposed start of works date" actionUrl="" screenReaderActionText="" actionText="">
          ${projectInfoView.proposedStartDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("PROJECT_OVERVIEW")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Overview of project" actionUrl="" screenReaderActionText="" actionText="">
          <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.projectOverview!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("METHOD_OF_PIPELINE_DEPLOYMENT")>
        <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline installation method" actionUrl="" screenReaderActionText="" actionText="">
          <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.methodOfPipelineDeployment!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("MOBILISATION_DATE")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Date of mobilisation" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.mobilisationDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("EARLIEST_COMPLETION_DATE")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Earliest completion date" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.earliestCompletionDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

     <#if requiredQuestions?seq_contains("LATEST_COMPLETION_DATE")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Latest completion date" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.latestCompletionDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("LICENCE_TRANSFER_PLANNED")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Is a licence transfer planned?" actionUrl="" screenReaderActionText="" actionText="">
        <#if projectInfoView.licenceTransferPlanned?has_content>
          ${projectInfoView.licenceTransferPlanned?then('Yes', 'No')}
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if projectInfoView.licenceTransferPlanned?has_content && projectInfoView.licenceTransferPlanned && requiredQuestions?seq_contains("LICENCE_TRANSFER_DATE")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Licence transfer date" actionUrl="" screenReaderActionText="" actionText="">
        ${projectInfoView.licenceTransferDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if projectInfoView.licenceTransferPlanned?has_content && projectInfoView.licenceTransferPlanned && requiredQuestions?seq_contains("COMMERCIAL_AGREEMENT_DATE")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Commercial agreement date" actionUrl="" screenReaderActionText="" actionText="">
         ${projectInfoView.commercialAgreementDate!}
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if projectInfoView.licenceTransferPlanned?has_content && projectInfoView.licenceTransferPlanned && requiredQuestions?seq_contains("LICENCE_TRANSFER_REFERENCE")>
        <@fdsCheckAnswers.checkAnswersRow keyText="What are the PEARS licence application references associated with this transfer?" actionUrl="" screenReaderActionText="" actionText="">
          <ul class="govuk-list">
              <#list projectInfoView.getLicenceReferences() as references>
                <li>${references}</li>
              </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("USING_CAMPAIGN_APPROACH")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Is the work to be completed using a campaign approach?" actionUrl="" screenReaderActionText="" actionText="">
        <#if projectInfoView.usingCampaignApproach?has_content>
          ${projectInfoView.usingCampaignApproach?then('Yes', 'No')}
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("PERMANENT_DEPOSITS_BEING_MADE")>
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

    <#if requiredQuestions?seq_contains("TEMPORARY_DEPOSITS_BEING_MADE")>
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

    <#if requiredQuestions?seq_contains("FIELD_DEVELOPMENT_PLAN") && projectInfoView.isFdpQuestionRequiredBasedOnField>
      <@fdsCheckAnswers.checkAnswersRow keyText="Do you have an approved field development plan (FDP) for the fields?" actionUrl="" screenReaderActionText="" actionText="">
        <#if projectInfoView.fdpOptionSelected?has_content>
          ${projectInfoView.fdpOptionSelected?then('Yes', 'No')}
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("FIELD_DEVELOPMENT_PLAN") && projectInfoView.fdpOptionSelected?has_content && projectInfoView.fdpOptionSelected == false>
      <@fdsCheckAnswers.checkAnswersRow keyText="Reason for not having an FDP" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.fdpNotSelectedReason!}</@multiLineText.multiLineText>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <#if requiredQuestions?seq_contains("PROJECT_LAYOUT_DIAGRAM")>
      <@fdsCheckAnswers.checkAnswersRow keyText="Project layout diagram" actionUrl="" screenReaderActionText="" actionText="">
        <#if projectInfoView.layoutDiagramFileView?has_content>
          <@fdsAction.link linkText=projectInfoView.layoutDiagramFileView.fileName linkUrl=springUrl(projectInfoView.layoutDiagramFileView.fileUrl) linkClass="govuk-link" linkScreenReaderText="Download ${projectInfoView.layoutDiagramFileView.fileName}" role=false start=false openInNewTab=true/> </br>
          <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${projectInfoView.layoutDiagramFileView.fileDescription!}</@multiLineText.multiLineText>
        <#else>
          No project layout diagram has been added to this application.
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>


  </@fdsCheckAnswers.checkAnswers>


</#macro>

