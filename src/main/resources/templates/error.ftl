<#include './layout.ftl'>
<#include 'error/errorComponents.ftl'>

<@defaultPage htmlTitle="Sorry, there is a problem with the service" pageHeading="Sorry, there is a problem with the service">
  <p class="govuk-body">Try again later.</p>
  <@errorReference errorRef!/>
</@defaultPage>