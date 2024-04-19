package com.example.vehiclemessenger;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vehiclemesssenger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRscanner extends AppCompatActivity {

    ActivityResultLauncher<ScanOptions> barLauncher;
    private static final String CHANNEL_ID = "chat_notification";
    private static final String CHANNEL_NAME = "Chat Notification";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        // Initialize the ActivityResultLauncher
        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedId = result.getContents();
                openChatRoomWithUser(scannedId);
            }
        });

        // Trigger scanning when the button is clicked
        findViewById(R.id.btn_scan).setOnClickListener(v -> scanCode());
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to turn on flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    private void openChatRoomWithUser(String userId) {
        // Check if the user ID obtained from QR code exists in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User exists, proceed to open chat room with this user
                    Intent intent = new Intent(QRscanner.this, chatWin.class);
                    intent.putExtra("nameeee", dataSnapshot.child("userName").getValue(String.class));
                    intent.putExtra("reciverImg", dataSnapshot.child("profilepic").getValue(String.class));
                    intent.putExtra("uid", userId);
                    startActivity(intent);
                } else {
                    // User does not exist
                    showToast("User not found for ID: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
