<#include '../../../layout.ftl'>
<#import '../../../feedback/serviceFeedbackLink.ftl' as serviceFeedbackLink/>


<#-- @ftlvariable name="submissionSummary" type="uk.co.ogauthority.pwa.features.application.submission.ApplicationSubmissionSummary" -->

<@defaultPage htmlTitle="Application submitted">
  <div class="govuk-panel govuk-panel--confirmation">
    <h1 class="govuk-panel__title">
      <#if submissionSummary.isFirstVersion>Application<#else>Update</#if> submitted
    </h1>
    <div class="govuk-panel__body">
      <p>Your reference number<br><strong>${submissionSummary.applicationReference}</strong></p>
    </div>
  </div>

  <ul class="govuk-list">
    <li>Submitted date and time: ${submissionSummary.getFormattedSubmissionTime()}</li>
    <li>Submitted by ${submissionSummary.submittedBy}</li>
  </ul>

  <p class="govuk-body">
    <#if submissionSummary.isFirstVersion>
      We have sent you a confirmation email.
    <#else>
      An email notification has been sent to the OGA
    </#if>
  </p>

  <h2 class="govuk-heading-m">What happens next</h2>

  <p class="govuk-body">
    <#if submissionSummary.isFirstVersion>
      Your application has been sent for review.
    <#else>
      Your application remains under review.
    </#if>
  </p>

  <#if submissionSummary.isFirstVersion>
    <p class="govuk-body">
      Any new pipelines with temporary references have been assigned pipeline numbers.
    </p>
  </#if>

  <@serviceFeedbackLink.feedbackLink feedbackUrl/>

  <@fdsAction.link linkClass="govuk-link govuk-!-font-size-19" linkText="Go back to work area" linkUrl="${springUrl(workAreaUrl)}"/>

</@defaultPage>