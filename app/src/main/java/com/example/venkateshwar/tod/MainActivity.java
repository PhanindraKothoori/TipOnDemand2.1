package com.example.venkateshwar.tod;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.firebase.database.ChildEventListener;
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

public class MainActivity extends AppCompatActivity{
    public static ArrayList<Learning> arrayList = new ArrayList<>();

    private FirebaseAuth mAuth;
    ImageButton gimme, add, all;
    TextView textView;
    EditText editText;
    DatabaseReference mDatabase;
    static DatabaseReference mLearnings;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private static FirebaseUser user;
    TextView uname;
    String username;
    static boolean already = false;

    ValueEventListener vel=new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            GenericTypeIndicator<List<Learning>> typeIndicator = new GenericTypeIndicator<List<Learning>>() {
            };
            List<Learning> cloudCopy = dataSnapshot.getValue(typeIndicator);
            HashSet<Learning> hashSet = new HashSet<Learning>();
            if (cloudCopy != null)
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
            if (cloudCopy != null && arrayList.size() != cloudCopy.size()) {
                mLearnings.setValue(arrayList);
            }
            Snackbar.make(findViewById(android.R.id.content), "Synced with cloud", Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    SwipeRefreshLayout srl;
    void refresh() {
        if (mLearnings!=null) {
            mLearnings.addValueEventListener(vel);
            mLearnings.removeEventListener(vel);
            Snackbar.make(findViewById(android.R.id.content), "Successfully Synced with Cloud.", Snackbar.LENGTH_LONG).show();
        }
        else{
            Snackbar.make(findViewById(android.R.id.content), "You're not logged in.", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!already) { //instantiate local firebase and make it persistent offline B]
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            already = true;
        }
//        getActionBar().setHomeButtonEnabled(true);
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

        srl= (SwipeRefreshLayout) findViewById(R.id.srl);

        if (srl==null){
            Snackbar.make(findViewById(android.R.id.content), "Srl Error.", Snackbar.LENGTH_LONG).show();

        }
        else {
//            srl.setNestedScrollingEnabled(true);
            srl.requestDisallowInterceptTouchEvent(true);
            srl.setEnabled(true);
            srl.setColorSchemeColors(getResources().getColor(R.color.white),
                    R.color.white,R.color.common_google_signin_btn_text_light);
            srl.setProgressBackgroundColorSchemeColor(Color.argb(255,8,0x94,0xff));

            srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refresh();
                    srl.setRefreshing(false);
                }
            });
        }
        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser tempuser = firebaseAuth.getCurrentUser();
                //User is signed in
                if (tempuser != null) {
                    user = tempuser;
                    username = user.getDisplayName();
                    mLearnings = mDatabase.child("users").child(user.getUid());
                    mLearnings.keepSynced(true);
                    AsyncTask.execute(runnable);
                    mLearnings.removeEventListener(vel);
                    mLearnings.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Snackbar.make(findViewById(android.R.id.content), "Successfully Added", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            Snackbar.make(findViewById(android.R.id.content), "Successfully Edited", Snackbar.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            Snackbar.make(findViewById(android.R.id.content), "Successfully Deleted", Snackbar.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            Snackbar.make(findViewById(android.R.id.content), "Successfully Moved", Snackbar.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Snackbar.make(findViewById(android.R.id.content), "Successfully Cancelled", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                    uname.setText(user.getDisplayName());
                } else {
                    //User is signed out
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

        if (all != null && textView != null) {
            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(), ListActivity.class);
                    startActivity(intent);
                }
            });
        }

        if (gimme != null) {
            gimme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unFocus();
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
                    unFocus();
                    String s = editText.getText().toString().trim();
                    try {
                        if (s.isEmpty()) {
                            Snackbar.make(findViewById(android.R.id.content), "Oy! Write something.", Snackbar.LENGTH_LONG).show();

                            throw new Exception();
                        }
                        for (Learning learning : arrayList) {
                            if (learning.getData().equals(s)) {
                                Snackbar.make(findViewById(android.R.id.content), "This one already exists.", Snackbar.LENGTH_LONG).show();
                                throw new Exception();
                            }
                        }
                        arrayList.add(new Learning(s));
//                        editText.setText("");
//                        Collections.sort(arrayList, new Comparator<Learning>() {
//                            @Override
//                            public int compare(Learning lhs, Learning rhs) {
//                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss");
//                                Date l = null, r = null;
//                                try {
//                                    l = dateFormat.parse(lhs.getCreated());
//                                    r = dateFormat.parse(rhs.getCreated());
//                                } catch (ParseException e) {
//                                    e.printStackTrace();
//                                }
//                                return l.compareTo(r);
//                            }
//                        });
                        if (mLearnings != null) {
                            mLearnings.child(String.valueOf(arrayList.size())).setValue(new Learning(s));
//                            mLearnings.setValue(arrayList);
                        }
                        editText.setText("");
                    } catch (Exception e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }

                }

            });

        }
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mLearnings.addListenerForSingleValueEvent(vel);
        }
    };

    void unFocus(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                Snackbar.make(findViewById(android.R.id.content), "Successfully Signed In", Snackbar.LENGTH_LONG).show();
            } else {
                // Google Sign In failed, update UI appropriately
                Snackbar.make(findViewById(android.R.id.content), "Couldn't sign in ", Snackbar.LENGTH_LONG).show();
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
                            Snackbar.make(findViewById(android.R.id.content), "Authentication failed.",
                                    Snackbar.LENGTH_SHORT).show();
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
