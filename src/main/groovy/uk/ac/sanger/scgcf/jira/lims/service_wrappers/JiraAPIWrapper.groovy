package uk.ac.sanger.scgcf.jira.lims.service_wrappers

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.util.WarningCollection
import groovy.util.logging.Slf4j
import uk.ac.sanger.scgcf.jira.lims.configurations.ConfigReader
import uk.ac.sanger.scgcf.jira.lims.utils.WorkflowUtils

/**
 * This class handles interactions with the Jira API
 *
 * Created by as28 on 23/06/16.
 */

@Slf4j(value = "LOG")
class JiraAPIWrapper {

    static CustomFieldManager customFieldManager

    static CustomFieldManager getCustomFieldManager() {
        if (customFieldManager == null) {
            customFieldManager = ComponentAccessor.getCustomFieldManager()
        }
        customFieldManager
    }

    //TODO: move all functions to WorkflowUtils
    /**
     * Get a custom field object from its name
     *
     * @param cfName
     * @return CustomField object
     */
    static CustomField getCFByName(String cfName) {
        LOG.debug "Get custom field for name: ${cfName}"
        // assumption here that custom field name is unique
        def customFields = getCustomFieldManager().getCustomFieldObjectsByName(cfName)
        if (customFields == null) {
            LOG.debug "No custom fields found with name: ${cfName}"
            return null
        }
        // assume first entry is correct, should only be one
        customFields[0]
    }

    /**
     * Get the value of a specified custom field name for an issue
     * N.B. accepts the real name not an alias
     *
     * @param curIssue
     * @param cfName
     * @return String value of custom field
     * TODO: make wrapper method for this method that takes the alias name for CF
     * TODO: split out to handle custom fields other than simple strings
     * TODO: handle various exceptions and fail silently e.g. if cfName not recognised get null pointer
     */
    static String getCFValueByName(Issue curIssue, String cfName) {
        LOG.debug "Attempting to get custom field value for issue with Id = <${curIssue.getId().toString()}> and cfname: ${cfName}"
        CustomField cf = getCFByName(cfName)
        String cfValue = null
        if(cf != null) {
            cfValue = curIssue.getCustomFieldValue(cf) as String
            LOG.debug "Custom field value = ${cfValue}"
        } else {
            LOG.error "Custom field null for issue with Id = <${curIssue.getId().toString()}> and cfname = <${cfName}>, cannot return value"
        }
        cfValue
    }

//    /**
//     * Useful method for retrieving multi-select custom field value as List<String>
//     */
//    @Nonnull
//    public List<String> getMultiSelectFieldValue(@Nonnull String fieldName, @Nonnull Issue issue) {
//        Validate.notNull(fieldName);
//        Validate.notNull(issue);
//        final CustomField customField = getCFByName(fieldName);
//
//        // Let's use the Option interface here as well instead of a specific implementation
//        @SuppressWarnings("unchecked")
//        final List<Option> value = (List<Option>) issue.getCustomFieldValue(customField);
//        // Handle NullPointerException
//        if (value == null) {
//            LOG.debug(
//                    "No value assigned to custom field '{}' on issue {}. Returning empty list.",
//                    customField, issue.getKey()
//            );
////            return Lists.newArrayList();
//            return new ArrayList<>()
//        }
//        // Handle non-list return values
//        if (!(value instanceof List)) {
//            LOG.debug(
//                    "Value of custom field '{}' on issue {} was not a List. Returning empty list.",
//                    customField, issue.getKey()
//            );
////            return Lists.newArrayList();
//            return new ArrayList<>()
//        }
//        // If it's empty, lets just return a new empty string list and forget about the origin type
//        if (value.isEmpty()) {
////            return Lists.newArrayList();
//            return new ArrayList<>()
//        }
//        // Handle potential ClassCastException for lists of any other kind, like Label
//        if (!(value.get(0) instanceof Option)) {
//            LOG.debug(
//                    "Value of custom field '{}' on issue {} was not a List<Option>. Returning empty list.",
//                    customField, issue.getKey()
//            );
////            return Lists.newArrayList();
//            return new ArrayList<>()
//        }
//        // Java 8
//        return value.stream()
//                .map(Option::getValue)
//                .collect(Collectors.toList());
//    }

