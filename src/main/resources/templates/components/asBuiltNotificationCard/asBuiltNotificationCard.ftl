<#import '../../fds/components/card/card.ftl' as fdsCard>
<#include '../asBuiltSummary/asBuiltNotificationSummary.ftl'>

<#-- @ftlvariable name="pipelineAsBuiltSubmissionView" type="uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltNotificationView"-->

<#macro asBuiltNotificationCard pipelineAsBuiltSubmissionView isOgaUser>

    <#if pipelineAsBuiltSubmissionView.submittedOnInstant?hasContent>
        <#assign submitButtonText = "Update notification"/>
    <#else>
        <#assign submitButtonText = "Submit notification"/>
    </#if>

  <@fdsCard.card>
      <@fdsCard.cardHeader cardHeadingText="${pipelineAsBuiltSubmissionView.pipelineNumber} - ${pipelineAsBuiltSubmissionView.pipelineTypeDisplay}">
          <@fdsCard.cardAction cardLinkText=submitButtonText cardLinkUrl=springUrl(pipelineAsBuiltSubmissionView.submissionLink) cardLinkScreenReaderText="for ${pipelineAsBuiltSubmissionView.pipelineNumber}"/>
      </@fdsCard.cardHeader>
      <#if pipelineAsBuiltSubmissionView.submittedOnInstant?hasContent>
          <@asBuiltNotificationSummary submission=pipelineAsBuiltSubmissionView historic=false summaryListClass="govuk-!-margin-bottom-0" isOgaUser=isOgaUser/>
      <#else>
          <div class="govuk-inset-text govuk-!-margin-bottom-0">
                This as-built notification has not been submitted
          </div>
      </#if>
  </@fdsCard.card>

</#macro>