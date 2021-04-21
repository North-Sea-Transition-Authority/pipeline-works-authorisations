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
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Submitted">
                    ${publicNoticeViewData.submittedTimestamp}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>

            <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Status">
                ${publicNoticeViewData.status.getDisplayText()}
            </@fdsCheckAnswers.checkAnswersRowNoAction>

            <#if publicNoticeViewData.status == "WITHDRAWN">
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Withdrawn by">
                    ${publicNoticeViewData.withdrawnByPersonName}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Withdrawn on">
                    ${publicNoticeViewData.withdrawnTimestamp}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Withdrawal reason">
                    ${publicNoticeViewData.withdrawalReason}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>

            <#if publicNoticeViewData.latestDocumentComments?has_content>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Case officer comments">
                   <@multiLineText.multiLineText>
                        <p class="govuk-body"> ${publicNoticeViewData.latestDocumentComments} </p> 
                    </@multiLineText.multiLineText>
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>            

            <#if publicNoticeViewData.publicationStartTimestamp?has_content>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Publication start date">
                   ${publicNoticeViewData.publicationStartTimestamp}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>
            
            <#if publicNoticeViewData.publicationEndTimestamp?has_content>
                <@fdsCheckAnswers.checkAnswersRowNoAction keyText="Publication end date">
                   ${publicNoticeViewData.publicationEndTimestamp}
                </@fdsCheckAnswers.checkAnswersRowNoAction>
            </#if>

        </@fdsCheckAnswers.checkAnswers>    
    </@fdsCheckAnswers.checkAnswersWrapper>




</#macro>

