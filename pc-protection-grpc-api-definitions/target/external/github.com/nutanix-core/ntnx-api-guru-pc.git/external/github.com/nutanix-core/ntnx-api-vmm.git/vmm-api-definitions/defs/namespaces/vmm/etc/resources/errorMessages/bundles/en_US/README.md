Namespace error bundles can be placed here in one or more YAML files.

Format for defining the app errors can be referred here: https://confluence.eng.nutanix.com:8443/pages/viewpage.action?spaceKey=AI&title=Error+Handling

App errors codes defined here fall in the following buckets (each block reserved
for the corresponding block):

- 1xxxx Generic VMM namespace errors
- 2xxxx block is reserved for catalog related errors (excluding VM templates)
- 3xxxx VM and sub-entities related errors

Within the 3xxxx band, there are multiple groups of errors/warnings logically bucketed into different types. Add new errors accordingly.

Few rules for the changes in the errors or warnings before asking for an approval from the VMM core team:
- Need an approval from the corresponding project's core team member. But ask for that only after the following points have been self-reviewed.
- Keep the error in the relevant file as they are bucketed currently. Add a new bucket with an available range if applicable.
- Make sure there are no duplicate errors present, this will avoid confusion for the end-user in the error documentation.
- Set the error group consciously, there is no clear guidance from API infra at this point (as of 10/23) but the idea is to bucket the errors/warnings logically (groups shouldn't be defined in either of these extreme ways - not too course grained or not too fine grained grouping). Firstly, errors need can be grouped based on the type of error it is (invalid argument error, already exists error, etc.), if not, consider the second option- if a bunch of errors are only specific to a workflow, you can have a workflow based grouping.
- Correctly represent the arguments for the error, argument names are important for the client to comprehend the semantic meaning of the message.
- Phrasing needs to be consistent with other errors we have already defined. If you are fixing any wording make sure to check for all the existing relevant errors that may be need to be fixed as well, if leading to backward incompatible changes refer to the next point.
- Backward incompatible changes are not allowed post the beta release of VMM APIs (pending guidance from API infra for deprecation of errors), need to discuss each situation case-by-case if needed. Start with #v4-vmm-internal or #ask-v4-vmm-eng slack channels.
  - Even from alpha to beta, make sure upgrade scenarios are reviewed if there is backward incompatible change.  
  - Backward incompatible change is not just limited to removing an argument or an existing part of an error definition, its also returning a new error for an existing API/workflow. All the clients need to made aware of the change in the error code for this existing workflow, so that they fix their programs. This is not a problem if the error code previously returned was internal error.
- Do not use the INTERNAL_ERROR group for any other error definition apart from error code 10000. If the reason is missing due to an unknown internal error, either use 10000 error code or format the error in such a way to avoid saying thats its an internal error while sharing as much as context that the user needs to know.
- Argument names need to make sense by themselves as much as possible, since the map is returned directly to the end-user, so think about it like an API object.
- Deprecating errors is as tedious as API objects especially now that the definitions are publicly available. So, each definition needs to be handled similar to how we handle API models.
- Potentially, we want to have an API-wise list of app error codes that can be returned to the end user in the documentation, for ex.: https://vdc-repo.vmware.com/vmwb-repository/dcr-public/5bc36046-6569-42b8-a60d-4b175d91fa9d/56a2807a-c2a0-4971-8cd8-ee5440e17b19/doc/vim.vm.Snapshot.html (Check the Faults tab for the list of errors that can be raised for the operation)
- We cannot have free form english strings in the arguments unless they are API specified attributes. This can lead to more verbose definitions but less ambiguity to the end user.
- Standard grammatical rules to be followed, for the English locale.
