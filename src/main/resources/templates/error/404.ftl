<#include '../layout.ftl'>
<#include 'errorComponents.ftl'>

<@defaultPage title="Page not found" pageHeading="Page not found">
  <p class="govuk-body">
    If you typed the web address, check it is correct.
  </p>
  <p class="govuk-body">
    If you pasted the web address, check you copied the entire address.
  </p>
  <@errorReference errorRef!/>
  <@techSupportInfo/>
</@defaultPage>