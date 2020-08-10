<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->

<div class="pwa-application-summary-section">
  <h2 class="govuk-heading-l" id="permanentDeposits">${sectionDisplayText}</h2>
  <#list depositList as deposit>
      <@diffedDepositViewSummary deposit=deposit/>
  </#list>
</div>

<#macro diffedDepositViewSummary deposit>
  <dl class="govuk-summary-list govuk-!-margin-bottom-9">

    <h3 class="govuk-heading-m">${deposit.depositReference}</h3>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Pipelines</dt>
      <dd class="govuk-summary-list__value">
          <#list deposit.pipelineRefs as pipelineRef>${pipelineRef}<br> </#list>
      </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Proposed start date</dt>
      <dd class="govuk-summary-list__value"> ${deposit.fromDateEstimate}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">End date</dt>
      <dd class="govuk-summary-list__value"> ${deposit.toDateEstimate}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Type of materials</dt>
      <dd class="govuk-summary-list__value"> ${deposit.materialType.value}
          <#if deposit.materialType.tag.displayName?has_content><strong class="govuk-tag">
              ${deposit.materialType.tag.displayName}
            </strong> </#if></dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Size</dt>
      <dd class="govuk-summary-list__value"> ${deposit.materialSize} </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Quantity</dt>
      <dd class="govuk-summary-list__value"> ${deposit.quantity}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Contingency included</dt>
      <dd class="govuk-summary-list__value"> ${deposit.contingencyAmount}</dd>
    </div>
      <#if deposit.materialTypeLookup == "GROUT_BAGS" && (deposit.groutBagsBioDegradable?? && deposit.groutBagsBioDegradable == false)>
        <div class="govuk-summary-list__row">
          <dt class="govuk-summary-list__key">Non-biodegradable grout bags reason</dt>
          <dd class="govuk-summary-list__value"> ${deposit.bioGroutBagsNotUsedDescription}</dd>
        </div>
      </#if>

    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From (WGS84)</dt>
      <dd class="govuk-summary-list__value">
          <@pwaCoordinate.display coordinatePair=deposit.fromCoordinates />
      </dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To (WGS84)</dt>
      <dd class="govuk-summary-list__value">
          <@pwaCoordinate.display coordinatePair=deposit.toCoordinates />
      </dd>
    </div>
  </dl>
</#macro>