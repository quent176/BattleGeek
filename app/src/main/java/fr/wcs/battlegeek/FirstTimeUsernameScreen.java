package fr.wcs.battlegeek;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.wcs.battlegeek.model.PlayerModel;
import fr.wcs.battlegeek.model.Settings;

public class FirstTimeUsernameScreen extends AppCompatActivity {

    private static final String TAG = "RankingFirebaseActivity";
    // Firebase instance variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private ChildEventListener mChildEventListener;
    SharedPreferences mSharedPreferences;
    private String uid;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_first_time_username_screen);

        final EditText playerName = (EditText) findViewById(R.id.input_playername);
        Button button_save = (Button) findViewById(R.id.button_save);

        //Call SharedPref
        mSharedPreferences = getSharedPreferences(Settings.FILE_NAME, MODE_PRIVATE);


        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerName.getText().toString().trim().length() == 0) {
                    Toast.makeText(FirstTimeUsernameScreen.this, R.string.message_error_emptyname, Toast.LENGTH_SHORT);
                } else {
                    PlayerModel newPlayer = new PlayerModel(playerName.getText().toString());

                    // Init Firebase User
                    // Initialize Firebase components
                    mDatabase = FirebaseDatabase.getInstance();
                    mDatabase.setPersistenceEnabled(true);
                    mUsersDatabaseReference = mDatabase.getReference().child("Users");
                    uid = mUsersDatabaseReference.child("Users").push().getKey();
                    mUsersDatabaseReference.child(uid).setValue(newPlayer);
                    mSharedPreferences.edit().putString(Settings.UID, uid).apply();

                    startActivity(new Intent(FirstTimeUsernameScreen.this, MainMenuActivity.class));
                }
            }
        });
    }
}

