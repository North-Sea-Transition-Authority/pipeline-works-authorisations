<#include '../layout.ftl'>

<#-- @ftlvariable name="publicNoticeViewData" type="uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView" -->
<#-- @ftlvariable name="publicNoticeActions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction>" -->
<#-- @ftlvariable name="existingPublicNoticeActions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction>" -->



<#macro publicNoticeView publicNoticeViewData displayAsHistoricalRequest=false existingPublicNoticeActions=[] publicNoticeActions=[]>

    <#if displayAsHistoricalRequest>
        <#assign submittedHeading = "Submitted ${publicNoticeViewData.submittedTimestamp}"/>
    </#if>

    <@fdsCheckAnswers.checkAnswersWrapper summaryListId="" headingText=submittedHeading! headingSize="h3" headingClass="govuk-heading-m">
    
        <#if !displayAsHistoricalRequest>
            <#list existingPublicNoticeActions as publicNoticeAction>
                <#if publicNoticeActions?seq_contains(publicNoticeAction)>
                    <@fdsAction.link linkText=publicNoticeAction.getDisplayText() linkUrl=springUrl(actionUrlMap[publicNoticeAction.name()]) linkClass="govuk-link govuk-link--stand-alone" role=false start=false /> &nbsp;
                </#if>
            </#list>
        </#if>

        <@fdsCheckAnswers.checkAnswers summaryListClass="">

            <#if !displayAsHistoricalRequest>
                <@fdsCheckAnswers.checkAnswersRow keyText="Submitted" actionText="" actionUrl="" screenReaderActionText="">
                    ${publicNoticeViewData.submittedTimestamp}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

            <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionText="" actionUrl="" screenReaderActionText="">
                ${publicNoticeViewData.status.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRow>

            <#if publicNoticeViewData.status == "WITHDRAWN">
                <@fdsCheckAnswers.checkAnswersRow keyText="Withdrawn by" actionText="" actionUrl="" screenReaderActionText="">
                    ${publicNoticeViewData.withdrawnByPersonName}
                </@fdsCheckAnswers.checkAnswersRow>
                <@fdsCheckAnswers.checkAnswersRow keyText="Withdrawn on" actionText="" actionUrl="" screenReaderActionText="">
                    ${publicNoticeViewData.withdrawnTimestamp}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

            <#if publicNoticeViewData.latestDocumentComments?has_content>
                <@fdsCheckAnswers.checkAnswersRow keyText="Case officer comments" actionText="" actionUrl="" screenReaderActionText="">
                   <@multiLineText.multiLineText>
                        <p class="govuk-body"> ${publicNoticeViewData.latestDocumentComments} </p> 
                    </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>            

            <#if publicNoticeViewData.publicationStartTimestamp?has_content>
                <@fdsCheckAnswers.checkAnswersRow keyText="Publication start date" actionText="" actionUrl="" screenReaderActionText="">
                   ${publicNoticeViewData.publicationStartTimestamp}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>
            
            <#if publicNoticeViewData.publicationEndTimestamp?has_content>
                <@fdsCheckAnswers.checkAnswersRow keyText="Publication end date" actionText="" actionUrl="" screenReaderActionText="">
                   ${publicNoticeViewData.publicationEndTimestamp}
                </@fdsCheckAnswers.checkAnswersRow>
            </#if>

        </@fdsCheckAnswers.checkAnswers>    
    </@fdsCheckAnswers.checkAnswersWrapper>




</#macro>