    /**
     * Set the value of a specified custom field for an issue
     * NB. A CUSTOM FIELD CAN ONLY BE SET IF THE FIELD IS IN THE EDIT VIEW OF THE ISSUE WORKFLOW!
     * e.g. to set MyCustomField on a plate issue it must be listed in the plate Edit screen
     *
     * @param curIssue
     * @param cfName
     * @param newValue
     * TODO: this needs to handle custom fields other than strings
     * TODO: could this input just an Issue Id?
     */
    static void setCFValueByName(Issue curIssue, String cfName, String newValue) {
        LOG.debug "Attempting to set customfield value by name"
        LOG.debug "Issue Id: ${curIssue.getId()}"
        LOG.debug "Custom field name: ${cfName}"
        LOG.debug "New value: ${newValue}"

        IssueService issueService = ComponentAccessor.getIssueService()

        // locate the custom field for the current issue from its name
        CustomField tgtField = getCustomFieldManager().getCustomFieldObjects(curIssue).find { it.name == cfName }
        if (tgtField == null) {
            LOG.error "Custom field with name <${cfName}> was not found, cannot set value"
            //TODO: what error handling is required here? maybe should not fail silently?
            return
        }
        LOG.debug "Custom field instance ID : ${tgtField.getId()}"

        // update the value of the field and save the change in the database
        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.addCustomFieldValue(tgtField.getId(), newValue)

        IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(WorkflowUtils.getLoggedInUser(), curIssue.getId(), issueInputParameters)

        if (updateValidationResult.isValid()) {
            LOG.debug "Issue update validation passed, running update"
            IssueService.IssueResult updateResult = issueService.update(WorkflowUtils.getLoggedInUser(), updateValidationResult);
            if (updateResult.isValid()) {
                LOG.debug "Issue updated successfully"
                if(updateResult.hasWarnings()) {
                    LOG.debug "Warnings present"
                    WarningCollection warnings = updateResult.getWarningCollection()
                    if (warnings.hasAnyWarnings()) {
                        Collection<String> messages = warnings.getWarnings()
                        messages.each {String message ->
                            LOG.debug(message)
                        }
                    }
                }
            } else {
                LOG.error "Update failed! Custom field with name <${cfName}> could not be updated to value <${newValue}>"
                ErrorCollection errors=updateResult.getErrorCollection()
                if (errors.hasAnyErrors()) {
                    LOG.error "Errors present"
                    Map<String,String> messages=errors.getErrors()
                    for (Map.Entry<String,String> message : messages.entrySet()) {
                        LOG.error(message.getKey() + " : " + message.getValue())
                    }
                }
            }
        } else {
            LOG.error "Update validation failed! custom field with name <${cfName}> could not be updated to value <${newValue}>"
            ErrorCollection errors=updateValidationResult.getErrorCollection();
            if (errors.hasAnyErrors()) {
                LOG.error "Errors present"
                Map<String,String> messages=errors.getErrors();
                for (      Map.Entry<String,String> message : messages.entrySet()) {
                    LOG.error(message.getKey() + " : " + message.getValue());
                }
            }
        }
    }

    // TODO: amalgamate setCF methods and move to WorkflowUtils
    /**
     * Set the value of a select custom field given the name of the option
     *
     * @param curIssue
     * @param cfName
     * @param sOptionName
     */
    static void setCFSelectValueByName(Issue curIssue, String cfName, String sOptionName) {

        LOG.debug "Attempting to set select type customfield value by option name"
        LOG.debug "Issue Id: ${curIssue.getId()}"
        LOG.debug "Custom field name: ${cfName}"
        LOG.debug "Option name: ${sOptionName}"

        IssueService issueService = ComponentAccessor.getIssueService()

        // locate the custom field for the current issue from its name
        CustomField cf = getCustomFieldManager().getCustomFieldObjects(curIssue).find { it.name == cfName }
        if (cf == null) {
            LOG.error "Custom field with name <${cfName}> was not found, cannot set option"
            //TODO: what error handling is required here? maybe should not fail silently?
            return
        }
        LOG.debug "Custom field instance ID : ${cf.getId()}"

        FieldConfig fieldConfig = cf.getRelevantConfig(curIssue)
        Option option = ComponentAccessor.optionsManager.getOptions(fieldConfig)?.find { it.toString() == sOptionName }

        // update the value of the field and save the change in the database
        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.addCustomFieldValue(cf.getId(), option.getOptionId().toString())

        IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(WorkflowUtils.getLoggedInUser(), curIssue.getId(), issueInputParameters)

        if (updateValidationResult.isValid()) {
            LOG.debug "Issue update validation passed, running update"
            IssueService.IssueResult updateResult = issueService.update(WorkflowUtils.getLoggedInUser(), updateValidationResult);
            if (updateResult.isValid()) {
                LOG.debug "Issue updated successfully"
                if(updateResult.hasWarnings()) {
                    LOG.debug "Warnings present"
                    WarningCollection warnings = updateResult.getWarningCollection()
                    if (warnings.hasAnyWarnings()) {
                        Collection<String> messages = warnings.getWarnings()
                        messages.each {String message ->
                            LOG.debug(message)
                        }
                    }
                }
            } else {
                LOG.error "Update failed! Custom field with name <${cfName}> could not be updated to option <${sOptionName}>"
                ErrorCollection errors=updateResult.getErrorCollection()
                if (errors.hasAnyErrors()) {
                    LOG.error "Errors present"
                    Map<String,String> messages=errors.getErrors()
                    for (Map.Entry<String,String> message : messages.entrySet()) {
                        LOG.error(message.getKey() + " : " + message.getValue())
                    }
                }
            }
        } else {
            LOG.error "Update validation failed! custom field with name <${cfName}> could not be updated to option <${sOptionName}>"
            ErrorCollection errors=updateValidationResult.getErrorCollection();
            if (errors.hasAnyErrors()) {
                LOG.error "Errors present"
                Map<String,String> messages=errors.getErrors();
                for (      Map.Entry<String,String> message : messages.entrySet()) {
                    LOG.error(message.getKey() + " : " + message.getValue());
                }
            }
        }
    }

    /**
     * Clear the value of a specified custom field for an issue
     * NB. Better to use Clear field post function.
     *
     * @param cfName
     * TODO: this needs to handle custom fields other than strings
     * TODO: should this input a MutableIssue?
     */
    static void clearCFValueByName(Issue curIssue, String cfName) {
        setCFValueByName(curIssue, cfName, "")
    }

    /**
     * Get the id of a specified custom field for an issue
     *
     * @param cfName name of the custom field
     * @return String id of custom field
     */
    static String getCFIDByName(String cfName) {
        LOG.debug "Get custom field Id for name: ${cfName}"
        String cfID = getCFByName(cfName).id
        LOG.debug("CF idString: ${cfID}")
        cfID
    }

    /**
     * Get the id of a specified custom field for an issue using alias name
     *
     * @param aliasName alias name of the custom field
     * @return String id of custom field
     */
    static String getCFIDByAliasName(String aliasName) {
        LOG.debug "Get custom field Id for Alias name: ${aliasName}"
        String cfID = getCFIDByName(ConfigReader.getCFName(aliasName))
        cfID
    }

}