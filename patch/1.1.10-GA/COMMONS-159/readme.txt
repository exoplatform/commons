COMMONS-159: [Mail] Cannot save file and Unknown error when saving file to folder "Personal Documents" on server

Problem description
* What is the problem to fix?
Bug reported: Cannot save file and Unknown error when saving file to folder "Personal Documents" on server
General bugs found:
Cannot save file and throw unknown error when saving file to "Drive" folders (not only Personal Documents)
Save file in wrong location when change current folder by breadcrumb link.

Fix description
* Problem analysis
Set selected folder at false moment.
JCR-path value is not generated in case folder of type "Drive".

* How is the problem fixed?
Set selected folder at render time.
Generate JCR path for Drive folder.
All modifications are done at java-script level.

Patch file: PROD-ID.patch

Tests to perform
* Reproduction test
- Open Mail application
- Configure a gmail account
- Open a message with attached file
- Click on button "Save to server" and select folder "Personal Documents"
Result: On application, message "unknown error" is displayed.
Note: This issue occurs only on Public drive, it does not occurs on other sub folder created in Public drive

Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
...
Changes in Selenium scripts 
...

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:


Configuration changes
Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
...

Validation (PM/Support/QA)
PM Comment
...
Support Comment
...
QA Feedbacks
...
