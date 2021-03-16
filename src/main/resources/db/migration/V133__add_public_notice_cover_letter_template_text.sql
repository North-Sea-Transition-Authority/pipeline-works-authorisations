UPDATE ${datasource.user}.template_text
SET text =
'In order to assist [INSERT COMPANY NAME] (“[INSERT FIRST PART OF COMPANY NAME]”) meet its operational timeframes, the Oil and Gas Authority (“OGA”) is willing to permit [INSERT FIRST PART OF COMPANY NAME] to publish, ' ||
'at its own risk, the public notice without [INSERT REASON AS TO WHY THIS IS OUTWITH STANDARD PROCESS]. ' ||
'The OGA treats each case individually and our flexibility on the timing of permitting the public notice does not set a precedent for future projects. ' ||
'For the avoidance of doubt, consent to the development of the [INSERT FIELD NAME] remains conditional upon [INSERT WHAT IS REQUIRED BEFORE A CONSENT CAN BE CONSIDERED] and satisfactory conclusion of the public notice process which, inter alia, is a precursor to a PWA.

Pursuant to paragraphs 2 and 3 of Schedule 2 to the Act, the Oil and Gas Authority has decided that the application is to be considered further and the Oil and Gas Authority accordingly directs you to publish in the ' ||
'[London Gazette and the Daily Telegraph OR Edinburgh Gazette and Aberdeen Press and Journal] a notice and schedule in the form set out attached to this public notice (Annex A & B), or in a form substantially to that effect. ' ||
'The Oil and Gas Authority further directs that the same notice, schedule and technical annex as enclosed (Annex A-C), be served on persons and bodies in the accompanying list (Annex D).

A copy of the notice, together with the technical annex (Annex A-C), which is in the form of a short statement agreed by the Health and Safety Executive in respect of protection of the pipeline, ' ||
'(which may require trenching or burial), limits of deviation, safety devices, leak detection and action to be taken by the operator in the event of a pipe break, together with a map (Admiralty Chart extract) not less than 1:200,000, ' ||
'delineating the route of the pipeline giving the co-ordinates of the pipelines'' start and termination points, must be made available for inspection at each of the places mentioned in the schedule to the notice (Annex B) during the times mentioned therein.'

WHERE text_type = 'PUBLIC_NOTICE_COVER_LETTER';