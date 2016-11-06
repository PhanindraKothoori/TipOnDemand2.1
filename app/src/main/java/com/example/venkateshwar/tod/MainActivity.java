package com.example.venkateshwar.tod;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<Learning> arrayList = new ArrayList<>();


    private FirebaseAuth mAuth;
    public static HashSet<Learning> hashSet = new HashSet<Learning>();
    ImageButton gimme, add, all;
    TextView textView;
    EditText editText;
    DatabaseReference mDatabase;
    DatabaseReference mLearnings;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private static FirebaseUser user;
    TextView uname;
    String username;
    static boolean already = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //instantiate local firebase and make it persistent offline B]
        if (!already) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            already = true;
        }
        mAuth = FirebaseAuth.getInstance();
        uname = (TextView) findViewById(R.id.uname);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        gimme = (ImageButton) findViewById(R.id.button);
        add = (ImageButton) findViewById(R.id.button2);
        all = (ImageButton) findViewById(R.id.button3);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        user = mAuth.getCurrentUser();
        uname.setTextColor(Color.BLACK);
        final ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Learning>> typeIndicator = new GenericTypeIndicator<List<Learning>>() {
                };
                List<Learning> cloudCopy = dataSnapshot.getValue(typeIndicator);
                HashSet<Learning> hashSet = new HashSet<Learning>();
                if(cloudCopy!=null)
                hashSet.addAll(cloudCopy);
                hashSet.addAll(arrayList);
                arrayList.clear();
                hashSet.remove(null);
                arrayList.addAll(hashSet);
                Collections.sort(arrayList, new Comparator<Learning>() {
                    @Override
                    public int compare(Learning lhs, Learning rhs) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
                        Date l = null, r = null;
                        try {
                            l = dateFormat.parse(lhs.getCreated());
                            r = dateFormat.parse(rhs.getCreated());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return l.compareTo(r);
                    }
                });
                if (arrayList.size()!=cloudCopy.size()) {
                    mLearnings.setValue(arrayList);
                }
                Toast.makeText(getApplicationContext(), "Synced with cloud", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                     User is signed in
                FirebaseUser tempuser = firebaseAuth.getCurrentUser();
                if (tempuser != null) {
                    user = tempuser;
                    username = user.getDisplayName();
                    mLearnings = mDatabase.child("users").child(user.getUid());
                    mLearnings.keepSynced(true);
                    mLearnings.addListenerForSingleValueEvent(vel);

                    uname.setText(user.getDisplayName());
                } else {
//                     User is signed out
                    uname.setText("");
                }
            }
        };


        mAuth.addAuthStateListener(mAuthListener);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclientid))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

//        syncCloud(mLearnings,arrayList);
        if (textView != null) {
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setLongClickable(true);
            textView.setTextIsSelectable(true);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
        }

        if (gimme != null) {
            gimme.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getApplicationContext(), "Gimme one", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        if (all != null) {
            all.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getApplicationContext(), "Show All", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        if (add != null) {
            add.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(getApplicationContext(), "Add", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        if (all != null && textView != null) {
            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    StringBuffer s1 = new StringBuffer();
                    int i = 0;

                    for (Learning str : arrayList) {
                        s1.append(i + 1 + ". " + str.toString() + "\n");
                        i++;
                    }
                    textView.setText(s1);

                }
            });
        }

        if (gimme != null) {
            gimme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    if (arrayList.size() > 0) {
                        int x = (int) (Math.random() * arrayList.size());
                        assert textView != null;
                        textView.setText(arrayList.get(x).getData());
                    }
                }
            });
        }
        if (add != null) {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    String s = editText.getText().toString().trim();
                    try {
                        if (s.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Oy! Write something.", Toast.LENGTH_LONG).show();
                            throw new Exception();
                        }
                        for (Learning learning : arrayList) {
                            if (learning.getData().equals(s)) {
                                Toast.makeText(getApplicationContext(), "This one already exists.", Toast.LENGTH_LONG).show();
                                throw new Exception();
                            }
                        }
                        hashSet.add(new Learning(s));
                        arrayList.add(new Learning(s));
                        editText.setText("");
                        Collections.sort(arrayList, new Comparator<Learning>() {
                            @Override
                            public int compare(Learning lhs, Learning rhs) {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
                                Date l = null, r = null;
                                try {
                                    l = dateFormat.parse(lhs.getCreated());
                                    r = dateFormat.parse(rhs.getCreated());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return l.compareTo(r);
                            }
                        });
                        if (mLearnings != null)
                            mLearnings.setValue(arrayList);
                    } catch (Exception e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }

                }

            });

        }


    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(getApplicationContext(), "Successfully Signed In", Toast.LENGTH_LONG).show();
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(getApplicationContext(), "Couldn't sign in ", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            uname.setText(user.getDisplayName());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signin: {
                signIn();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);

        }

    }
}
