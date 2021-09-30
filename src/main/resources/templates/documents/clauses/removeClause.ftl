<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="sectionClauseView" type="uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Remove clause" pageHeading="Remove clause" topNavigation=true twoThirdsColumn=true breadcrumbs=true>

    <@fdsForm.htmlForm>

        <@fdsCheckAnswers.checkAnswers>

            <@fdsCheckAnswers.checkAnswersRow keyText="Clause name" actionUrl="" screenReaderActionText="" actionText="">
                ${sectionClauseView.name}
            </@fdsCheckAnswers.checkAnswersRow>

            <@fdsCheckAnswers.checkAnswersRow keyText="Clause text" actionUrl="" screenReaderActionText="" actionText="">
                ${sectionClauseView.text!}
            </@fdsCheckAnswers.checkAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

        <@fdsAction.submitButtons primaryButtonText="Remove clause" linkSecondaryAction=true secondaryLinkText="Go back" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>


</@defaultPage>