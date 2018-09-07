package com.budziaszek.tabmate;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A login screen that offers login (and register) via email/password.
 */
public class LoginActivity extends Activity {

    private UserLoginTask mAuthTask = null;

    // UI references
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private Button mEmailSignInButton;
    private Button mEmailSignUpButton;
    private Button mEmailRegisterButton;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Boolean doRegister = false;

    // Start main activity (called after login)
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
            mPasswordConfirmView.setVisibility(View.GONE);
            mEmailRegisterButton.setVisibility(View.GONE);
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
            Log.d("User", currentUser.getEmail());
            startMain();
        }
    }

    private void initializeForm(){
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin(false);
                    return true;
                }
                return false;
            }
        });
        mPasswordConfirmView = (EditText) findViewById(R.id.password_confirm);
        mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin(true);
                    return true;
                }
                return false;
            }
        });

        mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(LoginActivity.this);
                attemptLogin(true);
            }
        });

        mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doRegister = true;
                mEmailSignInButton.setVisibility(View.GONE);
                mEmailSignUpButton.setVisibility(View.GONE);
                mPasswordConfirmView.setVisibility(View.VISIBLE);
                mEmailRegisterButton.setVisibility(View.VISIBLE);
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(LoginActivity.this);
                attemptLogin(false);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    // Hide the keyboard if necessary
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(Boolean register) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (password.length() == 0 || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_too_short_password));
            focusView = mPasswordView;
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

        //Check if password and confirm password are same
        if(register){
            if(mPasswordView.getText().equals(mPasswordConfirmView.getText())) {
                cancel = true;
                focusView = mPasswordConfirmView;
                mPasswordConfirmView.setError(getString(R.string.error_passwords_not_same));
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Attempt login or register
            mAuthTask = new UserLoginTask(email, password);
            if (register) {
                mAuthTask.doRegisterTask();
            }
            else {
                mAuthTask.doLoginTask();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask{

        private final String mEmail;
        private final String mPassword;
        private final String TAG = "Login";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }
        protected void doLoginTask() {
            showProgress(true);
            mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                //getString(R.string.error_invalid_password)
                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                Log.w(TAG, "loginUserWithEmail:failure", task.getException());
                                finishTask(false);
                            } else {
                                Log.w(TAG, "loginUserWithEmail:success");
                                finishTask(true);
                            }
                        }
                    });
        }
        protected void doRegisterTask() {
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                currentUser = mAuth.getCurrentUser();
                                doLoginTask();
                                //finishTask(true);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_SHORT).show();
                                finishTask(false);
                            }
                        }
                    });
        }
        protected void finishTask(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                startMain();
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

    }
}

