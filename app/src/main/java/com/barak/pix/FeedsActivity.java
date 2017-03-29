/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.barak.pix;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedsActivity extends AppCompatActivity implements PostsFragment.OnPostSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "FeedsActivity";
    private FloatingActionButton mFab;
    private FloatingActionButton mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeds);
        updateIMContactField(getContentResolver(),"gy","hjhj");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.feeds_view_pager);
        FeedsPagerAdapter adapter = new FeedsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(PostsFragment.newInstance(PostsFragment.TYPE_HOME), "אודות");
        adapter.addFragment(PostsFragment.newInstance(PostsFragment.TYPE_FEED), "נופים");

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.feeds_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.isAnonymous()) {
                    Toast.makeText(FeedsActivity.this, "You must sign-in to post.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent newPostIntent = new Intent(FeedsActivity.this, NewPostActivity.class);
                startActivity(newPostIntent);
            }
        });
        mCall = (FloatingActionButton) findViewById(R.id.call);
        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(FeedsActivity.this, android.Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(FeedsActivity.this,
                            android.Manifest.permission.CALL_PHONE)) {
                        Toast.makeText(FeedsActivity.this, "סירבת לתת הרשאל לחיוג", Toast.LENGTH_LONG).show();
                    } else {
                        ActivityCompat.requestPermissions(FeedsActivity.this,
                                new String[]{android.Manifest.permission.CALL_PHONE},
                                55);
                    }
                } else {
                    callPlease();
                }

            }
        });
    }

    @Override
    public void onPostComment(String postKey) {
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra(CommentsActivity.POST_KEY_EXTRA, postKey);
        startActivity(intent);
    }

    @Override
    public void onPostLike(final String postKey) {
        final String userKey = FirebaseUtil.getCurrentUserId();
        final DatabaseReference postLikesRef = FirebaseUtil.getLikesRef();
        postLikesRef.child(postKey).child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User already liked this post, so we toggle like off.
                    postLikesRef.child(postKey).child(userKey).removeValue();
                } else {
                    postLikesRef.child(postKey).child(userKey).setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feeds, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API,
                            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestIdToken(getString(R.string.default_web_client_id))
                                    .build())
                    .build();
            FirebaseAuth.getInstance().signOut();
            //Auth.GoogleSignInApi.signOut(googleApiClient);
            finish();
            return true;
        }
//        else if (id == R.id.action_profile) {
//            startActivity(new Intent(this, ProfileActivity.class));
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    class FeedsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public FeedsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 55: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    callPlease();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void callPlease() {
//        Intent callIntent = new Intent(Intent.ACTION_CALL);
//        callIntent.setData(Uri.parse("tel:0506886910"));
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//               return;
//        }
//        startActivity(callIntent);
        startActivity(new Intent(this,MapsActivity.class));
    }
    private static final String IM_LABEL = "Test protocol";
    private static final String LOG_TAG = "Log";
    /**
     * This method add my account under IM field at default Contact
     * application
     *
     * Labeled with my custom protocol.
     *
     * @param contentResolver
     *            content resolver
     * @param uid
     *            User id from android
     * @param account
     *            account name
     */
    public static void updateIMContactField(ContentResolver contentResolver,
                                            String uid, String account) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID,
                Integer.parseInt(uid));
        contentValues.put(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.Im.TYPE,
                ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM);
        contentValues.put(ContactsContract.CommonDataKinds.Im.LABEL, IM_LABEL);
        contentValues.put(ContactsContract.CommonDataKinds.Im.PROTOCOL,
                ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM);
        contentValues.put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL,
                IM_LABEL);

        contentValues.put(ContactsContract.CommonDataKinds.Im.DATA, account);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValues(contentValues).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Can't update Contact's IM field.");
        }
    }

    /**
     * This method remove IM entry at default Contact application.
     *
     * @param contentResolver
     *            content resolver
     * @param uid
     *            User id from android
     * @param account
     *            account name
     */
    public static void removeIMContactField(ContentResolver contentResolver,
                                            String uid, String account) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                .newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + "=? and "
                                + ContactsContract.Data.MIMETYPE + "=? and "
                                + ContactsContract.CommonDataKinds.Im.DATA
                                + " = ?",
                        new String[] {
                                String.valueOf(uid),
                                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
                                account }).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Can't delete Contact's IM field.");
        }
    }

    /**
     * This method remove IM all entries at default Contact application
     *
     * @param contentResolver
     *            content resolver
     */
    public static void deleteAllIMContactField(ContentResolver contentResolver) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                .newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Data.MIMETYPE
                                + "= ? and "
                                + ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL
                                + "= ?",
                        new String[] {
                                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
                                IM_LABEL }).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            Log.d(LOG_TAG,
                    "An exception occurred when deleting all IM field of Contact.");
        }
    }
}
