<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="generalTechInfoView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.GeneralTechInfoView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="generalTechInfoDetails">${sectionDisplayText}</h2>

    <@generalTechInfoDetails generalTechInfoView/>

</div>


<#macro generalTechInfoDetails generalTechInfoView>

  <@fdsCheckAnswers.checkAnswers>

    <@fdsCheckAnswers.checkAnswersRow keyText="Estimated life of the field" actionUrl="" screenReaderActionText="" actionText="">
        ${generalTechInfoView.estimatedFieldLife!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Have the pipeline systems been designed in accordance with industry recognised codes and standards?" actionUrl="" screenReaderActionText="" actionText="">
        <#if generalTechInfoView.pipelineDesignedToStandards?has_content>
            ${generalTechInfoView.pipelineDesignedToStandards?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if generalTechInfoView.pipelineDesignedToStandards?has_content && generalTechInfoView.pipelineDesignedToStandards>
        <@fdsCheckAnswers.checkAnswersRow keyText="Design codes/standards for the pipelines system" actionUrl="" screenReaderActionText="" actionText="">
            ${generalTechInfoView.pipelineStandardsDescription!}
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>

    <@fdsCheckAnswers.checkAnswersRow keyText="Description of the corrosion management strategy" actionUrl="" screenReaderActionText="" actionText="">
        ${generalTechInfoView.corrosionDescription!}
    </@fdsCheckAnswers.checkAnswersRow>

    <@fdsCheckAnswers.checkAnswersRow keyText="Will there be any future tie-in points for the planned pipelines and umbilicals?" actionUrl="" screenReaderActionText="" actionText="">
        <#if generalTechInfoView.plannedPipelineTieInPoints?has_content>
            ${generalTechInfoView.plannedPipelineTieInPoints?then('Yes', 'No')}
        </#if>
    </@fdsCheckAnswers.checkAnswersRow>

    <#if generalTechInfoView.plannedPipelineTieInPoints?has_content && generalTechInfoView.plannedPipelineTieInPoints>
        <@fdsCheckAnswers.checkAnswersRow keyText="Description of tie-in points" actionUrl="" screenReaderActionText="" actionText="">
            ${generalTechInfoView.tieInPointsDescription!}
        </@fdsCheckAnswers.checkAnswersRow>
    </#if>
      
  </@fdsCheckAnswers.checkAnswers>


</#macro>

