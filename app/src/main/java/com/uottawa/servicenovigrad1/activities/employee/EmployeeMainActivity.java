package com.uottawa.servicenovigrad1.activities.employee;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.uottawa.servicenovigrad1.R;
import com.uottawa.servicenovigrad1.activities.auth.LoginActivity;
import com.uottawa.servicenovigrad1.activities.branch.BranchInfoFragment;
import com.uottawa.servicenovigrad1.activities.branch.BranchServiceRequestsFragment;
import com.uottawa.servicenovigrad1.branch.Branch;
import com.uottawa.servicenovigrad1.user.UserAccount;
import com.uottawa.servicenovigrad1.user.UserController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmployeeMainActivity extends AppCompatActivity {
    private FirebaseFirestore firestore;
    private CollectionReference branchesReference;

    private Branch branch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);
        getSupportActionBar().hide();

        //Set up firestore
        firestore = FirebaseFirestore.getInstance();
        branchesReference = firestore.collection("branches");

        //If there is data passed through to this activity
        if(getIntent().getExtras() != null) {
            branch = (Branch) getIntent().getSerializableExtra("branch");
            if(branch.getServices().size() == 0) {
                //Launch edit services page
                Intent intent = new Intent(EmployeeMainActivity.this, EmployeeEditActivity.class);
                startActivityForResult(intent, 0);
            }
            initializeFields();
        } else {
            //Launch edit services page
            Intent intent = new Intent(EmployeeMainActivity.this, EmployeeEditActivity.class);
            startActivityForResult(intent, 0);
        }

        TextView branchInfoTitle = findViewById(R.id.employee_info_title);
        branchInfoTitle.setVisibility(View.VISIBLE);
    }

    private void initializeFields() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        BranchInfoFragment infoFragment = BranchInfoFragment.newInstance(branch);
        ft.replace(R.id.employee_info_fragment_container, infoFragment);

        BranchServiceRequestsFragment serviceFragment = BranchServiceRequestsFragment.newInstance(branch.getId());
        ft.replace(R.id.service_requests_fragment_container, serviceFragment);

        ft.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        UserAccount account = UserController.getInstance().getUserAccount();

        branchesReference.document(account.getUID()).addSnapshotListener(EmployeeMainActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error == null) {
                    if(value.exists()) {
                        String id = value.getId();
                        String name = value.getString("name");
                        String address = value.getString("address");
                        String phoneNumber = value.getString("phoneNumber");
                        ArrayList<String> servicesIds = (ArrayList<String>) value.get("services");

                        ArrayList<String> openDays = (ArrayList<String>) value.get("openDays");
                        int openingHour = value.getLong("openingHour").intValue();
                        int openingMinute = value.getLong("openingMinute").intValue();
                        int closingHour = value.getLong("closingHour").intValue();
                        int closingMinute = value.getLong("closingMinute").intValue();
                        double rating = value.getDouble("rating");

                        //Initialize the branch object.
                        Branch b = new Branch(
                            id,
                            name,
                            address,
                            phoneNumber,
                            servicesIds,
                            openDays,
                            openingHour,
                            openingMinute,
                            closingHour,
                            closingMinute,
                            rating
                        );
                        branch = b;
                        initializeFields();
                    } else {

                    }
                } else {

                }
            }
        });
    }

    public void editBranch(View view) {
        //Set up intent
        Intent intent = new Intent(EmployeeMainActivity.this, EmployeeEditActivity.class);
        //Add service to intent data to be edited
        intent.putExtra("branch", branch);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            //Get the branch
            Branch branch = (Branch) data.getSerializableExtra("branch");
            //Create a map with the data to write to cloud firestore
            Map<String, Object> branchInfo = new HashMap<>();
            branchInfo.put("name", branch.getName());
            branchInfo.put("phoneNumber", branch.getPhoneNumber());
            branchInfo.put("address", branch.getAddress());
            branchInfo.put("services", branch.getServices());
            branchInfo.put("openDays", branch.getOpenDays());
            branchInfo.put("openingHour", branch.getOpeningHour());
            branchInfo.put("openingMinute", branch.getOpeningMinute());
            branchInfo.put("closingHour", branch.getClosingHour());
            branchInfo.put("closingMinute", branch.getClosingMinute());
            branchInfo.put("rating", branch.getRating());

            UserAccount account = UserController.getInstance().getUserAccount();

            branchesReference.document(account.getUID()).set(branchInfo);
        }
    }

    /**
     * Sign out of the app.
     * @param view The current view.
     */
    public void signOut(View view) {
        //Create new AlertDialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EmployeeMainActivity.this);
        alertDialogBuilder
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out?")
                .setCancelable(true)
                .setPositiveButton(
                        "YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Sign out of Firebase
                                UserController.getInstance().signOut();
                                //Navigate back to Login Page
                                Intent intent = new Intent(EmployeeMainActivity.this, LoginActivity.class);
                                //set the new task and clear flags, so that the user can't go back here
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton(
                        "NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Close the dialog without closing the activity
                                dialog.cancel();
                            }
                        }
                );
        //Show AlertDialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}