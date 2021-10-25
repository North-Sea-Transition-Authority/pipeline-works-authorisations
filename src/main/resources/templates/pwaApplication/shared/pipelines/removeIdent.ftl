<#include '../../../layout.ftl'>
<#import '../../applicationSummarySections/appSummaryUtils.ftl' as summaryUtils>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="coreType" type="uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineCoreType" -->

<@defaultPage htmlTitle="Remove ident" pageHeading="Are you sure you want to remove this ident?" breadcrumbs=true>

    <#assign from>
        <@pwaCoordinate.display coordinatePair=identView.fromCoordinates />
    </#assign>
    <#assign to>
        <@pwaCoordinate.display coordinatePair=identView.toCoordinates />
    </#assign>

  <dl class="govuk-summary-list govuk-!-margin-bottom-9">
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Ident number</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty identView.identNumber/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Length</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty identView.length/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty identView.fromLocation/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From (coordinates)</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty from/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty identView.toLocation/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To (coordinates)</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty to/></dd>
    </div>

    <#assign isSingleCore = false>
    <#if coreType == "SINGLE_CORE">
      <#assign isSingleCore = true>
    </#if>
    <#assign externalDiameter = isSingleCore?then(identView.externalDiameter!, identView.externalDiameterMultiCore!) />
    <#assign internalDiameter = isSingleCore?then(identView.internalDiameter!, identView.internalDiameterMultiCore!) />
    <#assign wallThickness = isSingleCore?then(identView.wallThickness!, identView.wallThicknessMultiCore!) />
    <#assign maop = isSingleCore?then(identView.maop!, identView.maopMultiCore!) />
    <#assign insulationCoatingType = isSingleCore?then(identView.insulationCoatingType!, identView.insulationCoatingTypeMultiCore!) />
    <#assign productsToBeConveyed = isSingleCore?then(identView.productsToBeConveyed!, identView.productsToBeConveyedMultiCore!) />

    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">External diameter</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty value=externalDiameter suffix='mm'/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Internal diameter</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty value=internalDiameter suffix='mm'/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Wall thickness</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty value=wallThickness suffix='mm'/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">MAOP</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty value=maop suffix='barg'/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Insulation / coating type</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty insulationCoatingType/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Products to be conveyed</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty productsToBeConveyed/></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Description of component part</dt>
      <dd class="govuk-summary-list__value"><@summaryUtils.showNotProvidedWhenEmpty identView.componentPartsDescription/></dd>
    </div>    
  </dl>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove ident" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>
