<#include '../../../layout.ftl'>

<#-- @ftlvariable name="submissionSummary" type="uk.co.ogauthority.pwa.service.pwaapplications.generic.summary.ApplicationSubmissionSummary" -->

<@defaultPage htmlTitle="Application submitted" pageHeading="Application submitted">
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

  <p class="govuk-body">We have sent you a confirmation email.</p>

  <h2 class="govuk-heading-m">What happens next</h2>

  <p class="govuk-body">
    Your application has been sent for review.
  </p>

  <@fdsAction.link linkClass="govuk-link govuk-!-font-size-19" linkText="Go back to work area" linkUrl="${springUrl(workAreaUrl)}"/>

</@defaultPage>