package com.budziaszek.tabmate;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A login screen that offers login (and register) via email/password.
 */
//TODO Sign out (maybe not here)
public class LoginActivity extends Activity {

    private static final String TAG =  "LoginProcedure";

    // UI references
    private EditText mNameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private Button mEmailSignInButton;
    private Button mEmailSignUpButton;
    private Button mEmailRegisterButton;
    private Button  mForgotPasswordButton;
    private View mProgressView;
    private View mLoginFormView;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Boolean doRegister = false;

    /**
     * Starts MainActivity. Called after successful login/register of if user is already logged in.
     */
    public void startMain(){
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);//MainActivity.class);
        LoginActivity.this.startActivity(myIntent);
        finish();
    }

    /**
     * If register page is displayed, go to login page
     */
    @Override
    public void onBackPressed() {
        if(!doRegister) {
            super.onBackPressed();
        }
        else{
            mNameView.setVisibility(View.GONE);
            mPasswordConfirmView.setVisibility(View.GONE);
            mEmailRegisterButton.setVisibility(View.GONE);
            mLoginFormView.setVisibility(View.GONE);
            mEmailSignInButton.setVisibility(View.VISIBLE);
            mEmailSignUpButton.setVisibility(View.VISIBLE);
            doRegister = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        initializeForm();

        if(currentUser != null){
            Log.d(TAG, "User is logged in.");
            startMain();
        }
    }

    private void initializeForm(){
        mNameView = findViewById(R.id.user_name);

        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attempt(false);
                    return true;
                }
                return false;
            }
        });
        mPasswordConfirmView = findViewById(R.id.password_confirm);
        mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attempt(true);
                    return true;
                }
                return false;
            }
        });

        mEmailRegisterButton = findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Manager.hideKeyboard(LoginActivity.this);
                attempt(true);
            }
        });

        mEmailSignUpButton = findViewById(R.id.email_sign_up_button);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doRegister = true;
                mEmailSignInButton.setVisibility(View.GONE);
                mEmailSignUpButton.setVisibility(View.GONE);
                mNameView.setVisibility(View.VISIBLE);
                mNameView.requestFocus();
                mPasswordConfirmView.setVisibility(View.VISIBLE);
                mEmailRegisterButton.setVisibility(View.VISIBLE);
            }
        });

        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Manager.hideKeyboard(LoginActivity.this);
                attempt(false);
            }
        });

        mForgotPasswordButton = findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                alertForgotPassword();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login/register attempt is made.
     */
    private void attempt(Boolean register) {
        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirm = mPasswordConfirmView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check if password and confirm password are same
        if(register){
            if(!password.equals(confirm)) {
                cancel = true;
                focusView = mPasswordConfirmView;
                mPasswordConfirmView.setError(getString(R.string.error_passwords_not_match));
            }
        }

        // Check for a valid confirm password, if the user entered one.
        if (register && confirm.length() == 0 ) {
            mPasswordConfirmView.setError(getString(R.string.error_field_required));
            focusView = mPasswordConfirmView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (password.length() == 0 || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_too_short_password));
            focusView = mPasswordView;
            cancel = true;
        }

        //Check for a valid name.
        if (register && TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Attempt login or register
            if (register) {
                doRegisterTask(name, email, password);
            }
            else {
                doLoginTask(email, password);
            }
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
    }

    /**
     * Represents an asynchronous login used to authenticate, where Firebase is used.
     */
    protected void doLoginTask(final String email, final String password) {
        showProgress(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Exception exception = task.getException();
                            if(exception != null) {
                                InformUser.informFailure(LoginActivity.this, exception);
                                Log.e(TAG, "Login user failure.");
                            }
                            mForgotPasswordButton.setVisibility(View.VISIBLE);
                            showProgress(false);
                        } else {
                            Log.d(TAG, "Login user success.");
                            showProgress(false);
                            startMain();
                        }
                    }
                });
    }
    /**
     * Represents an asynchronous register used to authenticate, where Firebase is used.
     * After successful register attempts to login.
     */
    protected void doRegisterTask(final String name, final String email, final String password) {
        showProgress(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "Register user in Firestore success.");
                            addUser(LoginActivity.this, task.getResult().getUser().getUid(), name, email);
                            currentUser = mAuth.getCurrentUser();
                            sendVerificationEmail();
                            doLoginTask(email, password);
                        } else {
                            // If sign in fails, display a message to the user.
                            Exception exception = task.getException();
                            if(exception != null) {
                                InformUser.informFailure(LoginActivity.this, task.getException());
                                Log.e(TAG, "Sign in failure.");
                            }
                            showProgress(false);
                        }
                    }
                });
    }

    private void sendVerificationEmail(){
        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            InformUser.inform(LoginActivity.this, R.string.verification_email_sent);
                            Log.d(TAG, "Verification email sent.");
                        }
                    }
                });
    }

    /**
     * Creates User instance and adds new user to Firestore database to store more information.
     */
    public static void addUser(final Context activity, String id, String name, String email){
        User newUser = new User(id, name, email);
        FirestoreRequests firestoreRequests = new FirestoreRequests();
        firestoreRequests.addUser(newUser, id,
                (Void) -> Log.d(TAG, "Add user document to Firestore success."),
                (e) -> InformUser.informFailure(activity, e));
    }


    /**
     * Alert to enter email if user forgot password.
     */
    public void alertForgotPassword(){

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle(R.string.enter_email);

        // Set up the input
        final EditText input = new EditText(LoginActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundColor(getResources().getColor(R.color.colorAccentLightSemi, LoginActivity.this.getTheme()));

        FrameLayout container = new FrameLayout(LoginActivity.this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 10, 30, 10);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailAddress = input.getText().toString();
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                   InformUser.inform(LoginActivity.this, R.string.email_reset_sent);
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}

