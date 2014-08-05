package world.idea360.co.twittersearch;

import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


public class MyActivity extends ListActivity {
    //name of sharedPrefs XML file that stores the saved searches.
    private static final String SEARCHES = "searches";

    private EditText queryEditText;
    private EditText tagEditText;
    private SharedPreferences savedSearches;
    private ArrayList<String> tags;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //get query references
        queryEditText = (EditText) findViewById(R.id.querryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);

        //get shared prefs containing the user's searches.
        savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);

        //store the saved tags in an array list then sort them
        tags = new ArrayList<String>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        //Create an Array Adapter and use it to bind tags to list view
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, tags);
        setListAdapter(adapter);

        //register listener to save a new or edited search
        ImageButton saveButton =
                (ImageButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);

        //Register listener that searches Twitter when the users touches a tag
        getListView().setOnItemClickListener(itemClickListener);

        //Set listener that allows user to delete or edit a search!
        getListView().setOnItemLongClickListener(itemLongClickListener);
    }

    public OnClickListener saveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            //create tag is neither queryEditText nor tagEditText is empty.
            if(queryEditText.getText().length() > 0 &&
                    tagEditText.getText().length() > 0){

                addTaggedSearch(queryEditText.getText().toString(),
                        tagEditText.getText().toString());
                queryEditText.setText("");
                tagEditText.setText("");
                ((InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        tagEditText.getWindowToken(), 0);
            }
            //Display message asking user to provide a query tag
            else{
                //Create a new alert dialog user
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MyActivity.this);

                //Set the dialog's text
                builder.setMessage(R.string.missingMessage);

                //Provide OK button
                builder.setPositiveButton(R.string.OK, null);

                //Create the Alert
                AlertDialog errorDialog = builder.create();
                errorDialog.show();
            }
        }
    };

    private void addTaggedSearch(String query, String tag){
        //get shared prefs editor ro store data
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query);
        preferencesEditor.apply();

        //if tag is new, add to and sort tags, then display updated list
        if(!tags.contains(tag)){
            tags.add(tag);
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged();
        }
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {
       @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String tag = ((TextView) view).getText().toString();
            String urlString = getString(R.string.searchURL) +
                    Uri.encode(savedSearches.getString(tag,""), "UTF-8");

            //Create intent
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));

            startActivity(webIntent);
        }
    };

    OnItemLongClickListener itemLongClickListener =
            new OnItemLongClickListener()
            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id)
                {
                    // get the tag that the user long touched
                    final String tag = ((TextView) view).getText().toString();

                    // create a new AlertDialog
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(MyActivity.this);

                    // set the AlertDialog's title
                    builder.setTitle(
                            getString(R.string.shareEditDeleteTitle, tag));

                    // set list of items to display in dialog
                    builder.setItems(R.array.dialog_items,
                            new DialogInterface.OnClickListener()
                            {
                                // responds to user touch by sharing, editing or
                                // deleting a saved search
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    switch (which)
                                    {
                                        case 0: // share
                                            shareSearch(tag);
                                            break;
                                        case 1: // edit
                                            // set EditTexts to match chosen tag and query
                                            tagEditText.setText(tag);
                                            queryEditText.setText(
                                                    savedSearches.getString(tag, ""));
                                            break;
                                        case 2: // delete
                                            deleteSearch(tag);
                                            break;
                                    }
                                }
                            } // end DialogInterface.OnClickListener
                    ); // end call to builder.setItems

                    // set the AlertDialog's negative Button
                    builder.setNegativeButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener()
                            {
                                // called when the "Cancel" Button is clicked
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.cancel(); // dismiss the AlertDialog
                                }
                            }
                    ); // end call to setNegativeButton

                    builder.create().show(); // display the AlertDialog
                    return true;
                } // end method onItemLongClick
            }; // end OnItemLongClickListener declaration

    private void shareSearch(String tag)
    {
        // create the URL representing the search
        String urlString = getString(R.string.searchURL) +
                Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

        // create Intent to share urlString
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.sharedSubject));
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.shareMessage, urlString));
        shareIntent.setType("text/plain");

        // display apps that can share text
        startActivity(Intent.createChooser(shareIntent,
                getString(R.string.shareSearch)));
    }

    private void deleteSearch(final String tag)
    {
        // create a new AlertDialog
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);

        // set the AlertDialog's message
        confirmBuilder.setMessage(
                getString(R.string.confirmMessage, tag));

        // set the AlertDialog's negative Button
        confirmBuilder.setNegativeButton( getString(R.string.cancel),
                new DialogInterface.OnClickListener()
                {
                    // called when "Cancel" Button is clicked
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel(); // dismiss dialog
                    }
                }
        ); // end call to setNegativeButton

        // set the AlertDialog's positive Button
        confirmBuilder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener()
                {
                    // called when "Cancel" Button is clicked
                    public void onClick(DialogInterface dialog, int id)
                    {
                        tags.remove(tag); // remove tag from tags

                        // get SharedPreferences.Editor to remove saved search
                        SharedPreferences.Editor preferencesEditor =
                                savedSearches.edit();
                        preferencesEditor.remove(tag); // remove search
                        preferencesEditor.apply(); // saves the changes

                        // rebind tags ArrayList to ListView to show updated list
                        adapter.notifyDataSetChanged();
                    }
                } // end OnClickListener
        ); // end call to setPositiveButton

        confirmBuilder.create().show(); // display AlertDialog
    } // end method deleteSearch
}//end MyActivity