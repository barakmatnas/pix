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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth mAuth;
    private TextView mTextView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseUtil.getCurrentUserId() != null) {
//            startActivity(new Intent(this, ProfileActivity.class));
            Intent feedsIntent = new Intent(this, FeedsActivity.class);
            startActivity(feedsIntent);
            finish();
        }
        else{
            setContentView(R.layout.activity_welcome);

            findViewById(R.id.sign_in_button).setOnClickListener(this);
            mAuth = FirebaseAuth.getInstance();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.sign_in_button:
                Intent signInIntent = new Intent(this, ProfileActivity.class);
                startActivity(signInIntent);
                finish();
                break;
        }
    }
}
