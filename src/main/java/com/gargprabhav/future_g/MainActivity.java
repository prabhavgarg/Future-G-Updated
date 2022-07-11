package com.gargprabhav.future_g;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gargprabhav.future_g.GetSetListView;
import com.gargprabhav.future_g.LoginActivity;
import com.gargprabhav.future_g.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressDialog progressDialog;
    private static String USER_ID;
    private final String CURRENT = "CURRENT", DONE = "DONE", FUTURE = "FUTURE";
    DatabaseReference databaseReference1, databaseReference2;
    private Menu menu;
    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private ListView mlistViewCurrent, mlistViewDone, mlistViewFuture;
    private ArrayList<GetSetListView> currentArrayList, doneArrayList, futureArrayList;
    private ListViewAdapter currentListViewAdapter, doneListViewAdapter, futureListViewAdapter;
    private TextView noRecordsFound;
    private FloatingActionButton floatingActionButton;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_current:
                    initializeListView(mlistViewCurrent, mlistViewDone, mlistViewFuture);
                    noRecordsFound.setText("Click On + To Add Record");
                    floatingActionButton.show();
                    return true;
                case R.id.navigation_done:
                    initializeListView(mlistViewDone, mlistViewFuture, mlistViewCurrent);
                    noRecordsFound.setText("No Records Available");
                    floatingActionButton.hide();
                    return true;
                case R.id.navigation_future:
                    initializeListView(mlistViewFuture, mlistViewCurrent, mlistViewDone);
                    noRecordsFound.setText("No Records Available");
                    floatingActionButton.hide();
                    return true;
            }
            return false;
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menuu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.app_details:
                showAppDetails();
                return true;
            case R.id.profile:
                usersProfile();
                return true;
            case R.id.rateUs:
                rateOurApp();
                return true;
            case R.id.help:
                helpUser();
                return true;
            case R.id.share:
                shareApp();
                return true;
            case R.id.logout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAppDetails() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("App Details:");
        String details="1. This App is used to store your daily To-Do list.\n2. Press on plus button to add your event.\n3. You can mark your event as completed or save it for future by long pressing on event.\n4. Thanks for Installing my App. :)";
        alertDialog.setMessage(details);
        alertDialog.setIcon(R.drawable.logo);
        alertDialog.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void shareApp() {
        String shareBody = "https://play.google.com/store/apps/details?id=com.gargprabhav.future_g";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "APP NAME (Open it in Google Play Store to Download the Application)");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void rateOurApp() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.gargprabhav.future_g"));
        startActivity(intent);
    }

    private void helpUser() {
        String mailto = "mailto:prabhav.garg.boss@gmail.com";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            //TODO: Handle case where no email app is available
        }
    }

    private void usersProfile() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = "";
        if (user != null) {
            // User is signed in
            email = user.getEmail();
        }
        alertDialog.setTitle("User Email Id:");
        alertDialog.setMessage(email);
        alertDialog.setIcon(R.drawable.user);
        alertDialog.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //color of top status bar
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.dark_red));

        setContentView(R.layout.activity_main);
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String email = "";
        if (user != null) {
            // User is signed in
            email = user.getEmail();
        }
        if (email != null) {
            email = email.replace(".", "_");
            email = email.replace("@", "__");
        }
        USER_ID = email;

        if (firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        initUI();

        floatingActionButton.setOnClickListener(this);
    }

    private void initUI() {
        mlistViewCurrent = (ListView) findViewById(R.id.listview_current);
        mlistViewDone = (ListView) findViewById(R.id.listview_done);
        mlistViewFuture = (ListView) findViewById(R.id.listview_future);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        initializeListView(mlistViewCurrent, mlistViewDone, mlistViewFuture);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        currentArrayList = new ArrayList<>();
        doneArrayList = new ArrayList<>();
        futureArrayList = new ArrayList<>();
        currentListViewAdapter = new ListViewAdapter(this, currentArrayList);
        doneListViewAdapter = new ListViewAdapter(this, doneArrayList);
        futureListViewAdapter = new ListViewAdapter(this, futureArrayList);
        noRecordsFound = (TextView) findViewById(R.id.noRecordsFound);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
    }

    private void longPressOfItems(final int position, final String name, final ArrayList<GetSetListView> arrayList, String first, final String second, final String third) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Take Action...");
        alertDialog.setMessage("Select your action...??");
        alertDialog.setIcon(R.drawable.ic_baseline_add_alert_24);

        alertDialog.setPositiveButton(first, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // mlistViewCurrent.
                databaseReference1 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(name);
                databaseReference1.child(arrayList.get(position).getId()).removeValue();
                arrayList.remove(position);
                Toast.makeText(getApplication(), "Successfully Deleted!!", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNegativeButton(second, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                databaseReference1 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(name);
                pushingDataToFirebase(second, arrayList.get(position).getDate(), arrayList.get(position).getStatus(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription(), arrayList.get(position).getImage(), 2);
                databaseReference1.child(arrayList.get(position).getId()).removeValue();
                arrayList.remove(position);
                Toast.makeText(getApplication(), "Shifted to " + second, Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNeutralButton(third, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                databaseReference1 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(name);
                pushingDataToFirebase(third, arrayList.get(position).getDate(), arrayList.get(position).getStatus(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription(), arrayList.get(position).getImage(), 2);
                databaseReference1.child(arrayList.get(position).getId()).removeValue();
                arrayList.remove(position);
                Toast.makeText(getApplication(), "Shifted to " + third, Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    private String addNewItem() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_details, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText dialogueTitle = (EditText) promptsView.findViewById(R.id.dialogue_title);
        final EditText dialogueDescription = (EditText) promptsView.findViewById(R.id.dialogue_description);
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                pushingDataToFirebase(CURRENT, getDate(), "To-Do Now", dialogueTitle.getText().toString().trim(), dialogueDescription.getText().toString().trim(), R.drawable.current_smile, 1);
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        return dialogueTitle.getText().toString().trim();
    }

    private String addNewItem(String title, String description, int position, ArrayList<GetSetListView> arrayList, String name, String status) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.add_details, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText dialogueTitle = (EditText) promptsView.findViewById(R.id.dialogue_title);
        final EditText dialogueDescription = (EditText) promptsView.findViewById(R.id.dialogue_description);

        //set default values
        dialogueTitle.setText(title);
        dialogueDescription.setText(description);
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                databaseReference2 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(name);
                databaseReference2.child(arrayList.get(position).getId()).removeValue();
                arrayList.remove(position);
                pushingDataToFirebase(name, getDate(), status, dialogueTitle.getText().toString().trim(),
                        dialogueDescription.getText().toString().trim(), R.drawable.current_smile, 1);
                Toast.makeText(getApplication(), "Edited", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
        return dialogueTitle.getText().toString().trim();
    }

    private void showDetails(int position, ArrayList<GetSetListView> arrayList) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.show_details, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        final TextView titleHeading = (TextView) promptsView.findViewById(R.id.dialogue_title_heading);
        final TextView dateDetails = (TextView) promptsView.findViewById(R.id.date_details);
        final TextView statusDetails = (TextView) promptsView.findViewById(R.id.status_details);
        final TextView detailDescriptions = (TextView) promptsView.findViewById(R.id.dialogue_description_details);
        if (arrayList.get(position).getTitle().trim().equals("")) {
            titleHeading.setText("Title");
        } else titleHeading.setText(arrayList.get(position).getTitle());
        dateDetails.setText(arrayList.get(position).getDate());
        if (arrayList.get(position).getStatus().trim().equals("")) {
            statusDetails.setText("Pending");
        } else statusDetails.setText(arrayList.get(position).getStatus());
        if (arrayList.get(position).getDescription().trim().equals("")) {
            detailDescriptions.setText("Work is pending...");
        } else detailDescriptions.setText(arrayList.get(position).getDescription());


        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).setNegativeButton("EDIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String name="";
                if(statusDetails.getText().equals("To-Do Now")){
                    name = CURRENT;
                }else if(statusDetails.getText().equals("Completed")){
                    name = DONE;
                }else{
                    name = FUTURE;
                }
                addNewItem(titleHeading.getText().toString(), detailDescriptions.getText().toString(),
                        position,arrayList,name, statusDetails.getText().toString());
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrievalOfData(CURRENT, mlistViewCurrent, currentArrayList, currentListViewAdapter, "DELETE", DONE, FUTURE);
        retrievalOfData(DONE, mlistViewDone, doneArrayList, doneListViewAdapter, "DELETE", CURRENT, FUTURE);
        retrievalOfData(FUTURE, mlistViewFuture, futureArrayList, futureListViewAdapter, "DELETE", DONE, CURRENT);
    }

    private void retrievalOfData(final String name, final ListView listview, final ArrayList<GetSetListView> arrayList, final ListViewAdapter adapter, final String first, final String second, final String third) {
        databaseReference1 = FirebaseDatabase.getInstance().getReference(MainActivity.USER_ID).child(name);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                progressDialog.dismiss();
                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting artist
                    GetSetListView getSetListView = postSnapshot.getValue(GetSetListView.class);
                    //adding artist to the list
                    arrayList.add(getSetListView);
                }

                Collections.reverse(arrayList);
                //creating adapter
                if (adapter == currentListViewAdapter) {
                    currentListViewAdapter = new ListViewAdapter(MainActivity.this, arrayList);
                    listview.setAdapter(currentListViewAdapter);
                } else if (adapter == doneListViewAdapter) {
                    doneListViewAdapter = new ListViewAdapter(MainActivity.this, arrayList);
                    listview.setAdapter(doneListViewAdapter);
                } else {
                    futureListViewAdapter = new ListViewAdapter(MainActivity.this, arrayList);
                    listview.setAdapter(futureListViewAdapter);
                }
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showDetails(position, arrayList);
                    }
                });
                listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        longPressOfItems(position, name, arrayList, first, second, third);
                        return true;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeListView(ListView visibleList, ListView invisibleList1, ListView invisibleList2) {
        visibleList.setVisibility(View.VISIBLE);
        invisibleList1.setVisibility(View.INVISIBLE);
        invisibleList2.setVisibility(View.INVISIBLE);
        if (menu != null) {
            MenuItem menuItem = menu.findItem(R.id.app_details);
            if (visibleList == mlistViewCurrent)
                menuItem.setVisible(true);
            else
                menuItem.setVisible(false);
        }
    }

    public String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void pushingDataToFirebase(String name, String dateString, String statusString, String titleString,
                                      String descriptionString, int image, int num) {
        databaseReference2 = FirebaseDatabase.getInstance().getReference();
        String id = databaseReference2.push().getKey();
        switch (name) {
            case "CURRENT":
                statusString = "To-Do Now";
                image = R.drawable.current_smile;
                break;
            case "DONE":
                statusString = "Completed";
                image = R.drawable.done_smile;
                break;
            case "FUTURE":
                statusString = "To-Do Later";
                image = R.drawable.future_simle;
                break;
        }
        GetSetListView getSetListView = new GetSetListView(titleString, dateString, descriptionString, statusString, id, image);
        databaseReference2.child(MainActivity.USER_ID).child(name).child(id).setValue(getSetListView);
        if (num == 1) Toast.makeText(this, "Event Saved Successfully", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if (view == floatingActionButton) {
            addNewItem();
        }
    }
}