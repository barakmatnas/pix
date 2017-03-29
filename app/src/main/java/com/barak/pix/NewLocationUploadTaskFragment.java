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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.barak.pix.Models.PixLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class NewLocationUploadTaskFragment extends Fragment {
    public static final String TAG = "NewLocationTaskFragment";



    public interface TaskCallbacks {
        void onPostUploaded(String error);
    }
    private Context mApplicationContext;
    private TaskCallbacks mCallbacks;


    public NewLocationUploadTaskFragment() {
        // Required empty public constructor
    }

    public static NewLocationUploadTaskFragment newInstance() {
        return new NewLocationUploadTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across config changes.
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskCallbacks) {
            mCallbacks = (TaskCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskCallbacks");
        }
        mApplicationContext = context.getApplicationContext();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }




    public void uploadLocation(Double lat, Double lon) {
        UploadLocationTask uploadTask = new UploadLocationTask(lat, lon);
        uploadTask.execute();
    }

    class UploadLocationTask extends AsyncTask<Void, Void, Void> {
        public Double lat;
        public Double lon;


        public UploadLocationTask(Double lat, Double lon) {
            this.lat = lat;
            this.lon = lon;

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            final DatabaseReference ref = FirebaseUtil.getBaseRef();
            DatabaseReference locationsRef = FirebaseUtil.getLocationssRef();
            final String newPostKey = locationsRef.push().getKey();
            PixLocation pixLocation = new PixLocation( lat,  lon,  FirebaseUtil.getAuthor().getFull_name(),System.currentTimeMillis());
            Map<String, Object> updatedUserData = new HashMap<>();
            //updatedUserData.put(FirebaseUtil.getPeoplePath() + FirebaseUtil.getAuthor().getUid() + "/locations/" + newPostKey, true);
            updatedUserData.put(FirebaseUtil.getLocationsPath() + newPostKey, new ObjectMapper().convertValue(pixLocation, Map.class));
            ref.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                    if (firebaseError == null) {
                        mCallbacks.onPostUploaded(null);
                    } else {
                        Log.e(TAG, "Unable to create new post: " + firebaseError.getMessage());
                        FirebaseCrash.report(firebaseError.toException());
                        mCallbacks.onPostUploaded(mApplicationContext.getString(
                                R.string.error_upload_task_create));
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(mApplicationContext,"הצלחה",Toast.LENGTH_LONG).show();
        }
    }




}
