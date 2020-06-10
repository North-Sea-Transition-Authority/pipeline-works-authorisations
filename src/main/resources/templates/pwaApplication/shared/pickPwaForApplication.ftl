<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Start PWA application">
    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorEnhanced path="form.pickablePwaString" labelText="Select the PWA you are varying" options=selectablePwaMap pageHeading=true labelHeadingClass="govuk-label--l" />
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to work area" linkSecondaryActionUrl=springUrl(workareaUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>