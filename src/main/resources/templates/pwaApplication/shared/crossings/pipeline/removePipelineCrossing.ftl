<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<@defaultPage htmlTitle="Remove pipeline crossing" pageHeading="Remove pipeline crossing" breadcrumbs=true>

    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues key="Pipeline reference" value=view.reference/>
        <@fdsDataItems.dataValues key="Pipeline owners" value=view.owners/>
    </@fdsDataItems.dataItem>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline crossing" secondaryLinkText="Back to crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>