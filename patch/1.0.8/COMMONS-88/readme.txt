Summary

    * Status: Implement DataInjectorService
    * CCP Issue: Product Jira Issue: COMMON-88.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Implement DataInjectorService. This will be used for CS/KS data injectors.

Fix description

How is the problem fixed?

    * Implement an abstract class DataInjector to handle inject/reject data
    * Implement DataInjectorService which provides RESTful services:
          o execute(HashMap<String , String> params): to execute tasks that require to response data to client.
          o inject(HashMap<String , String> params): to inject data into the product.
          o reject(HashMap<String , String> params): to clear data that were injected before.

Developers who want to inject data for a specified product will implement a class which extends DataInjector and registers the class to DataInjectorService as a plugin.

Patch file: COMMONS-88.patch

Tests to perform

Reproduction test

    * N/A

Tests performed at DevLevel
*

Tests performed at QA/Support Level

    *

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

Support Comment

    * Support review: Patch validated

QA Feedbacks

    *
