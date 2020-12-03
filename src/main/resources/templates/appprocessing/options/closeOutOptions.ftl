<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Close out options variation" topNavigation=true breadcrumbs=true fullWidthColumn=true>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <@fdsInsetText.insetText >
      <p> TODO: some close out guidance </p>
    </@fdsInsetText.insetText>


    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons primaryButtonText="Close out options" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>