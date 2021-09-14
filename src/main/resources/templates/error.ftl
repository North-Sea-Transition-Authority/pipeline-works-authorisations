<#include './layout.ftl'>
<#include 'error/errorComponents.ftl'>

<@defaultPage htmlTitle="Sorry, there is a problem with the service" pageHeading="Sorry, there is a problem with the service">

  <p class="govuk-body">Try again later.</p>
  <p class="govuk-body">
    If you continue to experience this problem, contact the service desk using the
    details below. Be sure to include the error reference below in any correspondence.
  </p>

  <ul class="govuk-list">
    <li>${technicalSupportContact.serviceName}</li>
    <li>${technicalSupportContact.phoneNumber}</li>
    <li>
        <@fdsAction.link
        linkText=technicalSupportContact.emailAddress
        linkUrl="mailto:${technicalSupportContact.emailAddress}?subject=${technicalSupportContact.serviceName} - Error reference ${errorRef}"
        />
    </li>
  </ul>

  <@errorReference errorRef!/>

</@defaultPage>