<#include './layout.ftl'>
<#include 'error/errorComponents.ftl'>

<@defaultPage title="Sorry, there is a problem with the service" pageHeading="Sorry, there is a problem with the service">
  <p class="govuk-body">Try again later.</p>
  <@errorReference errorRef!/>
  <@techSupportInfo/>
</@defaultPage>