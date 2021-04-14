<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="deposit" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositOverview" -->


<#macro depositViewSummary deposit>
    <dl class="govuk-summary-list govuk-!-margin-bottom-9">

        <#if deposit.depositIsForConsentedPipeline?has_content>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Is the deposit for a consented pipeline or a pipeline that is on this application?</dt>
                <dd class="govuk-summary-list__value">
                    ${deposit.depositIsForConsentedPipeline?then('Yes', 'No')}
                </dd>
            </div>
        </#if>
        <#if deposit.depositIsForConsentedPipeline?has_content && deposit.depositIsForConsentedPipeline>            
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Pipelines</dt>
                <dd class="govuk-summary-list__value">
                    <#list deposit.pipelineRefs as pipelineRef>${pipelineRef}<br> </#list>
                </dd>
            </div>
        </#if>
        <#if deposit.depositIsForPipelinesOnOtherApp?has_content>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Is the deposit for proposed pipelines on other applications that haven’t yet been consented?</dt>
                <dd class="govuk-summary-list__value">
                    ${deposit.depositIsForPipelinesOnOtherApp?then('Yes', 'No')}
                </dd>
            </div>
        </#if>
        <#if deposit.depositIsForPipelinesOnOtherApp?has_content && deposit.depositIsForPipelinesOnOtherApp>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Application reference and proposed pipeline numbers </dt>
                <dd class="govuk-summary-list__value">
                    ${deposit.appRefAndPipelineNum!}
                </dd>
            </div>
        </#if>
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
            <dd class="govuk-summary-list__value"> ${deposit.contingencyAmount!}</dd>
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
        <#if deposit.footnote?has_content>
            <div class="govuk-summary-list__row">
                <dt class="govuk-summary-list__key">Any other information</dt>
                <dd class="govuk-summary-list__value">
                    ${deposit.footnote}
                </dd>
            </div>
        </#if>
    </dl>
</#macro>
