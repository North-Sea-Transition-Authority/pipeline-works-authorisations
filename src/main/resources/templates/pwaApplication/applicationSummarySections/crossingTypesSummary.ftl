<#include '../../pwaLayoutImports.ftl'>
<#include 'appSummaryUtils.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="crossingTypesView" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.CrossingTypesView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="crossingTypeDetails">${sectionDisplayText}</h2>

    <@crossingTypeDetails crossingTypesView/>

</div>


<#macro crossingTypeDetails crossingTypesView>

    <@fdsCheckAnswers.checkAnswers>

        <@fdsCheckAnswers.checkAnswersRow keyText="Are any pipelines crossed?" actionUrl="" screenReaderActionText="" actionText="">
            <#if crossingTypesView.pipelinesCrossed?has_content>
                <@showYesNoForBool crossingTypesView.pipelinesCrossed/>
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>                 

        <@fdsCheckAnswers.checkAnswersRow keyText="Are any cables crossed?" actionUrl="" screenReaderActionText="" actionText="">
            <#if crossingTypesView.cablesCrossed?has_content>
                <@showYesNoForBool crossingTypesView.cablesCrossed/>
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>           

        <@fdsCheckAnswers.checkAnswersRow keyText="Is any median line crossed?" actionUrl="" screenReaderActionText="" actionText="">
            <#if crossingTypesView.medianLineCrossed?has_content>
                <@showYesNoForBool crossingTypesView.medianLineCrossed/>
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>         
            
    </@fdsCheckAnswers.checkAnswers>

</#macro>