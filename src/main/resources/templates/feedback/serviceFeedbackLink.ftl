<#include '../layout.ftl'/>

<#macro feedbackLink feedbackUrl>
  <p class="govuk-body">
    <@fdsAction.link
      linkClass="govuk-link govuk-!-font-size-19"
      linkText="What did you think of this service?"
      linkUrl=springUrl(feedbackUrl)
    />
    <span> (takes 30 seconds)</span>
  </p>
</#macro>