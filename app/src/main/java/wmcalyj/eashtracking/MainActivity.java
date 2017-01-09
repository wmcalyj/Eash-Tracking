package wmcalyj.eashtracking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import wmcalyj.eashtracking.service.FileService;
import wmcalyj.eashtracking.service.RequestService;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private static final String MyTag = "MainActivityTag";
    private Button check, cancel, clear;
    private EditText editText;
    private Context mContext = this;
    private ListView listView;
    private Spinner carrierSpinner;
    private RelativeLayout searchHistoryList;
    private ArrayAdapter listViewArrayAdapter;
    private CircularProgressBar progressBar;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean succeed = intent.getBooleanExtra(Constants.REQUEST_RESULT_BOOLEAN, false);
            if (succeed) {
                updateRecentlySearchedHistory();
                clearEditText();
                clearCarrierSpinner();
                dismissProgressBar();
            } else {
                dismissProgressBar();
                Toast.makeText(mContext, Constants.FAIL_TO_LOAD_WEB_PAGE, Toast.LENGTH_LONG).show();
            }
        }
    };

    private void clearCarrierSpinner() {
        if (carrierSpinner == null) {
            carrierSpinner = (Spinner) findViewById(R.id.carrier_spinner);
        }
        carrierSpinner.setSelection(0);
    }

    private void clearEditText() {
        if (editText == null) {
            editText = (EditText) findViewById(R.id.tracking_number);

        }
        editText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        progressBar = (CircularProgressBar) findViewById(R.id.loading_progress_bar);
        editText = ((EditText) findViewById(R.id.tracking_number));
        check = (Button) findViewById(R.id.check_button);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trackingNumber = editText.getText()
                        .toString();
                if (StringUtil.isBlank(trackingNumber)) {
                    Toast.makeText(mContext, Constants.TRACKING_NUMBER_EMPTY_WARNING, Toast
                            .LENGTH_LONG).show();
                } else {
                    displayProgressBar();
                    String carrier = ((Spinner) findViewById(R.id.carrier_spinner))
                            .getSelectedItem()
                            .toString();

                    RequestService.getInstance().makeRequest(trackingNumber, carrier, mContext);
                }
            }
        });
        cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEditText();
                clearCarrierSpinner();
                RequestService.getInstance().cancelRequest();
            }
        });
        clear = (Button) findViewById(R.id.clear_history_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAlertDialogForDeletingAllHistory();
            }
        });
        listView = (ListView) findViewById(R.id.recent_search_list_view);
        registerForContextMenu(listView);


    }

    private void buildAlertDialogForDeletingAllHistory() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);

        // set title
        alertDialogBuilder.setTitle("Delete All History Records");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to delete all history records?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RecentlySearchedTrackingNumber.getInstance().clearAllHistoryRecords();
                        updateRecentlySearchedHistory();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter(Constants.REQUEST_RESULT_BROADCAST_MESSAGE));
        RecentlySearchedTrackingNumber.loadInstance(this);
        updateRecentlySearchedHistory();
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        FileService.getInstance().saveRecentlySearchedTrackingNumbers(this);
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo
            contextMenuInfo) {
        super.onCreateContextMenu(contextMenu, v, contextMenuInfo);
        contextMenu.setHeaderTitle(Constants.MORE_ACTION_TITLE);
        setContextMenuDetails(contextMenu, v, contextMenuInfo);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String itemTitle = item.getTitle().toString();
        if (itemTitle.equals(Constants.MoreActions.ADD_COMMENT)) {
            displayCommentDialog(item);
        } else if (itemTitle.equals(Constants.MoreActions.VIEW_EDIT_COMMENT)) {
            displayCommentDialogForEdit(item);
        } else if (itemTitle.equals(Constants.MoreActions.DELETE_COMMENT)) {
            deleteComment(item);
        } else if (itemTitle.equals(Constants.MoreActions.DELETE)) {
            // Delete
            deleteSelectedRecord(item);
        }


        return true;
    }

    private void deleteComment(MenuItem item) {
        String trackingId = getTrackingNumberIdFromMenuItem(item);
        buildAlertDialogForDeletingComment(trackingId);
    }

    private void buildAlertDialogForDeletingComment(final String trackingId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);

        // set title
        alertDialogBuilder.setTitle("Delete comment");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to delete this comment?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RecentlySearchedTrackingNumber.getInstance().deleteCommentForId(trackingId);
                        Toast.makeText(mContext, "Comment deleted.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void displayCommentDialogForEdit(MenuItem item) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.add_edit_comment_dialog);
        dialog.setTitle("View/Edit Comment");
        String trackingId = getTrackingNumberIdFromMenuItem(item);
        String comment = RecentlySearchedTrackingNumber.getInstance().getComment(trackingId);
        if (!StringUtil.isBlank(comment)) {
            EditText body = (EditText) dialog.findViewById(R.id.addEditCommentBody);
            body.setText(comment);
        }
        setAddEditCommentOKButton(dialog, trackingId);
        setAddEditCommentCancelButton(dialog);
        dialog.show();
    }

    private void displayCommentDialog(MenuItem item) {
        // custom dialog
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.add_edit_comment_dialog);
        dialog.setTitle("Add/Edit Comment");
        String trackingId = getTrackingNumberIdFromMenuItem(item);
        setAddEditCommentOKButton(dialog, trackingId);
        setAddEditCommentCancelButton(dialog);
        dialog.show();

    }

    private void setAddEditCommentCancelButton(final Dialog dialog) {
        Button cancel = (Button) dialog.findViewById(R.id.addEditCommentCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void setAddEditCommentOKButton(final Dialog dialog, final String trackingId) {
        Button ok = (Button) dialog.findViewById(R.id.addEditCommentOK);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText commentView = (EditText) dialog.findViewById(R.id.addEditCommentBody);
                String comment = commentView.getText().toString();
                RecentlySearchedTrackingNumber.getInstance().saveComment(trackingId, comment);
                dialog.dismiss();
                Toast.makeText(mContext, "Comment \"" + comment + "\" saved", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void deleteSelectedRecord(MenuItem item) {
        String trackingNumberId = getTrackingNumberIdFromMenuItem(item);
        buildAlertDialogForDeletingRecord(trackingNumberId, item);
    }

    private void buildAlertDialogForDeletingRecord(final String trackingNumberId, final MenuItem
            item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);

        // set title
        alertDialogBuilder.setTitle("Delete comment");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are you sure you want to delete this record?\n" + trackingNumberId)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (StringUtil.isBlank(trackingNumberId)) {
                            Toast.makeText(mContext, "Cannot find tracking number for this record.",
                                    Toast.LENGTH_LONG);
                            return;
                        } else {
                            RecentlySearchedTrackingNumber.getInstance().removeTrackingNumberById
                                    (trackingNumberId);
                            updateRecentlySearchedHistory();
//                            if (listViewArrayAdapter != null) {
//                                Object toRemove = listViewArrayAdapter.getItem(((AdapterView
//                                        .AdapterContextMenuInfo)
//                                        item.getMenuInfo()).position);
//                                if (toRemove != null) {
//                                    listViewArrayAdapter.remove(toRemove);
//                                } else {
//                                    Log.e(MyTag, "Item to be removed is not found");
//                                }
//                            } else {
//                                Log.e(MyTag, "List View Array Adapter is null");
//                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private String getTrackingNumberIdFromMenuItem(MenuItem item) {
        if (item == null || item.getMenuInfo() == null) {
            return null;
        }
        return getTrackingNumberIdFromContextMenuInfo(item.getMenuInfo());
    }

    private void setContextMenuDetails(ContextMenu contextMenu, View v, ContextMenu
            .ContextMenuInfo contextMenuInfo) {
        String trackingId = getTrackingNumberIdFromContextMenuInfo(contextMenuInfo);
        if (!RecentlySearchedTrackingNumber.getInstance().hasCommentForId(trackingId)) {
            contextMenu.add(0, v.getId(), 0, Constants.MoreActions.ADD_COMMENT);
        } else {
            contextMenu.add(0, v.getId(), 0, Constants.MoreActions.VIEW_EDIT_COMMENT);
            contextMenu.add(0, v.getId(), 0, Constants.MoreActions.DELETE_COMMENT);
        }
        contextMenu.add(0, v.getId(), 0, Constants.MoreActions.DELETE);//groupId, itemId, order,
        // title

    }

    private String getTrackingNumberIdFromContextMenuInfo(ContextMenu.ContextMenuInfo
                                                                  contextMenuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
                contextMenuInfo;
        if (info.targetView == null) {
            return null;
        }
        String id = ((TextView) info.targetView).getText().toString();
        return id;
    }

    private void updateRecentlySearchedHistory() {
        TrackingNumber header = RecentlySearchedTrackingNumber.getInstance().getHeader();
        if (header == null) {
            hideClearButton();
        } else {
            displayClearButton();
        }
        List<String> records = processHistoryRecord(header);
        listViewArrayAdapter = new ArrayAdapter<>(mContext,
                R.layout.history_view, records);
        listView.setAdapter(listViewArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayProgressBar();
                TextView record = (TextView) view;
                String trackingNumberId = record.getText().toString();
                TrackingNumber current = RecentlySearchedTrackingNumber.getInstance()
                        .getTrackingNumber
                                (trackingNumberId);
                RequestService.getInstance().makeRequest(current.trackingNumber, current.carrier,
                        mContext);
            }
        });
    }

    private void hideClearButton() {
        if (clear == null) {
            clear = (Button) findViewById(R.id.clear_history_button);
        }
        clear.setVisibility(GONE);
    }

    private void displayClearButton() {
        if (clear == null) {
            clear = (Button) findViewById(R.id.clear_history_button);
        }
        clear.setVisibility(View.VISIBLE);
    }

    private List<String> processHistoryRecord(final TrackingNumber header) {
        TrackingNumber tmp = header;
        List<String> records = new ArrayList<>();
        while (tmp != null) {
            records.add(tmp.id);
            tmp = tmp.next;
        }
        return records;
    }

    private void displayProgressBar() {
        if (progressBar == null) {
            progressBar = (CircularProgressBar) findViewById(R.id.loading_progress_bar);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    private void dismissProgressBar() {
        if (progressBar == null) {
            progressBar = (CircularProgressBar) findViewById(R.id.loading_progress_bar);
        }
        progressBar.setVisibility(GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.option_menu_help) {
            createHelpDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void createHelpDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.help_dialog);
        dialog.setTitle(Constants.OPTION_MENU_HELP_TITLE);
        setOptionMenuOkButton(dialog);
        dialog.show();


    }

    public void setOptionMenuOkButton(final Dialog dialog) {
        Button ok = (Button) dialog.findViewById(R.id.help_dialog_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
