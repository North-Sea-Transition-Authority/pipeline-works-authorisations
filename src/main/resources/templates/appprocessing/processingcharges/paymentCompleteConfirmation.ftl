
<#-- @ftlvariable name="workAreaUrl" type="java.lang.String" -->
<#-- @ftlvariable name="caseManagementUrl" type="java.lang.String" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->

<#include '../../layout.ftl'>

<#assign pageHeading="Application payment complete" />
<#assign pageHeadingWithAppRef="${pageHeading} ${appRef}" />

<@defaultPage htmlTitle="${pageHeadingWithAppRef}" topNavigation=true breadcrumbs=false fullWidthColumn=true>

    <div class="govuk-panel govuk-panel--confirmation">
        <h1 class="govuk-panel__title">
            Application ${appRef} successfully completed payment
        </h1>
    </div>

    <h2 class="govuk-heading-m">What happens next</h2>
    <p class="govuk-body">Your application will be processed by the assigned case officer.</p>
    <ul class="govuk-list govuk-list--bullet">
        <li>You can <@fdsAction.link linkClass="govuk-link govuk-!-font-size-19" linkText="view your application" linkUrl="${springUrl(caseManagementUrl)}"/> or;</li>
        <li>You can <@fdsAction.link linkClass="govuk-link govuk-!-font-size-19" linkText="work on another application" linkUrl="${springUrl(workAreaUrl)}"/> from the work area.</li>
    </ul>

</@defaultPage>