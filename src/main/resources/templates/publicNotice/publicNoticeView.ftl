<#include '../layout.ftl'>

<#-- @ftlvariable name="publicNoticeViewData" type="uk.co.ogauthority.pwa.model.view.publicnotice.PublicNoticeView" -->
<#-- @ftlvariable name="publicNoticeActions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction>" -->
<#-- @ftlvariable name="existingPublicNoticeActions" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction>" -->

<#macro publicNoticeView publicNoticeViewData displayAsHistoricalRequest=false existingPublicNoticeActions=[] publicNoticeActions=[] historicalRequestNumber=0>

    <#if displayAsHistoricalRequest>
        <#assign submittedHeading = "Submitted ${publicNoticeViewData.submittedTimestamp}"/>
    </#if>

        <#assign content>
            <#if !displayAsHistoricalRequest>
                <@fdsSummaryList.summaryListCardActionList>
                    <#list existingPublicNoticeActions as publicNoticeAction>
                        <#if publicNoticeActions?seq_contains(publicNoticeAction)>
                            <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(actionUrlMap[publicNoticeAction.name()]) itemText=publicNoticeAction.getDisplayText() itemScreenReaderText=publicNoticeAction.getScreenReaderText()/>
                        </#if>
                    </#list>
                </@fdsSummaryList.summaryListCardActionList>
            <#else>
                <@fdsSummaryList.summaryListCardActionList>
                    <#if publicNoticeViewData.documentDownloadUrl?has_content>
                      <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(publicNoticeViewData.documentDownloadUrl) itemText="Download" itemScreenReaderText="public notice document"/>
                    </#if>
                    </@fdsSummaryList.summaryListCardActionList>
            </#if>
        </#assign>

            <#if !displayAsHistoricalRequest>
                <#assign cardHeading = "Public notice"/>
            <#else>
                <#assign cardHeading = "Previous public notice #${historicalRequestNumber}"/>
            </#if>

            <@fdsSummaryList.summaryListCard headingText=cardHeading cardActionsContent=content summaryListId="summary-card-list">

            <#if !displayAsHistoricalRequest>
                <@fdsSummaryList.summaryListRowNoAction keyText="Submitted">
                    ${publicNoticeViewData.submittedTimestamp}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <@fdsSummaryList.summaryListRowNoAction keyText="Status">
                ${publicNoticeViewData.status.getDisplayText()}
            </@fdsSummaryList.summaryListRowNoAction>

            <#if publicNoticeViewData.publicNoticeRequestStatus?has_content && !displayAsHistoricalRequest>
                <@fdsSummaryList.summaryListRowNoAction keyText="Public notice request status">
                    ${publicNoticeViewData.publicNoticeRequestStatus.getDisplayText()}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <#if publicNoticeViewData.rejectionReason?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText="Rejection reason">
                    ${publicNoticeViewData.rejectionReason}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <#if publicNoticeViewData.status == "WITHDRAWN">
                <@fdsSummaryList.summaryListRowNoAction keyText="Withdrawn by">
                    ${publicNoticeViewData.withdrawnByPersonName}
                </@fdsSummaryList.summaryListRowNoAction>
                <@fdsSummaryList.summaryListRowNoAction keyText="Withdrawn on">
                    ${publicNoticeViewData.withdrawnTimestamp}
                </@fdsSummaryList.summaryListRowNoAction>
                <@fdsSummaryList.summaryListRowNoAction keyText="Withdrawal reason">
                    ${publicNoticeViewData.withdrawalReason}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <#if publicNoticeViewData.latestDocumentComments?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText="Case officer comments">
                   <@multiLineText.multiLineText>
                        <p class="govuk-body"> ${publicNoticeViewData.latestDocumentComments} </p>
                    </@multiLineText.multiLineText>
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <#if publicNoticeViewData.publicationStartTimestamp?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText="Publication start date">
                   ${publicNoticeViewData.publicationStartTimestamp}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <#if publicNoticeViewData.publicationEndTimestamp?has_content>
                <@fdsSummaryList.summaryListRowNoAction keyText="Publication end date">
                   ${publicNoticeViewData.publicationEndTimestamp}
                </@fdsSummaryList.summaryListRowNoAction>
            </#if>

            <@fdsDetails.summaryDetails summaryTitle="Show history" detailsClass="govuk-!-margin-bottom-0 govuk-!-margin-top-4">
                <@fdsTimeline.timeline>
                    <@fdsTimeline.timelineSection>
                        <#list publicNoticeViewData.publicNoticeEvents as event>
                            <#if event?is_last>
                                <#assign class="fds-timeline__time-stamp--no-border"/>
                            </#if>

                            <@fdsTimeline.timelineTimeStamp
                            timeStampHeading=event.eventType.getDisplayText()
                            timeStampHeadingHint=event.eventTimestampString
                            nodeNumber=" "
                            timeStampClass=class
                            >
                                <@fdsTimeline.timelineEvent>
                                    <#if event.personId?has_content>
                                        <@fdsDataItems.dataItem>
                                            <@fdsDataItems.dataValues key=event.eventType.getActionText() value=event.getPersonName()!""/>
                                        </@fdsDataItems.dataItem>
                                    </#if>

                                    <#if event.publicationStartDate?has_content>
                                        <@fdsDataItems.dataItem>
                                            <@fdsDataItems.dataValues key="Publication start date" value=event.publicationStartDate/>
                                        </@fdsDataItems.dataItem>
                                    </#if>

                                    <#if event.publicationEndDate?has_content>
                                        <@fdsDataItems.dataItem>
                                            <@fdsDataItems.dataValues key="Publication end date" value=event.publicationEndDate/>
                                        </@fdsDataItems.dataItem>
                                    </#if>

                                    <#if event.comment?has_content>
                                        <@fdsDataItems.dataItem>
                                            <@fdsDataItems.dataValues key="Review comment" value=event.comment/>
                                        </@fdsDataItems.dataItem>
                                    </#if>
                                </@fdsTimeline.timelineEvent>
                            </@fdsTimeline.timelineTimeStamp>
                        </#list>
                    </@fdsTimeline.timelineSection>
                </@fdsTimeline.timeline>
            </@fdsDetails.summaryDetails>

    </@fdsSummaryList.summaryListCard>

</#macro>
