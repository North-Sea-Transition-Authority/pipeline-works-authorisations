<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="generalTechInfoView" type="uk.co.ogauthority.pwa.features.application.tasks.generaltech.GeneralTechInfoView" -->
<#-- @ftlvariable name="resourceType" type="java.lang.String" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="generalTechInfoDetails">${sectionDisplayText}</h2>

    <@generalTechInfoDetails generalTechInfoView/>

</div>


<#macro generalTechInfoDetails generalTechInfoView>

  <#local multiLineTextBlockClass = "govuk-summary-list" />

  <@fdsCheckAnswers.checkAnswers>
    <#if resourceType == "PETROLEUM">
      <@fdsCheckAnswers.checkAnswersRow keyText="Estimated life of the field" actionUrl="" screenReaderActionText="" actionText="">
        <#if generalTechInfoView.estimatedAssetLife?has_content>
          ${generalTechInfoView.estimatedAssetLife} years
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    <#elseif resourceType == "CCUS">
      <@fdsCheckAnswers.checkAnswersRow keyText="Estimated life of the storage site" actionUrl="" screenReaderActionText="" actionText="">
        <#if generalTechInfoView.estimatedAssetLife?has_content>
          ${generalTechInfoView.estimatedAssetLife} years
        </#if>
      </@fdsCheckAnswers.checkAnswersRow>
    </#if>


    <@fdsCheckAnswers.checkAnswersRow keyText="Has the pipeline or pipeline system been designed in accordance with industry recognised codes and standards?" actionUrl="" screenReaderActionText="" actionText="">
        <#if generalTechInfoView.pipelineDesignedToStandards?has_content>
            ${generalTechInfoView.pipelineDesignedToStandards?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if generalTechInfoView.pipelineDesignedToStandards?has_content && generalTechInfoView.pipelineDesignedToStandards>
        <@fdsCheckAnswers.checkAnswersRow keyText="Design codes/standards for the pipelines system" actionUrl="" screenReaderActionText="" actionText="">
            <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${generalTechInfoView.pipelineStandardsDescription!} </@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <@fdsCheckAnswers.checkAnswersRow keyText="Description of the corrosion management strategy" actionUrl="" screenReaderActionText="" actionText="">
        <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${generalTechInfoView.corrosionDescription!} </@multiLineText.multiLineText>
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Will there be any future tie-in points for the planned pipelines and umbilicals?" actionUrl="" screenReaderActionText="" actionText="">
        <#if generalTechInfoView.plannedPipelineTieInPoints?has_content>
            ${generalTechInfoView.plannedPipelineTieInPoints?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if generalTechInfoView.plannedPipelineTieInPoints?has_content && generalTechInfoView.plannedPipelineTieInPoints>
        <@fdsCheckAnswers.checkAnswersRow keyText="Description of tie-in points" actionUrl="" screenReaderActionText="" actionText="">
            <@multiLineText.multiLineText blockClass=multiLineTextBlockClass> ${generalTechInfoView.tieInPointsDescription!} </@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>

  </@fdsCheckAnswers.checkAnswers>


</#macro>

