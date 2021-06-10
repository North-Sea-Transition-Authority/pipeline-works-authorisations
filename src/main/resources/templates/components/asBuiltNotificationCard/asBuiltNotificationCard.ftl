<#import '../../fds/components/card/card.ftl' as fdsCard>

<#-- @ftlvariable name="pipelineAsBuiltSubmissionView" type="uk.co.ogauthority.pwa.model.view.asbuilt.AsBuiltPipelineNotificationSubmissionView"-->

<#macro asBuiltNotificationCard pipelineAsBuiltSubmissionView>

  <#if pipelineAsBuiltSubmissionView.submittedOnInstant?hasContent>
      <#assign submitButtonText = "Submit update"/>
    <#else>
      <#assign submitButtonText = "Submit"/>
  </#if>

  <@fdsCard.card>
      <@fdsCard.cardHeader cardHeadingText="${pipelineAsBuiltSubmissionView.pipelineNumber} - ${pipelineAsBuiltSubmissionView.pipelineTypeDisplay}">
          <@fdsCard.cardAction cardLinkText=submitButtonText cardLinkUrl=springUrl(pipelineAsBuiltSubmissionView.submissionLink) cardLinkScreenReaderText="Card with card actions and file list"/>
      </@fdsCard.cardHeader>
      <#if pipelineAsBuiltSubmissionView.submittedOnInstant?hasContent>
        <dl class="govuk-summary-list">
          <#if pipelineAsBuiltSubmissionView.submittedByPersonName?hasContent>
            <div class="govuk-summary-list__row">
              <dt class="govuk-summary-list__key">
                Submitted by
              </dt>
              <dd class="govuk-summary-list__value">
                ${pipelineAsBuiltSubmissionView.submittedByPersonName}
              </dd>
            </div>
          </#if>
          <#if pipelineAsBuiltSubmissionView.submittedOnInstant?hasContent>
            <div class="govuk-summary-list__row">
              <dt class="govuk-summary-list__key">
                Submitted on
              </dt>
              <dd class="govuk-summary-list__value">
                ${pipelineAsBuiltSubmissionView.submittedOnInstantDisplay}
              </dd>
            </div>
          </#if>
          <#if pipelineAsBuiltSubmissionView.asBuiltNotificationStatusDisplay?hasContent>
            <div class="govuk-summary-list__row">
              <dt class="govuk-summary-list__key">
                As-built status
              </dt>
              <dd class="govuk-summary-list__value">
                ${pipelineAsBuiltSubmissionView.asBuiltNotificationStatusDisplay}
              </dd>
            </div>
          </#if>
          <#if pipelineAsBuiltSubmissionView.dateLaid?hasContent>
            <div class="govuk-summary-list__row">
              <dt class="govuk-summary-list__key">
                Date laid
              </dt>
              <dd class="govuk-summary-list__value">
                ${pipelineAsBuiltSubmissionView.dateLaidDisplay}
              </dd>
            </div>
            <#elseIf pipelineAsBuiltSubmissionView.expectedDateLaid?hasContent>
              <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">
                  Expected laid date
                </dt>
                <dd class="govuk-summary-list__value">
                  ${pipelineAsBuiltSubmissionView.expectedLaidDateDisplay}
                </dd>
              </div>
          </#if>
          <#if pipelineAsBuiltSubmissionView.dateBroughtIntoUse?hasContent>
            <div class="govuk-summary-list__row">
              <dt class="govuk-summary-list__key">
                Date pipeline was/will be brought into use
              </dt>
              <dd class="govuk-summary-list__value">
                ${pipelineAsBuiltSubmissionView.dateBroughtIntoUseDisplay}
              </dd>
            </div>
          </#if>
        </dl>
        <#else>
          <div class="govuk-inset-text govuk-!-margin-bottom-0">
            This as-built notification has not been submitted
          </div>
      </#if>
  </@fdsCard.card>

</#macro>