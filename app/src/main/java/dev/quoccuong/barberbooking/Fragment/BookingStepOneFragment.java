package dev.quoccuong.barberbooking.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.quoccuong.barberbooking.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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
import dev.quoccuong.barberbooking.Adapter.MySalonAdapter;
import dev.quoccuong.barberbooking.Common.SpacesItemDecoration;
import dev.quoccuong.barberbooking.Interface.IAllSalonsLoadListener;
import dev.quoccuong.barberbooking.Interface.IBranchLoadListener;
import dev.quoccuong.barberbooking.Model.Salon;
import dmax.dialog.SpotsDialog;

public class BookingStepOneFragment extends Fragment implements IAllSalonsLoadListener, IBranchLoadListener {

    static BookingStepOneFragment instance;

    CollectionReference allSalonsRef;
    CollectionReference branchRef;

    IAllSalonsLoadListener iAllSalonsLoadListener;
    IBranchLoadListener iBranchLoadListener;

    @BindView(R.id.spinner)
    MaterialSpinner spinner;
    @BindView(R.id.recycler_salon)
    RecyclerView recyclerSalon;

    Unbinder unbinder;
    AlertDialog dialog;

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
        iBranchLoadListener = this;

        dialog = new SpotsDialog.Builder().setContext(getActivity()).build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_one, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        initView();
        loadAllSalon();

        return itemView;
    }

    private void initView() {
        recyclerSalon.setHasFixedSize(true);
        recyclerSalon.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerSalon.addItemDecoration(new SpacesItemDecoration(10));
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
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0)
                    loadBranchOfCity(item.toString());
                else
                    recyclerSalon.setVisibility(View.GONE);
            }
        });
    }

    private void loadBranchOfCity(String cityName) {
        dialog.show();

        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(cityName)
                .collection("Branch");

        branchRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Salon> salons = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        salons.add(documentSnapshot.toObject(Salon.class));
                    }
                    iBranchLoadListener.onBranchLoadSuccess(salons);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBranchLoadListener.onBranchLoadFailed(e.getMessage());
            }
        });
    }

    @Override
    public void onAllSalonsFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> salons) {
        MySalonAdapter adapter = new MySalonAdapter(getActivity(), salons);
        recyclerSalon.setAdapter(adapter);
        recyclerSalon.setVisibility(View.VISIBLE);
        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
