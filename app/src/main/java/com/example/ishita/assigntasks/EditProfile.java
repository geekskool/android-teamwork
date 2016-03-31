package com.example.ishita.assigntasks;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Toast;

import com.example.ishita.assigntasks.helper.Config;
import com.example.ishita.assigntasks.helper.PrefManager;
import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This activity is used to update user profile information to Firebase.
 */
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
        loginRef = new Firebase(Config.LOGIN_REF);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setting profile details if details are there on Firebase
        inputName.setText(userDetails.get(Config.KEY_NAME) == null ? "" : userDetails.get(Config.KEY_NAME));
        if (userDetails.get(Config.KEY_PICTURE) != null) {
            String picture64 = userDetails.get(Config.KEY_PICTURE);
            Bitmap bmp = decodeBase64(picture64);
            profilePhoto.setImageBitmap(bmp);
        }
    }

    /**
     * This method converts the base64 encoded string back to the image bitmap
     *
     * @param encodedImage the encoded string to be converted to bitmap
     * @return the bitmap created from the string
     */
    private Bitmap decodeBase64(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    /**
     * OnClick handler for the "update profile" button
     * @param updateButton the button that was clicked to trigger this method
     */
    public void updateProfile(View updateButton) {
        String name = inputName.getText().toString();

        // If name field is not blank, set the user's name in firebase.
        if (!name.equals(""))
            loginRef.child(userDetails.get("mobile")).child(Config.KEY_NAME).setValue(name);

        // If user has updated the profile photo, upload that to firebase.
        if (hasPhoto(profilePhoto)) {
            Bitmap bmp = ((BitmapDrawable) profilePhoto.getDrawable()).getBitmap();
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
            byte[] byteArray = bYtE.toByteArray();
            String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.v("imageFile", imageFile);
            loginRef.child(userDetails.get("mobile")).child(Config.KEY_PICTURE).setValue(imageFile);
            bmp.recycle();
        } else {
            // If user has removed the profile photo or left it blank, remove the profile photo,
            // if any, from firebase.
            loginRef.child(userDetails.get("mobile")).child(Config.KEY_PICTURE).removeValue();
        }
        //display success message to user
        Toast.makeText(getApplicationContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
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

    /**
     * OnClick handler for "edit photo" button
     * @param editPhotoButton the button that was clicked to trigger this method
     */
    public void editPhoto(View editPhotoButton) {

        //The list of edit photo options. Have to use hardcoded string since this is a
        //CharSequence[] and R.string values can't be used since they are int indexes
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Remove Photo"};

        //creating the alert dialog to show when the edit photo button is clicked
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_photo_dialog_title);
        builder.setItems(
                //if the profile photo is set, show the option to remove the photo
                //else show only the first two options.
                (hasPhoto(profilePhoto) ? items : Arrays.copyOfRange(items, 0, 2)),
                new DialogInterface.OnClickListener() {

                    /**
                     * OnClick handler for each of the menu items
                     * @param dialog the menu from which the user selected an item
                     * @param item the menu item that the user clicked on
                     */
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
                            //checking for OS version to call the version-appropriate setImageDrawable()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                profilePhoto.setImageDrawable(getDrawable(R.drawable.blank_profile));
                            } else {
                                profilePhoto.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blank_profile));
                            }
                            dialog.dismiss(); //dismiss the dialog when an option is selected
                        }
                    }
                });
        builder.show(); //finally, show this dialog upon button click
    }

    /**
     * The function that is called when the user returns to EditProfile after having selected a
     * file or taken a photo from the camera app.
     * @param requestCode To determine if the user has returned from the camera app or selected a file
     * @param resultCode success or error
     * @param data data received from the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**if a bitmap was not fetched successfully, there's no point in attempting to
         * parse the data.
         */
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                //if the user took a photo using the camera, save that file to external storage and
                //set that image to the profilePhoto ImageView
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (thumbnail != null) {
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
                } else {
                    Toast.makeText(getApplicationContext(), R.string.image_capture_error, Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == SELECT_FILE) {
                /**
                 * if user chose to upload a file from external storage, set that image to the
                 * profile photo ImageView.
                 */
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
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
