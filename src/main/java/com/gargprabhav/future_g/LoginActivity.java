package com.gargprabhav.future_g;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //defining view objects
    private TextView textView;
    private EditText editTextEmail;
    private EditText editTextPassword, editTextRePassword;
    private Button buttonSignup;
    private TextView textViewSignin;
    private ProgressDialog progressDialog;
    private ImageView googleButton, phoneButton;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    //Google SignIn
    GoogleSignInClient mGoogleSignInClient;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int RC_SIGN_IN = 101;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //removing Title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        setContentView(R.layout.activity_login);
        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if getCurrentUser does not returns null
        if (firebaseAuth.getCurrentUser() != null) {
            //that means user is already logged in
            //so close this activity
            finish();

            //and open profile activity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        //initializing views
        textView = (TextView) findViewById(R.id.textView);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextRePassword = (EditText) findViewById(R.id.editTextRePassword);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);
        googleButton = (ImageView) findViewById(R.id.googleButton);
        //phoneButton = (ImageView) findViewById(R.id.phoneButton);

        buttonSignup = (Button) findViewById(R.id.buttonSignup);

        progressDialog = new ProgressDialog(this);

        //attaching listener to button
        buttonSignup.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);
        googleButton.setOnClickListener(this);
        //phoneButton.setOnClickListener(this);

    }

    //method for user login
    private void userLogin() {
        editTextRePassword.setVisibility(View.GONE);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(!email.matches(emailPattern)){
            editTextEmail.setError("Enter Correct Email");
            editTextEmail.requestFocus();
        }else if(password.isEmpty() || password.length() < 6){
            editTextPassword.setError("Enter Proper Password");
            editTextPassword.requestFocus();
        }

        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
//            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
//            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
//                    Toast.makeText(LoginActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
//                    Toast.makeText(LoginActivity.this, "Registration Error", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });


        progressDialog.setMessage("Logging In Please Wait...");
        progressDialog.setTitle("Loggin In");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        //if the task is successfull
                        if (task.isSuccessful()) {
                            //start the profile activity
                            Toast.makeText(LoginActivity.this, "Log-In Successful", Toast.LENGTH_LONG).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else {
                            Toast.makeText(LoginActivity.this, "Log-In Error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void registerUser() {
        editTextRePassword.setVisibility(View.VISIBLE);
        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String rePassword = editTextRePassword.getText().toString().trim();

        if(!email.matches(emailPattern)){
            editTextEmail.setError("Enter Correct Email");
            editTextEmail.requestFocus();
        }else if(password.isEmpty() || password.length() < 6){
            editTextPassword.setError("Enter Proper Password");
            editTextPassword.requestFocus();
        }else if(!password.equals(rePassword)){
            editTextRePassword.setError("Password Not Matching");
            editTextRePassword.requestFocus();
        }


        //checking if email and passwords are empty
        if (TextUtils.isEmpty(email)) {
//            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
//            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length()<6) {
//          Toast.makeText(this, "Please Enter Password More Than 6 ", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(rePassword)) {
//            Toast.makeText(this, "Please Re-enter Password", Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.setTitle("Registration");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Registration Error", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onClick(View view) {

        if (view == buttonSignup) {
            if (buttonSignup.getText().toString().equals("Sign Up"))
                registerUser();
            else
                userLogin();
        }

        if (view == textViewSignin) {
            if (buttonSignup.getText().toString().equals("Sign In")) {
                textViewSignin.setText("Already Registered? Sign In Here");
                buttonSignup.setText("Sign Up");
                editTextRePassword.setVisibility(View.VISIBLE);
                textView.setText("User Registration");
            } else {
                //open login activity when user taps on the already registered textview
                textViewSignin.setText("Sign Up Here!");
                buttonSignup.setText("Sign In");
                editTextRePassword.setVisibility(View.GONE);
                textView.setText("User Login");
            }
        }

        if(view == googleButton){
            //Toast.makeText(LoginActivity.this, "hhhhhhhaaaa", Toast.LENGTH_LONG).show();
            googleSignIn();
            progressDialog.setMessage("Google Sign In...");
            progressDialog.show();

        }

    }

    private void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Sign-In Successful", Toast.LENGTH_LONG).show();
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(LoginActivity.this, "Error Occurred", Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (buttonSignup.getText().toString().equals("Sign up"))
            finish();
        else {
            textViewSignin.setText("Already Registered? Sign In Here");
            buttonSignup.setText("Sign Up");
            textView.setText("User Registration");
        }
    }
}
