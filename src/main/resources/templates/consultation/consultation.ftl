<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} consultations" pageHeading="${appRef} consultations" topNavigation=true twoThirdsColumn=false>





  <@fdsAction.link linkText="Request consultations" linkUrl=springUrl(requestConsultationsUrl) linkClass="govuk-button"/>
</@defaultPage>