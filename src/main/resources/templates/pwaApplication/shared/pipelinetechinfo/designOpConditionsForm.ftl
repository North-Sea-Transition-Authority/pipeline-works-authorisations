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
            labelText="What are the temperature operating conditions?" nestedPath="" unitMeasurement="°C" />

        <@minMaxInput minFormPath="form.temperatureDesignMinMax.minValue" maxFormPath="form.temperatureDesignMinMax.maxValue"
            labelText="What are the temperature design conditions?" nestedPath="" unitMeasurement="°C" />            

        <@minMaxInput minFormPath="form.pressureOpInternalExternal.minValue" maxFormPath="form.pressureOpInternalExternal.maxValue"
            labelText="What are the pressure operating conditions?" nestedPath="" unitMeasurement="barg" altMinLabel="internal" altMaxLabel="external"/>

        <@minMaxInput minFormPath="form.pressureDesignInternalExternal.minValue" maxFormPath="form.pressureDesignInternalExternal.maxValue"
            labelText="What are the pressure design conditions?" nestedPath="" unitMeasurement="barg" altMinLabel="internal" altMaxLabel="external"/>            

        <@minMaxInput minFormPath="form.flowrateOpMinMax.minValue" maxFormPath="form.flowrateOpMinMax.maxValue"
            labelText="What are the flowrate operating conditions?" nestedPath="" unitMeasurement="kscm/d" />

        <@minMaxInput minFormPath="form.flowrateDesignMinMax.minValue" maxFormPath="form.flowrateDesignMinMax.maxValue"
            labelText="What are the flowrate design conditions?" nestedPath="" unitMeasurement="kscm/d" />

        <@fdsTextInput.textInput path="form.uvalueOp" labelText="What are the U-value operating conditions?" suffix="W/m2K" inputClass="govuk-input--width-5"/>
        <@fdsTextInput.textInput path="form.uvalueDesign" labelText="What are the U-value design conditions?" suffix="W/m2K" inputClass="govuk-input--width-5"/>

        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>