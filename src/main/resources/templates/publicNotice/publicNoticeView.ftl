<#include '../layout.ftl'>

<#-- @ftlvariable name="publicNoticeViewData" type="uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView" -->



<#macro publicNoticeView publicNoticeViewData displayAsHistoricalRequest=false>

    <#if displayAsHistoricalRequest>
        <h3 class="govuk-heading-m"> Submitted ${publicNoticeViewData.submittedTimestamp} </h3>
    </#if>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">
        <#nested/>
        <#if !displayAsHistoricalRequest>
            <@fdsCheckAnswers.checkAnswersRow keyText="Submitted" actionText="" actionUrl="" screenReaderActionText="">
                ${publicNoticeViewData.submittedTimestamp}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
            ${publicNoticeViewData.status.getDisplayText()}
        </@fdsCheckAnswers.checkAnswersRow>


    </@fdsCheckAnswers.checkAnswers>


</#macro>

