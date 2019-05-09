package dev.quoccuong.barberbooking.Fragment;

import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dev.quoccuong.barberbooking.Interface.IAllSalonsLoadListener;

public class BookingStepOneFragment extends Fragment implements IAllSalonsLoadListener {

    static BookingStepOneFragment instance;

    CollectionReference allSalonsRef;
    CollectionReference branchRef;

    IAllSalonsLoadListener iAllSalonsLoadListener;

    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    @BindView(R.id.recycler_salon)
    RecyclerView recyclerSalon;

    Unbinder unbinder;

    public static BookingStepOneFragment getInstance() {
        if (instance == null)
            instance = new BookingStepOneFragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        allSalonsRef = FirebaseFirestore.getInstance().collection("AllSalon");
        iAllSalonsLoadListener = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_one, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        loadAllSalon();

        return itemView;
    }

    private void loadAllSalon() {
        allSalonsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> list = new ArrayList<>();
                            list.add("Please choose city");
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                list.add(documentSnapshot.getId());
                            }
                            iAllSalonsLoadListener.onAllSalonsLoadSuccess(list);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAllSalonsLoadListener.onAllSalonsFailed(e.getMessage());
            }
        });
    }

    @Override
    public void onAllSalonsLoadSuccess(List<String> areaNames) {
        spinner.setItems(areaNames);
    }

    @Override
    public void onAllSalonsFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
