package com.ssmptc.QrRegistry.ShopLoginSignup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ssmptc.QrRegistry.DataBase.Model;
import com.ssmptc.QrRegistry.DataBase.SessionManagerShop;
import com.ssmptc.QrRegistry.R;

public class ShopProfile extends AppCompatActivity {


    TextView tv_shopName,tv_shopId;
    ImageView btn_back;
    private TextInputLayout et_ShopName,et_LicenseNumber,et_category,et_location,et_ownerName,et_email,et_time,et_days ,et_description;
    private Button btnUpload,btnShowAll;
    private ImageView btnChooseImg;
    private ProgressDialog progressDialog;

    //vars
    private DatabaseReference root,reference ;
    private StorageReference storageReference;
    private Uri filePath;
    private String shopId;
    private String  _ShopName,_LicenseNumber,_category,_location,_ownerName,_email,_time,_days,_description;
    SessionManagerShop managerShop;



    EditText desc;

    // Minimum Android Version jellybean
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_profile);

        tv_shopName = findViewById(R.id.tv_shopName);
        tv_shopId = findViewById(R.id.tv_shopId);
        btn_back = findViewById(R.id.btn_backToSd);

        btnChooseImg = findViewById(R.id.btn_chooseImage);
        btnUpload= findViewById(R.id.btn_uploadImage);
        btnShowAll= findViewById(R.id.btn_showAllImage);


        // Update Details
        et_ShopName = findViewById(R.id.et_shopName);
        et_LicenseNumber = findViewById(R.id.et_LicenseNumber);
        et_category = findViewById(R.id.et_category);
        et_location = findViewById(R.id.et_shopLocation);
        et_ownerName = findViewById(R.id.et_ownerName);
        et_email = findViewById(R.id.et_email);
        et_time = findViewById(R.id.et_time);
        et_days = findViewById(R.id.et_Days);
        et_description = findViewById(R.id.et_description);


        managerShop = new SessionManagerShop(getApplicationContext());
        shopId = managerShop.getShopId();

        tv_shopName.setText(managerShop.getShopName());
        tv_shopId.setText(managerShop.getShopId());

        reference = FirebaseDatabase.getInstance().getReference("Shops").child(shopId).child("Shop Profile");

        root = FirebaseDatabase.getInstance().getReference("Shops").child(shopId).child("Shop Images");
        storageReference = FirebaseStorage.getInstance().getReference("ShopImages").child(shopId);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShopProfile.this,ShopDashBoard.class));
                finish();
            }
        });

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (filePath!= null){
                    uploadImage(filePath);
                }else{
                    Toast.makeText(ShopProfile.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShopProfile.this,ShopImages.class));
            }
        });
        dbUpdate();

    }

    private void chooseImage() {

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, 2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            // checking request code and result code
            // if request code is PICK_IMAGE_REQUEST and
            // resultCode is RESULT_OK
            // then set image in the image view
            if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {

                // Get the Uri of data
                filePath = data.getData();
                btnChooseImg.setImageURI(filePath);

            }

        }

    // UploadImage method
    private void uploadImage(Uri uri)
    {
        //Initialize ProgressDialog
        progressDialog = new ProgressDialog(ShopProfile.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        progressDialog.dismiss();

                        Model model = new Model(uri.toString());
                        String modelId = root.push().getKey();
                        if (modelId != null) {
                            root.child(modelId).setValue(model);
                        }
                        Toast.makeText(ShopProfile.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        btnChooseImg.setImageResource(R.drawable.add_image1);
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ShopProfile.this, "Uploading Failed !!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));

    }

    public void updateData(View view) {





        if (!validateShopName() | !validateCategory() | !validateLicense() | !validateOwnerName() | !validateLocation()) {

            return;
        }

        dbUpdate();



        if (isNameChanged() | isCategoryChanged() | isLocationChanged() | isPhoneNumberChanged() | isLicenseChanged() | isEmailChanged() | isDescriptionChanged() | isTimeChanged() | isDayChanged()){

            //managerShop.setDetails("","",_ShopName,"","","","");
            Toast.makeText(ShopProfile.this, "Data has been Updated", Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(ShopProfile.this, "Data is same and can not be Updated", Toast.LENGTH_SHORT).show();
    }



    private void dbUpdate(){
        Query getShopData = FirebaseDatabase.getInstance().getReference("Shops").child(shopId).child("Shop Profile");
        getShopData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                _ShopName = dataSnapshot.child("shopName").getValue(String.class);
                et_ShopName.getEditText().setText(_ShopName);
                tv_shopName.setText(dataSnapshot.child("shopName").getValue(String.class));
                _category = dataSnapshot.child("category").getValue(String.class);
                et_category.getEditText().setText(_category);
                _location = dataSnapshot.child("location").getValue(String.class);
                et_location.getEditText().setText(_location);
                _ownerName = dataSnapshot.child("ownerName").getValue(String.class);
                et_ownerName.getEditText().setText(_ownerName);
                _LicenseNumber = dataSnapshot.child("licenseNumber").getValue(String.class);
                et_LicenseNumber.getEditText().setText(_LicenseNumber);
                _email = dataSnapshot.child("email").getValue(String.class);
                et_email.getEditText().setText(_email);
                _description = dataSnapshot.child("description").getValue(String.class);
                et_description.getEditText().setText(_description);
                _time = dataSnapshot.child("working time").getValue(String.class);
                et_time.getEditText().setText(_time);
                _days = dataSnapshot.child("working days").getValue(String.class);
                et_days.getEditText().setText(_days);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private boolean isTimeChanged() {


        if (!_time.equals(et_time.getEditText().getText().toString())){
            reference.child("working time").setValue(et_time.getEditText().getText().toString());
            return true;
        }else
            return false;
    }

    private boolean isDayChanged() {

        if (!_days.equals(et_days.getEditText().getText().toString())){
            reference.child("working days").setValue(et_days.getEditText().getText().toString());
            return true;
        }else
            return false;

    }

    private boolean isDescriptionChanged() {
        if (!_description.equals(et_description.getEditText().getText().toString())){

            reference.child("description").setValue(et_description.getEditText().getText().toString());
            return true;
        }else
            return false;
    }

    private boolean isEmailChanged() {
        if (!_email.equals(et_email.getEditText().getText().toString())){

            reference.child("email").setValue(et_email.getEditText().getText().toString());
            return true;
        }else
            return false;
    }

    private boolean isLicenseChanged() {
        if (!_LicenseNumber.equals(et_LicenseNumber.getEditText().getText().toString())){

            reference.child("licenseNumber").setValue(et_LicenseNumber.getEditText().getText().toString());
            return true;
        }else
            return false;
    }

    private boolean isPhoneNumberChanged() {
        if (!_ownerName.equals(et_ownerName.getEditText().getText().toString())){

            reference.child("ownerNameNumber").setValue(et_ownerName.getEditText().getText().toString());
            return true;
        }else
            return false;

    }

    private boolean isLocationChanged() {
        if (!_location.equals(et_location.getEditText().getText().toString())){

            reference.child("location").setValue(et_location.getEditText().getText().toString());
            return true;
        }else
            return false;
    }

    private boolean isCategoryChanged() {
        if (!_category.equals(et_category.getEditText().getText().toString())){

            reference.child("category").setValue(et_category.getEditText().getText().toString());
            return true;
        }else
            return false;
    }

    private boolean isNameChanged() {
        if (!_ShopName.equals(et_ShopName.getEditText().getText().toString())){

            reference.child("shopName").setValue(et_ShopName.getEditText().getText().toString());
            return true;
        }else
            return false;
    }


    private boolean validateLicense(){
        String val1 = et_LicenseNumber.getEditText().getText().toString().trim();

        if (val1.isEmpty()){
            et_LicenseNumber.setError("Day can not be empty");
            return false;
        }
        else{
            et_LicenseNumber.setError(null);
            et_LicenseNumber.setErrorEnabled(false);
            return true;
        }

    }
    private boolean validateShopName(){
        String val1 = et_ShopName.getEditText().getText().toString().trim();

        if (val1.isEmpty()){
            et_ShopName.setError("Field can not be empty");
            return false;
        }
        else{
            et_ShopName.setError(null);
            et_ShopName.setErrorEnabled(false);
            return true;
        }

    }
    private boolean validateLocation(){
        String val1 = et_location.getEditText().getText().toString().trim();

        if (val1.isEmpty()){
            et_location.setError("Field can not be empty");
            return false;
        }
        else{
            et_location.setError(null);
            et_location.setErrorEnabled(false);
            return true;
        }

    }
    private boolean validateCategory(){
        String val1 = et_category.getEditText().getText().toString().trim();

        if (val1.isEmpty()){
            et_category.setError("Field can not be empty");
            return false;
        }
        else{
            et_category.setError(null);
            et_category.setErrorEnabled(false);
            return true;
        }

    }
    private boolean validateOwnerName(){
        String val1 = et_ownerName.getEditText().getText().toString().trim();

        if (val1.isEmpty()){
            et_ownerName.setError("Field can not be empty");
            return false;
        }
        else{
            et_ownerName.setError(null);
            et_ownerName.setErrorEnabled(false);
            return true;
        }

    }

}