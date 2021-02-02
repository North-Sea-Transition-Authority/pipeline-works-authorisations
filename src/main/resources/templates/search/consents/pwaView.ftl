<#include '../../layout.ftl'>
<#import 'consentSearchTopLevelView.ftl' as consentSearchTopLevelView>

<#-- @ftlvariable name="consentSearchResultView" type="uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView" -->

<@defaultPage htmlTitle="View PWA" fullWidthColumn=true topNavigation=true wrapperWidth=true>

    <h3 class="govuk-heading-s"> View PWA </h3>
    <h1 class="govuk-heading-xl">${consentSearchResultView.latestConsentReference}</h1>

    <@consentSearchTopLevelView.topLevelData consentSearchResultView/>



</@defaultPage>