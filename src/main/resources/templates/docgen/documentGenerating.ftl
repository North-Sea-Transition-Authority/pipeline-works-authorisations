<#include '../layout.ftl'>

<@defaultPage htmlTitle="Document generating" pageHeading="Your document is being generated" fullWidthColumn=true topNavigation=false>

  <@fdsInsetText.insetText>
    <p class="govuk-body">Please wait... <span class="spinner"></span></p>
    <p class="govuk-body">You will be automatically redirected when your document is ready.</p>
  </@fdsInsetText.insetText>

  <script src="${springUrl("/assets/static/js/pwa/docgenStatusPoller.js")}"></script>
  <script>
    DocgenStatusPoller.contextPath = "${springUrl("")}";
    DocgenStatusPoller.statusUrl = "${statusUrl}";
    DocgenStatusPoller.returnUrl = "${springUrl(returnUrl)}";
    DocgenStatusPoller.startPolling();
  </script>
</@defaultPage>