<#include '../../pwaLayoutImports.ftl'>
<#import '../shared/options/optionConfirmationSummary.ftl' as optionsConfirmationSummary>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="view" type="uk.co.ogauthority.pwa.service.pwaapplications.options.PadConfirmationOfOptionView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="optionConfirmation">${sectionDisplayText}</h2>

    <@optionsConfirmationSummary.summary view></@optionsConfirmationSummary.summary>
<#--    <@fdsCheckAnswers.checkAnswers>-->
<#--        <#assign multiLineTextBlockClass = "govuk-summary-list" />-->

<#--        <@fdsCheckAnswers.checkAnswersRow keyText="Confirmed work" actionUrl="" screenReaderActionText="" actionText="">-->
<#--            ${view.workType!""}-->
<#--        </@fdsCheckAnswers.checkAnswersRow>-->

<#--        <#if view.workDescription?has_content>-->
<#--            <@fdsCheckAnswers.checkAnswersRow keyText="Description of work" actionUrl="" screenReaderActionText="" actionText="">-->
<#--                <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${view.workDescription!""}</@multiLineText.multiLineText>-->
<#--            </@fdsCheckAnswers.checkAnswersRow>-->
<#--        </#if>-->


<#--    </@fdsCheckAnswers.checkAnswers>-->

</div>

