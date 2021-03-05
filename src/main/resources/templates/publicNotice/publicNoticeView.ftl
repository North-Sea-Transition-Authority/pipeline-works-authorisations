<#include '../layout.ftl'>

<#-- @ftlvariable name="publicNoticeViewData" type="uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView" -->



<#macro publicNoticeView publicNoticeViewData displayAsHistoricalRequest=false>

    <#if displayAsHistoricalRequest>
        <#assign submittedHeading = "Submitted ${publicNoticeViewData.submittedTimestamp}"/>
    </#if>

    <@fdsCheckAnswers.checkAnswersWrapper summaryListId="" headingText=submittedHeading! headingSize="h3" headingClass="govuk-heading-m">
        <@fdsCheckAnswers.checkAnswers summaryListClass="">

            <#if !displayAsHistoricalRequest>
                <@fdsCheckAnswers.checkAnswersRow keyText="Submitted" actionText="" actionUrl="" screenReaderActionText="">
                    ${publicNoticeViewData.submittedTimestamp}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

            <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
                ${publicNoticeViewData.status.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRow>

            <#if publicNoticeViewData.latestDocumentComments?has_content>
                <@fdsCheckAnswers.checkAnswersRow keyText="Case officer comments" actionText="" actionUrl="" screenReaderActionText="">
                   <@multiLineText.multiLineText>
                        <p class="govuk-body"> ${publicNoticeViewData.latestDocumentComments} </p> 
                    </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

        </@fdsCheckAnswers.checkAnswers>    
    </@fdsCheckAnswers.checkAnswersWrapper>




</#macro>
