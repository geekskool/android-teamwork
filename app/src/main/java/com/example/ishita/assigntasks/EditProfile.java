package com.example.ishita.assigntasks;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class EditProfile extends AppCompatActivity {

    public static final int SELECT_FILE = 2;
    public static final int REQUEST_CAMERA = 1;

    ImageView profilePhoto;
    TextView inputName;
    PrefManager prefManager;
    HashMap<String, String> userDetails;
    Firebase loginRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Firebase.setAndroidContext(this);

        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
        inputName = (TextView) findViewById(R.id.inputName);
        prefManager = new PrefManager(getApplicationContext());
        userDetails = prefManager.getUserDetails();
        loginRef = new Firebase(PrefManager.LOGIN_REF);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inputName.setText(userDetails.get("name") == null ? "" : userDetails.get("name"));
        if (userDetails.get("picture") != null) {
            String picture64 = userDetails.get("picture");
            Bitmap bmp = decodeBase64(picture64);
            profilePhoto.setImageBitmap(bmp);
        }
    }

    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void updateProfile(View updateButton) {
        String name = inputName.getText().toString();
        if (!name.equals(""))
            loginRef.child(userDetails.get("mobile")).child("name").setValue(name);

        if (hasPhoto(profilePhoto)) {
            Bitmap bmp = ((BitmapDrawable) profilePhoto.getDrawable()).getBitmap();
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
            byte[] byteArray = bYtE.toByteArray();
            String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.v("imageFile", imageFile);
            loginRef.child(userDetails.get("mobile")).child("picture").setValue(imageFile);
            bmp.recycle();
        } else {
            loginRef.child(userDetails.get("mobile")).child("picture").removeValue();
        }
    }

    /**
     * This function checks if the image resource currently attached to an ImageView is the same as
     * that set in the XML.
     *
     * @param imageView The ImageView whose contents are to be checked
     * @return returns true if the image resource inside the ImageView was replaced with another;
     * else returns false
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private boolean hasPhoto(ImageView imageView) {
        boolean result = true;
        Drawable.ConstantState constantState;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            constantState = getResources()
                    .getDrawable(R.drawable.blank_profile, getTheme())
                    .getConstantState();
        } else {
            constantState = getResources()
                    .getDrawable(R.drawable.blank_profile)
                    .getConstantState();
        }

        if (imageView.getDrawable().getConstantState() == constantState) {
            result = false;
        }

        return result;
    }

    public void editPhoto(View editPhotoButton) {
        final CharSequence[] items = {"Take Photo", "Choose from Library", new StringBuilder()};
        if (hasPhoto(profilePhoto)) {
            items[2] = "Remove Photo";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_photo_dialog_title);
        builder.setItems(
                (items[2] == "Remove Photo" ? items : Arrays.copyOfRange(items, 0, 2)),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CAMERA);
                        } else if (items[item].equals("Choose from Library")) {
                            Intent intent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select File"),
                                    SELECT_FILE);
                        } else if (items[item].equals("Remove Photo")) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                profilePhoto.setImageDrawable(getDrawable(R.drawable.blank_profile));
                            } else {
                                profilePhoto.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blank_profile));
                            }
                            dialog.dismiss();
                        }
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profilePhoto.setImageBitmap(thumbnail);
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);
                profilePhoto.setImageBitmap(bm);
            }
        }
    }
}
