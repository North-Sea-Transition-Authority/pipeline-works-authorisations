<#include '../../../layout.ftl'>
<#include 'minMaxInput.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="backUrl" type=" java.lang.String"-->

<@defaultPage htmlTitle="Design and operating conditions" pageHeading="Design and operating conditions" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        
        <@minMaxInput minFormPath="form.temperatureOpMinMax.minValue" maxFormPath="form.temperatureOpMinMax.maxValue"
                    nestedPath="" unitMeasurement=""/>

        <@minMaxInput minFormPath="form.pressureOpInternalExternal.minValue" maxFormPath="form.pressureOpInternalExternal.maxValue"
                    nestedPath="" unitMeasurement="" altMinLabel="internal" altMaxLabel="external"/>
                


        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>
