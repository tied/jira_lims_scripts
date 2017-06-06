package uk.ac.sanger.scgcf.jira.lims.enums

/**
 * Enumerated list for the name of transitions.
 * 
 * Created by ke4 on 25/01/2017.
 */
enum TransitionName {

    SS2_REVERT_TO_WITH_CUSTOMER("REVERT_TO_WITH_CUSTOMER"),
    SS2_REVERT_TO_READY_FOR_SS2("REVERT_TO_READY_FOR_SS2"),
    SS2_REVERT_TO_READY_FOR_SUBMISSION("REVERT_TO_READY_FOR_SUBMISSION"),
    SS2_REVERT_TO_READY_FOR_RECEIVING("REVERT_TO_READY_FOR_RECEIVING"),
    SS2_FAIL_ALL_PLATES_IN_GROUP("FAIL_ALL_PLATES_IN_GROUP"),
    SS2_AWAITING_SS2_FEEDBACK("AWAITING_SS2_FEEDBACK"),
    SS2_START_SUBMISSION("START_SUBMISSION"),
    SS2_START_IMPORT_DECLARATION("START_IMPORT_DECLARATION"),
    SS2_FAIL_IN_SPRI_96("FAIL_IN_SPRI_96"),
    SS2_FAIL_IN_SPRI_384("FAIL_IN_SPRI_384"),
    SS2_RE_RUN_REQUESTED_BY_SPRI("RE-RUN_REQUESTED_BY_SPRI"),
    SS2_COMPLETE_SPRI("COMPLETE_SPRI"),
    SS2_FAIL_IN_QUANT("FAIL_IN_QUANT"),
    SS2_RE_RUN_REQUESTED_BY_QUANT("RE_RUN_REQUESTED_BY_QUANT"),
    DNA_FAIL_IN_SPRI_96("FAIL_IN_SPRI_96"),
    DNA_FAIL_IN_SPRI_384("FAIL_IN_SPRI_384"),
    DNA_RE_RUN_REQUESTED_BY_SPRI("RE-RUN_REQUESTED_BY_SPRI"),
    DNA_COMPLETE_SPRI("COMPLETE_SPRI"),
    DNA_FAIL_IN_QUANT("FAIL_IN_QUANT"),
    DNA_RE_RUN_REQUESTED_BY_QUANT("RE_RUN_REQUESTED_BY_QUANT"),
    CMB_FAIL_IN_SPRI("FAIL_IN_SPRI"),
    CMB_COMPLETE_SPRI("COMPLETE_SPRI"),
    ECH_FAIL_IN_SPRI("FAIL_IN_SPRI"),
    ECH_READY_FOR_QUANTIFICATION("READY_FOR_QUANTIFICATION"),
    ECH_START_QUANTIFICATION_ANALYSIS("START_QUANTIFICATION_ANALYSIS"),
    ECH_READY_FOR_NORMALISATION_AND_LIBRARY_PREP("READY_FOR_NORMALISATION_AND_LIBRARY_PREP"),
    ECH_FAIL_IN_QUANTIFICATION_ANALYSIS("FAIL_IN_QUANTIFICATION_ANALYSIS"),
    ECH_FAIL_AND_REQUEST_QUANT_FEEDBACK("FAIL_AND_REQUEST_QUANT_FEEDBACK"),
    ECH_FAIL_OUTRIGHT_AND_REQUEST_QUANT_FEEDBACK("FAIL_OUTRIGHT_AND_REQUEST_QUANT_FEEDBACK"),
    SPRI_ALL_PLATES_RESOLVED("ALL_PLATES_RESOLVED")

    String transitionName

    public TransitionName(String transitionName) {
        this.transitionName = transitionName
    }

    @Override
    String toString() {
        transitionName
    }
}
