package dev.quoccuong.barberbooking;

import android.app.AlertDialog;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.quoccuong.barberbooking.Common.Common;
import dev.quoccuong.barberbooking.Fragment.HomeFragment;
import dev.quoccuong.barberbooking.Fragment.ShoppingFragment;
import dev.quoccuong.barberbooking.Model.User;
import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    BottomSheetDialog bottomSheetDialog;

    CollectionReference userRef;

    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(HomeActivity.this);

        // init
        userRef = FirebaseFirestore.getInstance().collection("User");
        loadingDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        // Check intent, if isLogin = true, enable full access
        // If isLogin = false, just let user around shopping to view
        if (getIntent() != null) {
            boolean isLogin = getIntent().getBooleanExtra(Common.IS_LOGIN, false);

            if (isLogin) {
                loadingDialog.show();
                // check if user is exists
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {
                        if (account != null) {
                            DocumentReference currentUser = userRef.document(account.getPhoneNumber().toString());
                            currentUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot userSnapshot = task.getResult();
                                        if (!userSnapshot.exists()) {
                                            showUpdateDialog(account.getPhoneNumber().toString());
                                        }
                                        dismissLoadingDialog();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Toast.makeText(HomeActivity.this, "" + accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        // View
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_home)
                    fragment = new HomeFragment();
                else if (menuItem.getItemId() == R.id.action_shopping)
                    fragment = new ShoppingFragment();
                return loadFragment(fragment);
            }
        });

        // set default home
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private void dismissLoadingDialog() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    private void showUpdateDialog(final String phoneNumber) {
        // init loadingDialog
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setTitle("One more step!");
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_update_information, null);

        Button btnUpdate = sheetView.findViewById(R.id.btn_update);
        final TextInputEditText edtName = sheetView.findViewById(R.id.edt_name);
        final TextInputEditText edtAddress = sheetView.findViewById(R.id.edt_address);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = new User(edtName.getText().toString(), edtAddress.getText().toString(), phoneNumber);
                userRef.document(phoneNumber)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                bottomSheetDialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Thank you", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                bottomSheetDialog.dismiss();
                                Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }
}
