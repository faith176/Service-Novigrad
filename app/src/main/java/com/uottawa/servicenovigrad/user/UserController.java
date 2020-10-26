package com.uottawa.servicenovigrad.user;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uottawa.servicenovigrad.utils.Function;
import com.uottawa.servicenovigrad.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class UserController {
    private static UserController uInstance = null;

    private UserAccount userAccount;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    protected UserController() {
        this(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
    }

    protected UserController(FirebaseFirestore db, FirebaseAuth auth) {
        this.auth = auth;
        firestore = db;
    }

    public void signInAsAdmin(final View view, final Function onSuccess) {
        //Sign into firebase as an anonymous user, to meet firestore rules
        auth.signInAnonymously()
        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                setUserAccount(new AdminAccount(auth.getCurrentUser().getUid()));
                onSuccess.f();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            //Failed to get data from database
            @Override
            public void onFailure(@NonNull Exception e) {
                //Show failed error
                Utils.showSnackbar("Failed to sign in as admin.", view);
            }
        });
    }

    /**
     * Signs user in with given email and password. Input validation is not handled here.
     * Shows error message in a snackbar on the view if any.
     * @param email the email of the user to sign in.
     * @param password the password of the user to sign in.
     * @param view the current view, required to show the error message.
     * @param onSuccess the function to call after everything has succeeded.
     */
    public void signIn(
        final String email,
        String password,
        final View view,
        final Function onSuccess
    ) {
        //Sign into firebase
        auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            //Successful login attempt
            @Override
            public void onSuccess(AuthResult authResult) {
                //Retrieve user info from firestore
                firestore.collection("users")
                .document(auth.getCurrentUser().getUid()) //Gets the document with the user UID, where the data should be stored.
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //Successful data getting
                        //Gets the name and role from the document snapshot
                        String n = (String) documentSnapshot.getData().get("name");
                        String r = (String) documentSnapshot.getData().get("role");
                        switch(r) {
                            case "employee":
                                setUserAccount(new EmployeeAccount(n, email, auth.getCurrentUser().getUid()));
                                break;
                            case "customer":
                                setUserAccount(new CustomerAccount(n, email, auth.getCurrentUser().getUid()));
                                break;
                            default:
                                Utils.showSnackbar("Invalid data from database!", view);
                                break;
                        }
                        //Call the function to call after everything has succeeded.
                        //We pass through the login data to write to shared preferences.
                        onSuccess.f(n, email, r, auth.getCurrentUser().getUid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    //Failed to get data from database
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Show failed error
                        Utils.showSnackbar("Failed to get user details from database!", view);
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Failed login attempt
                try {
                    //Try to get firebase auth exception message
                    String errorMessage = "Log In Error: " + ((FirebaseAuthException) e).getErrorCode().replace("ERROR_", "");
                    //Show failed error
                    Utils.showSnackbar(errorMessage, view);
                } catch (Exception ex) {
                    //Unexpected error
                    Utils.showSnackbar("An unexpected error occurred.", view);
                }
            }
        });
    }

    /**
     * Signs user up with the given name, email, role, and password. Input validation is not handled here.
     * @param name the name of the user to sign up with
     * @param email the email of the user to sign up with
     * @param role the role of the user to sign up with
     * @param password the password of the user to sign up with
     * @param view the current view, required to show the error message.
     * @param onSuccess the function to call after everything has succeeded.
     */
    public void signUp(
        final String name,
        final String email,
        final String role,
        final String password,
        final View view,
        final Function onSuccess
    ) {
        // Create user on Firebase auth
        auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //Create a map with the data to write to cloud firestore
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("name", name);
                userInfo.put("email", email);
                userInfo.put("role", role);

                //Writes the data to firestore
                firestore.collection("users")
                .document(auth.getCurrentUser().getUid())
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    //Only when firestore succeeds in writing user data to database does the app log the user in.
                    @Override
                    public void onSuccess(Void aVoid) {
                        switch(role) {
                            case "employee":
                                setUserAccount(new EmployeeAccount(name, email, auth.getCurrentUser().getUid()));
                                break;
                            case "customer":
                                setUserAccount(new CustomerAccount(name, email, auth.getCurrentUser().getUid()));
                                break;
                            default:
                                //This really shouldn't happen
                                Utils.showSnackbar("Invalid role! This should never happen.", view);
                                break;
                        }
                        onSuccess.f(name, email, role, auth.getCurrentUser().getUid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Show failed error
                        Utils.showSnackbar("Failed to add user to database.", view);
                        //Tries to delete current user so that user can try to create new account again.
                        auth.getCurrentUser().delete();
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Show failed error
                Utils.showSnackbar("Failed to create user!", view);
            }
        });
    }

    public void signOut() {
        if(userAccount instanceof AdminAccount) {
            //Dispose of anonymous account
            auth.getCurrentUser().delete();
        } else {
            auth.signOut();
        }
        userAccount = null;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    private void setUserAccount(UserAccount account) {
        this.userAccount = account;
    }

    public static synchronized void initialize(UserAccount account) {
        uInstance = new UserController();
        uInstance.setUserAccount(account);
    }

    public static synchronized void initialize(UserAccount account, FirebaseFirestore db, FirebaseAuth auth) {
        uInstance = new UserController(db, auth);
        uInstance.setUserAccount(account);
    }

    public static synchronized UserController getInstance() {
        if (uInstance == null) {
            uInstance = new UserController();
        }
        return uInstance;
    }
}
