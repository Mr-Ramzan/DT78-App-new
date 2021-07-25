package com.fbiego.dt78.firebase

import android.app.Activity
import android.util.Log
import com.fbiego.dt78.R
import com.fbiego.dt78.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class FirebaseMethods(activity: Activity) {
    private var mActivity: Activity
    private var userID: String? = null
    private var mAuth: FirebaseAuth
    private var myRef: DatabaseReference
    private var database: FirebaseDatabase
    private var mStorage: FirebaseStorage
    private var mStorageRef: StorageReference
    private var mediaCount: Long = 0

    companion object {
        private const val TAG = "FirebaseMethods"
    }

    init {
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        mStorage = FirebaseStorage.getInstance()
        myRef = database.reference
        mStorageRef = mStorage.reference
        mActivity = activity
        if (mAuth.currentUser != null) {
            userID = mAuth.currentUser!!.uid
        }
    } //
    //
        private fun  checkIfUsernameExists( userName : String,  usersSnapshot: DataSnapshot):Boolean{
            //Checking if user_name already exists.
           var user = User()
            Log.d(TAG,"Username checking");
            for(ds in  usersSnapshot.child(mActivity.getString(R.string.users_node)).getChildren()){

                user.username = (Objects.requireNonNull(ds.getValue(User::class.java))?.username);
                if(user.username.equals(userName.toLowerCase())){
                    Log.d(TAG,"Username exists");
                    return true;
                }
            }
            return false;
        }
    //
    //
    //    public void signUp(String photo, final String userName, final String Phone, String bloodGroup, final String city, final String area, final UserLocation location, final boolean isDonar, final boolean needBlood, final ProgressBar progressBar, UserDataAddedCallback callback) {
    //
    //        progressBar.setVisibility(View.VISIBLE);
    //        final FirebaseUser user = mAuth.getCurrentUser();
    //        userID = Objects.requireNonNull(user).getUid();
    //
    //        Query query = myRef.child(mActivity.getString(R.string.users_node)).orderByChild(mActivity.getString(R.string.usernameField)).equalTo(StringManipulation.condenseUserName(userName));
    //        query.addListenerForSingleValueEvent(new ValueEventListener() {
    //            @Override
    //            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    //
    //                if (!dataSnapshot.exists()) {
    ////                  (String username, String displayName, String userCity, String subArea, Location location, String phoneNo, String bloodGroup, String profile_photo, boolean canDonate, boolean needBlood)
    //                    addNewUserData(userName, userName, city, area, location, Phone, bloodGroup, photo, isDonar, needBlood, callback);
    //                    Toast.makeText(mActivity, "Adding User!!", Toast.LENGTH_SHORT).show();
    //
    //                } else {
    //                    String name = userName + ".";
    //                    name += Objects.requireNonNull(myRef.push().getKey()).substring(3, 10);
    //                    addNewUserData(name, userName, city, area, location, Phone, bloodGroup, photo, isDonar, needBlood, callback);
    //                    Toast.makeText(mActivity, "Username already exists!!", Toast.LENGTH_SHORT).show();
    //                }
    //            }
    //
    //            @Override
    //            public void onCancelled(@NonNull DatabaseError databaseError) {
    //                Toast.makeText(mActivity, "Cancelled!!", Toast.LENGTH_SHORT).show();
    //
    //            }
    //        });
    //    }
    //
    //
    //    private void sendVerificationEmail(final Boolean isRequired) {
    //
    //        FirebaseUser user = mAuth.getCurrentUser();
    //        if (user != null) {
    //            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
    //                @Override
    //                public void onComplete(@NonNull Task<Void> task) {
    //                    if (task.isSuccessful()) {
    //                        Log.d(TAG, "Verification code sent");
    //                        Toast.makeText(mActivity, "Verification email sent, please check your inbox", Toast.LENGTH_SHORT).show();
    //
    //                        if (isRequired) {
    //                            mAuth.signOut();
    //                            mActivity.finish();
    //                        }
    //                    } else {
    //                        Toast.makeText(mActivity, "Couldn't send verification email !!", Toast.LENGTH_SHORT).show();
    //                    }
    //                }
    //            });
    //        }
    //    }
    //
    //
    //    public void reAuthenticateUser(final String newEmail, String password) {
    //        try {
    //            AuthCredential credential = EmailAuthProvider
    //                    .getCredential(mAuth.getCurrentUser().getEmail(), password);
    //            // Prompt the user to re-provide their sign-in credentials
    //            Objects.requireNonNull(mAuth.getCurrentUser()).reauthenticate(credential)
    //                    .addOnCompleteListener(new OnCompleteListener<Void>() {
    //                        @Override
    //                        public void onComplete(@NonNull Task<Void> task) {
    //                            if (task.isSuccessful()) {
    //                                Log.d(TAG, "User re-authenticated.");
    //                                sendVerificationEmail(false);
    //                                updateEmail(newEmail);
    //                            } else {
    //                                Log.d(TAG, "User re-authentication failed.");
    //                                Toast.makeText(mActivity, "Failed!! Your password doesn't match!!", Toast.LENGTH_SHORT).show();
    //                            }
    //                        }
    //                    });
    //        } catch (NullPointerException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //
    //    private void updateEmail(final String newEmail) {
    //
    //        FirebaseUser user = mAuth.getCurrentUser();
    //
    //        Objects.requireNonNull(user).updateEmail(newEmail)
    //                .addOnCompleteListener(new OnCompleteListener<Void>() {
    //                    @Override
    //                    public void onComplete(@NonNull Task<Void> task) {
    //                        if (task.isSuccessful()) {
    //                            myRef.child(mActivity.getString(R.string.users_node))
    //                                    .child(userID)
    //                                    .child(mActivity.getString(R.string.emailField)).setValue(newEmail);
    //                            Toast.makeText(mActivity, "Email updated", Toast.LENGTH_SHORT).show();
    //                            Log.d(TAG, "User email address updated.");
    //                        } else {
    //                            Toast.makeText(mActivity, "Failed!!", Toast.LENGTH_SHORT).show();
    //
    //                        }
    //                    }
    //                });
    //    }
    //
    //    public void updateUsername(String username) {
    //
    //        //In users node
    //        myRef.child(mActivity.getString(R.string.users_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.usernameField)).setValue(username);
    //
    //        //In user_account_settings node
    //        myRef.child(mActivity.getString(R.string.user_account_settings_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.usernameField)).setValue(username);
    //    }
    //
    //    public void updateDisplayname(String displayName) {
    //
    //        //In user_account_settings node
    //        myRef.child(mActivity.getString(R.string.user_account_settings_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.displayNameField)).setValue(displayName);
    //    }
    //
    //    public void updateDescription(String description) {
    //
    //        //In user_account_settings node
    //        myRef.child(mActivity.getString(R.string.user_account_settings_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.descriptionField)).setValue(description);
    //    }
    //
    //    public void updateWebsite(String website) {
    //
    //        //In user_account_settings node
    //        myRef.child(mActivity.getString(R.string.user_account_settings_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.websiteField)).setValue(website);
    //    }
    //
    //    private void updateProfilePhoto(String photoUri) {
    //
    //        //In user_account_settings node
    //        myRef.child(mActivity.getString(R.string.user_account_settings_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.profilePhotoField)).setValue(photoUri);
    //    }
    //
    //    public void updatePhoneNo(long phoneNo) {
    //
    //        //In users node
    //        myRef.child(mActivity.getString(R.string.users_node))
    //                .child(userID)
    //                .child(mActivity.getString(R.string.phoneNoField)).setValue(phoneNo);
    //    }
    //
    //    public void addFollowingAndFollowers(String uid) {
    //
    //        FirebaseDatabase.getInstance().getReference()
    //                .child(mActivity.getString(R.string.node_following))
    //                .child(userID)
    //                .child(uid)
    //                .child(mActivity.getString(R.string.users_id))
    //                .setValue(uid);
    //
    //        FirebaseDatabase.getInstance().getReference()
    //                .child(mActivity.getString(R.string.node_followers))
    //                .child(uid)
    //                .child(userID)
    //                .child(mActivity.getString(R.string.users_id))
    //                .setValue(userID);
    //    }
    //
    //    public void removeFollowingAndFollowers(String uid) {
    //
    //        FirebaseDatabase.getInstance().getReference()
    //                .child(mActivity.getString(R.string.node_following))
    //                .child(userID)
    //                .child(uid)
    //                .removeValue();
    //
    //        FirebaseDatabase.getInstance().getReference()
    //                .child(mActivity.getString(R.string.node_followers))
    //                .child(uid)
    //                .child(userID)
    //                .removeValue();
    //    }
    //
    //    public MasterUserSettings retrieveUserData(DataSnapshot dataSnapshot, String userID, boolean needAccountSettings, boolean needUser) {
    //
    //        User user = new User();
    //        UserAccountSettings settings = new UserAccountSettings();
    //
    //        for (DataSnapshot ds : dataSnapshot.getChildren()) {
    //
    //            try {
    //                //Getting data from the UserAccountSettings node
    //                if (needAccountSettings && Objects.requireNonNull(ds.getKey()).equals(mActivity.getString(R.string.user_account_settings_node))) {
    //                    try {
    //                        settings= Objects.requireNonNull(ds.child(userID).getValue(UserAccountSettings.class));
    //
    //                    } catch (Exception e) {
    //                        e.printStackTrace();
    //                    }
    //
    //                }
    //                //Getting data from the Users node
    //                if (needUser && Objects.requireNonNull(ds.getKey()).equals(mActivity.getString(R.string.users_node))) {
    //                    try {
    //                        user= Objects.requireNonNull(ds.child(userID).getValue(User.class));
    //
    //
    //
    //                    } catch (Exception e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            } catch (NullPointerException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //        return new MasterUserSettings(user, settings);
    //    }
    //
    //    public ArrayList<User> retrieveUsersData(DataSnapshot dataSnapshot) {
    //
    //        ArrayList<User> users = new ArrayList<>();
    //        for (DataSnapshot ds : dataSnapshot.getChildren()) {
    //            try {
    //                //Getting data from the Users node
    //                User user = ds.getValue(User.class);
    //                assert user != null;
    //                if (user.getDonor() && !user.getUserId().equals(userID) && (user.getUserCity().equals(StaticSupport.citySearched) && user.getUserBloodGroup().equals(StaticSupport.typeSearched))) {
    //                    users.add(user);
    //                }
    //            } catch (NullPointerException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //        return users;
    //    }
    //
    //    public void uploadProfilePhoto(Uri imageUrl, UploadProfileCallback calback) {
    //
    //        final String FIREBASE_IMAGE_STORAGE = "photos/users/";
    //        FileCompressor compressor = new FileCompressor(mActivity);
    //        final StorageReference storageReference;
    //
    //
    //        storageReference = mStorageRef.child(FIREBASE_IMAGE_STORAGE + userID + "/profile_photo");
    //        UploadTask uploadTask = storageReference.putFile(imageUrl);
    //
    //        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
    //            @Override
    //            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
    //                if (!task.isSuccessful()) {
    //                    throw Objects.requireNonNull(task.getException());
    //                }
    //                // Continue with the task to get the download URL
    //                return storageReference.getDownloadUrl();
    //            }
    //        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
    //            @Override
    //            public void onComplete(@NonNull Task<Uri> task) {
    //
    //                if (task.isSuccessful()) {
    //                    Uri downloadUri = task.getResult();
    //                    updateProfilePhoto(downloadUri.toString());
    //                    calback.returnUrl(downloadUri.toString());
    //                    Toast.makeText(mActivity, "Photo uploaded successfully", Toast.LENGTH_LONG).show();
    //                } else {
    //                    Toast.makeText(mActivity, "Upload failed", Toast.LENGTH_SHORT).show();
    //                }
    //            }
    //        });
    //    }
    //
    //    public String getProfilePic(String userId, DataSnapshot ds) {
    //        String picture = "";
    //
    //        if (ds.hasChild(userId)) {
    //            for (DataSnapshot child : ds.getChildren()) {
    //                if (Objects.requireNonNull(child.getKey()).equals(userId))
    //                    picture = Objects.requireNonNull(Objects.requireNonNull(ds.getValue(UserAccountSettings.class)).getProfile_photo());
    //            }
    //        }
    //
    //        return picture;
    //
    //    }
    //    private void addNewUserData(String username, String displayName, String userCity, String subArea, UserLocation userLocation, String phoneNo, String bloodGroup, String profile_photo, boolean canDonate, boolean needBlood, UserDataAddedCallback callback) {
    //
    //        User user = new User(userID, username, phoneNo, bloodGroup, userCity, subArea, userLocation, canDonate, needBlood,0L);
    //        final UserAccountSettings settings = new UserAccountSettings(displayName,
    //                profile_photo, StringManipulation.condenseUserName(username).toLowerCase(Locale.getDefault()));
    //
    //        myRef.child(mActivity.getString(R.string.users_node))
    //                .child(userID)
    //                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
    //            @Override
    //            public void onComplete(@NonNull Task<Void> task) {
    //
    //                myRef.child(mActivity.getString(R.string.user_account_settings_node))
    //                        .child(userID)
    //                        .setValue(settings).addOnCompleteListener(new OnCompleteListener<Void>() {
    //                    @Override
    //                    public void onComplete(@NonNull Task<Void> task) {
    //
    ////                        if (!Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()) {
    ////                            sendVerificationEmail(true);
    //
    ////                        }
    //                        callback.dataAddedSuccess(true);
    //                        Log.d(TAG, "new user added");
    //                    }
    //                }).addOnFailureListener(new OnFailureListener() {
    //                    @Override
    //                    public void onFailure(@NonNull Exception e) {
    //                        callback.dataAddedSuccess(false);
    //                        Log.d(TAG, "new user add faild");
    //                    }
    //                });
    //
    //            }
    //        });
    //
    //    }
    //
    //
    //    public void addRequest(String reqId, String userId, String bloodGroup, String hospital, String description, boolean active, long created_at, long closed_at, String city, String area, UserLocation location, RequestPostCallback callback) {
    //
    //        String id = myRef.child(mActivity.getString(R.string.request_node))
    //                .push().getKey();
    //        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                Request request = new Request(id, userId, bloodGroup, hospital, description, active, created_at, closed_at, location, city, area);
    //                myRef.child(mActivity.getString(R.string.request_node))
    //                        .child(id)
    //                        .setValue(request).
    //                        addOnCompleteListener(new OnCompleteListener<Void>() {
    //                                                  @Override
    //                                                  public void onComplete(@NonNull Task<Void> task) {
    //
    //                                                      callback.success(true);
    //
    //
    //                                                  }
    //
    //                                              }
    //                        )
    //                        .addOnFailureListener(new OnFailureListener() {
    //                            @Override
    //                            public void onFailure(@NonNull Exception e) {
    //                                callback.success(false);
    //                            }
    //                        });
    //            }
    //        }, 1500);
    //
    //    }
    //
    //    public void updateRequest(String reqId, String userId, String bloodGroup, String hospital, String description, boolean active, long created_at, long closed_at, String city, String area, UserLocation location, RequestPostCallback callback) {
    //
    ////                    Toast.makeText(mActivity,"ke found \""+reqId+"\"",Toast.LENGTH_SHORT).show();
    ////                    Request request = new Request(reqId, userId, bloodGroup, hospital, description, active, created_at, closed_at, location, city, area);
    //        Map<String, Object> updates = new HashMap<String, Object>();
    //        updates.put("id", reqId);
    //        updates.put("userId", userId);
    //        updates.put("bloodGroup", bloodGroup);
    //        updates.put("hospital", hospital);
    //        updates.put("description", description);
    //        updates.put("active", active);
    //        updates.put("created_at", created_at);
    //        updates.put("closed_at", closed_at);
    //        updates.put("request_location", location);
    //        updates.put("city", city);
    //        updates.put("area", area);
    //
    //        myRef.child(mActivity.getString(R.string.request_node))
    //                .child(reqId)
    //                .updateChildren(updates)
    //                .addOnCompleteListener(new OnCompleteListener<Void>() {
    //                    @Override
    //                    public void onComplete(@NonNull Task<Void> task) {
    //                        callback.success(true);
    //                    }
    //                }).addOnFailureListener(new OnFailureListener() {
    //            @Override
    //            public void onFailure(@NonNull Exception e) {
    //                callback.success(false);
    //            }
    //        });
    //    }
    //
    //    public void checkRequestExist(String Id, RequestExistCallback callback) {
    //        Query query = myRef.child(mActivity.getString(R.string.request_node)).child(Id);
    //        query.addListenerForSingleValueEvent(new ValueEventListener() {
    //            @Override
    //            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    //                if (dataSnapshot.exists()) {
    ////                    Toast.makeText(mActivity,"Exists"+Id,Toast.LENGTH_SHORT).show();
    //                    callback.exists(true);
    //                } else {
    //                    callback.exists(false);
    ////                    Toast.makeText(mActivity,"Not Exist"+Id,Toast.LENGTH_SHORT).show();
    //                }
    //            }
    //
    //            @Override
    //            public void onCancelled(@NonNull DatabaseError error) {
    //                callback.exists(false);
    //
    //            }
    //        });
    //    }
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //    public void retriveRecipeList(DataSnapshot dataSnapshot) {
    //        Realm realm = Realm.getDefaultInstance();
    //
    //        ArrayList<RecipesModelClass> recipes = new ArrayList<>();
    //
    //        for (DataSnapshot ds : dataSnapshot.getChildren()) {
    //
    //            try {
    ////                Log.i("Object", "=====================>" + ds.getValue());
    //                try {
    //                    if (ds.exists() ) {
    //                        try{
    //
    //                            realm.executeTransaction(new Realm.Transaction() {
    //                                @Override
    //                                public void execute(Realm it) {
    //                                    float rating = 0;
    //                                    if(ds.child("r").exists()){
    //                                        rating = Integer.parseInt(ds.child("r").getValue().toString());
    //                                    }
    //
    //                                    float ratingNumber = 0;
    //                                    if(ds.child("nr").exists()) {
    //                                        ratingNumber = Integer.parseInt(ds.child("nr").getValue().toString());
    //                                    }
    //
    //                                    int categoryNumber = 0;
    //                                    if (ds.child("c").exists()) {
    //                                        categoryNumber = Integer.parseInt(ds.child("c").getValue().toString());
    //                                    }
    //
    //                                    int chefNumber = 0;
    //                                    if (ds.child("cn").exists()) {
    //                                        chefNumber = Integer.parseInt(ds.child("cn").getValue().toString());
    //                                    }
    //
    //                                    String tittle = ds.child("t").getValue().toString();
    //                                    String image = ds.child("i").getValue().toString();
    //                                    RecipesModelClass temp = new RecipesModelClass(image, tittle, rating, ratingNumber,categoryNumber,chefNumber);
    //                                    temp.setKey(Integer.parseInt(ds.getKey()));
    //                                    it.insertOrUpdate(temp);
    //                                }
    //                            });
    //                        }catch (Exception e ){e.printStackTrace();}
    //                    }
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //
    //            } catch (NullPointerException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //    }
    //
    //    public ArrayList<RecipesModelClass> retriveRecipesByRating(DataSnapshot dataSnapshot) {
    //
    //        ArrayList<RecipesModelClass> recipes = new ArrayList<>();
    //        for (DataSnapshot ds : dataSnapshot.getChildren()) {
    //            try {
    ////                Log.i("Object", "=====================>" + ds.getValue());
    //                try {
    //                    if (ds.exists() ) {
    //                        try{
    //                            float rating = 0;
    //                            if(ds.child("r").exists()){
    //                                rating = Integer.parseInt(ds.child("r").getValue().toString());
    //                            }
    //                            float ratingNumber = 0;
    //                            if(ds.child("nr").exists()) {
    //                                ratingNumber = Integer.parseInt(ds.child("nr").getValue().toString());
    //                            }
    //                            int categoryNumber = 0;
    //                            if (ds.child("c").exists()) {
    //                                categoryNumber = Integer.parseInt(ds.child("c").getValue().toString());
    //                            }
    //                            int chefNumber = 0;
    //                            if (ds.child("cn").exists()) {
    //                                chefNumber = Integer.parseInt(ds.child("cn").getValue().toString());
    //                            }
    //                            String tittle = ds.child("t").getValue().toString();
    //                            String image = ds.child("i").getValue().toString();
    //                            RecipesModelClass temp = new RecipesModelClass(image, tittle, rating, ratingNumber,categoryNumber,chefNumber);
    //                            temp.setKey(Integer.parseInt(ds.getKey()));
    //                            recipes.add(temp);
    //                        }catch (Exception e ){e.printStackTrace();}
    //                    }
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            } catch (NullPointerException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //        Collections.sort(recipes, RecipesModelClass.BY_RATING_DESCENDING);
    //
    //        return recipes;
    //    }
    //    public RecipesModelClass getnewRecipes(DataSnapshot ds){
    //
    //            try {
    //                if (ds.exists()) {
    //                    try {
    //                        float rating = 0;
    //                        if (ds.child("r").exists()) {
    //                            rating = Integer.parseInt(ds.child("r").getValue().toString());
    //                        }
    //                        float ratingNumber = 0;
    //                        if (ds.child("nr").exists()) {
    //                            ratingNumber = Integer.parseInt(ds.child("nr").getValue().toString());
    //                        }
    //                        int categoryNumber = 0;
    //                        if (ds.child("c").exists()) {
    //                            categoryNumber = Integer.parseInt(ds.child("c").getValue().toString());
    //                        }
    //                        int chefNumber = 0;
    //                        if (ds.child("cn").exists()) {
    //                            chefNumber = Integer.parseInt(ds.child("cn").getValue().toString());
    //                        }
    //                        String tittle = ds.child("t").getValue().toString();
    //                        String image = ds.child("i").getValue().toString();
    //                        RecipesModelClass temp = new RecipesModelClass(image, tittle, rating, ratingNumber,categoryNumber,chefNumber);
    //                        temp.setKey(Integer.parseInt(ds.getKey()));
    //
    //                        return temp;
    //                    } catch (Exception e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //
    //
    //        return null;
    //    }
    ////
    //    public Request retrieveUserRequest(DataSnapshot dataSnapshot) {
    //
    //        Request request = new Request();
    //
    //        for (DataSnapshot ds : dataSnapshot.getChildren()) {
    //
    //            try {
    ////                Toast.makeText(mActivity,ds.getValue().toString(),Toast.LENGTH_SHORT).show();
    //                Log.i("Object", "=====================>" + ds.getValue());
    //                try {
    //                    if (ds.exists() && Objects.requireNonNull(ds.getValue(Request.class)).getUserId().equals(userID) && Objects.requireNonNull(ds.getValue(Request.class)).isActive()) {
    //                        request = Objects.requireNonNull(ds.getValue(Request.class));
    //                    }
    //                } catch (Exception e) {
    //                    e.printStackTrace();
    //                }
    //            } catch (NullPointerException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //        return request;
    //    }
    //
    //    public void updateUser(User user, RequestPostCallback callback) {
    //
    //
    //        Map<String, Object> updates = new HashMap<String, Object>();
    //
    //        updates.put("donor", user.getDonor());
    //        updates.put("needBlood", user.getNeedBlood());
    //        updates.put("subArea", user.getSubArea());
    //        updates.put("userBloodGroup", user.getUserBloodGroup());
    //        updates.put(" userCity", user.getUserCity());
    //        updates.put("userId", user.getUserId());
    //        updates.put("userLocation", user.getUserLocation());
    //        updates.put("userName", user.getUserName());
    //        updates.put("userPhone", user.getUserPhone());
    //        updates.put("lastDonation",user.getLastDonation());
    //
    //
    //        myRef.child(mActivity.getString(R.string.users_node))
    //                .child(userID)
    //                .updateChildren(updates)
    //                .addOnCompleteListener(new OnCompleteListener<Void>() {
    //                    @Override
    //                    public void onComplete(@NonNull Task<Void> task) {
    //                        callback.success(true);
    //                    }
    //                }).addOnFailureListener(new OnFailureListener() {
    //            @Override
    //            public void onFailure(@NonNull Exception e) {
    //                callback.success(false);
    //            }
    //        });
    //    }
}