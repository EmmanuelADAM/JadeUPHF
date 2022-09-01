package jade.domain.FIPAAgentManagement;

/**
 * This interface contains all the string constants for frame and slot
 * names of exceptions defined in the
 *  fipa-agent-management   ontology.
 */
public interface ExceptionVocabulary {

    /**
     * A symbolic constant, containing the name of this ontology.
     */
    String NAME = "Exception";

    // Not-understood Exception Predicates
    String UNSUPPORTEDACT = "unsupported-act";
    String UNSUPPORTEDACT_ACT = "act";

    String UNEXPECTEDACT = "unexpected-act";
    String UNEXPECTEDACT_ACT = "act";

    String UNSUPPORTEDVALUE = "unsupported-value";
    String UNSUPPORTEDVALUE_VALUE = "value";

    String UNRECOGNISEDVALUE = "unrecognised-value";
    String UNRECOGNISEDVALUE_VALUE = "value";

    // Refusal Exception Predicates
    String UNAUTHORISED = "unauthorised";

    String UNSUPPORTEDFUNCTION = "unsupported-function";
    String UNSUPPORTEDFUNCTION_FUNCTION = "function";

    String MISSINGARGUMENT = "missing-argument";
    String MISSINGARGUMENT_ARGUMENT = "argument-name";

    String UNEXPECTEDARGUMENT = "unexpected-argument";
    String UNEXPECTEDARGUMENT_ARGUMENT = "argument-name";

    String UNEXPECTEDARGUMENTCOUNT = "unexpected-argument-count";

    String MISSINGPARAMETER = "missing-parameter";
    String MISSINGPARAMETER_OBJECT_NAME = "object-name";
    String MISSINGPARAMETER_PARAMETER_NAME = "parameter-name";

    String UNEXPECTEDPARAMETER = "unexpected-parameter";
    String UNEXPECTEDPARAMETER_OBJECT_NAME = "object-name";
    String UNEXPECTEDPARAMETER_PARAMETER_NAME = "parameter-name";

    String UNRECOGNISEDPARAMETERVALUE = "unrecognised-parameter-value";
    String UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME = "parameter-name";
    String UNRECOGNISEDPARAMETERVALUE_PARAMETER_VALUE = "parameter-value";

    // Failure Exception Predicates
    String INTERNALERROR = "internal-error";
    String INTERNALERROR_MESSAGE = "error-message";

}
