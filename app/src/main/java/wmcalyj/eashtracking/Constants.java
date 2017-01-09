package wmcalyj.eashtracking;

import java.util.Arrays;
import java.util.List;

import static wmcalyj.eashtracking.Constants.MoreActions.ADD_COMMENT;
import static wmcalyj.eashtracking.Constants.MoreActions.DELETE;
import static wmcalyj.eashtracking.Constants.MoreActions.DELETE_COMMENT;
import static wmcalyj.eashtracking.Constants.MoreActions.DETAILS;
import static wmcalyj.eashtracking.Constants.MoreActions.VIEW_EDIT_COMMENT;

/**
 * Created by mengchaowang on 1/5/17.
 */

public class Constants {
    // For private file I/O
    public static final String FILE_NAME = "Easy_Tracking_History";


    public static final String UPS_CLASS = "dataTable";
    public static final String REQUEST_URL = "REQUEST_URL_FOR_TRACKING";

    public static final String REQUEST_RESULT_BROADCAST_MESSAGE =
            "REQUEST_RESULT_BROADCAST_MESSAGE";
    public static final String REQUEST_RESULT_BOOLEAN = "REQUEST_RESULT_BOOLEAN";
    public static final String MORE_ACTION_TITLE = "More Actions";


    public static String TRACKINGNUMBER_DELIMITER = "--";
    public static String DUMMY_TRACKINGNUMBER_NUMBER = "DUMMYNUMBER";
    public static String DUMMY_TRACKINGNUMBER_CARRIER = "DUMMYCARRIER";
    public static String FAIL_TO_LOAD_WEB_PAGE = "Failed to load web page, please check your " +
            "tracking number and carrier.";
    public static String TRACKING_NUMBER_EMPTY_WARNING = "Please enter tracking number before " +
            "checking";
    public static String OPTION_MENU_HELP_TITLE = "Help / Instructions";


    public static final List<String> getAllActions() {
        return Arrays.asList(ADD_COMMENT, VIEW_EDIT_COMMENT, DELETE_COMMENT,
                DETAILS, DELETE);
    }

    public class MoreActions {
        public static final String ADD_COMMENT = "Add Comment";
        public static final String VIEW_EDIT_COMMENT = "View/Edit Comment";
        public static final String DELETE_COMMENT = "Delete Comment";
        public static final String DETAILS = "Details";
        public static final String DELETE = "Delete this record";


    }
}
