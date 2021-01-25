<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="appUpdateSummaryView" type="uk.co.ogauthority.pwa.model.view.appprocessing.applicationupdates.ApplicationUpdateSummaryView" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="previousAppUpdateSection">${sectionDisplayText}</h2>

    <#assign multiLineTextBlockClass = "govuk-summary-list" />

    <@fdsCheckAnswers.checkAnswers>

        <@fdsCheckAnswers.checkAnswersRow keyText="Update request" actionUrl="" screenReaderActionText="" actionText="">
            <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${appUpdateSummaryView.requestReason!}</@multiLineText.multiLineText>
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Response submitted by" actionUrl="" screenReaderActionText="" actionText="">
            ${appUpdateSummaryView.responseByPersonName}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Response submitted" actionUrl="" screenReaderActionText="" actionText="">
            ${appUpdateSummaryView.responseTimestamp}
        </@fdsCheckAnswers.checkAnswersRow>

        <@fdsCheckAnswers.checkAnswersRow keyText="Response confirmation" actionUrl="" screenReaderActionText="" actionText="">
            ${appUpdateSummaryView.responseType}
        </@fdsCheckAnswers.checkAnswersRow>

        <#if appUpdateSummaryView.responseOtherChanges?has_content>
            <@fdsCheckAnswers.checkAnswersRow keyText="Details" actionUrl="" screenReaderActionText="" actionText="">
                <@multiLineText.multiLineText blockClass=multiLineTextBlockClass>${appUpdateSummaryView.responseOtherChanges!}</@multiLineText.multiLineText>
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

    </@fdsCheckAnswers.checkAnswers>

</div>



