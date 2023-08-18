package com.example.cartwithfirebase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.cartwithfirebase.adapter.MyDrinkAdapter;
import com.example.cartwithfirebase.eventbus.MyUpdateCartEvent;
import com.example.cartwithfirebase.listener.ICartLoadListener;
import com.example.cartwithfirebase.listener.IDrinkLoadlistener;
import com.example.cartwithfirebase.model.CartModel;
import com.example.cartwithfirebase.model.DrinkModel;
import com.example.cartwithfirebase.utils.SpaceItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.eventbus.EventBus;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.eventbus.Subscribe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements IDrinkLoadlistener, ICartLoadListener {
    @BindView(R.id.recycler_drink)
    RecyclerView recyclerDrink;

    @BindView(R.id.mainlayout)
    RecyclerView mainLayout;

    @BindView(R.id.badge)
    RecyclerView badge;

    @BindView(R.id.btnCart)
    RecyclerView btnCart;

    IDrinkLoadlistener drinkLoadlistener;
    ICartLoadListener cartLoadListener;
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);

    }
    protected void onStop() {
        if(EventBus.getDefault().hasSuscriberforEvent(MyUpdateCartEvent.class))
            EventBus.getDefault().removeStickyEvent(MyUpdateCartEvent.class);
        EventBus.getDefault().unregister(this);
        super.onStop();

    }
    @Subscribe(threadMode=ThreadMode.MAIN,sticky=true)
    public void onUpdateCart(MyUpdateCartEvent event){
        countCartItem();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout .activity_main);
        init();
        loadDrinkFromFirebase();
        countCartItem();

    }

    private void loadDrinkFromFirebase(){
        List<DrinkModel> drinkModels = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference("Drink")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot drinkSnapshot:snapshot.getChildren()){
                                DrinkModel drinkModel = drinkSnapshot.getValue(DrinkModel.class);
                                drinkModel.setKey(drinkSnapshot.getKey());
                                drinkModels.add(drinkModel);

                            }
                            drinkLoadlistener.onDrinkLoadSuccess(drinkModels);
                        }
                        else{
                            drinkLoadlistener.onDrinkLoadFailed("can't find drink");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        drinkLoadlistener.onDrinkLoadFailed(error.getMessage());

                    }
                });
    }
    private void init(){
        ButterKnife.bind(this);
        drinkLoadlistener =this;
        cartLoadListener = this;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        recyclerDrink.setLayoutManager(gridLayoutManager);
        recyclerDrink.addItemDecoration(new SpaceItemDecoration());

    }

    @Override
    public void onDrinkLoadSuccess(List<DrinkModel> drinkModelList) {
        MyDrinkAdapter adapter = new MyDrinkAdapter(this,drinkModelList,cartLoadListener);
        recyclerDrink.setAdapter(adapter);
    }

    @Override
    public void onDrinkLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();

    }

    @Override
    public void onCartLoadSuccess(List<CartModel> cartModelList) {
        int cartSum=0;
        for(CartModel cartModel: cartModelList)
            cartSum+=cartModel.getQuantity();
        badge.setNumber(cartSum);

    }


    @Override
    public void onCartLoadFailed(String message) {
        Snackbar.make(mainLayout,message,Snackbar.LENGTH_LONG).show();

    }
    protected void onResume(){
        super.onResume();;
        countCartItem();
    }

    private void countCartItem() {
        List<CartModel> cartModels = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Cart")
                .child("UNIQUE_USER_ID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot cartSnapshot:snapshot.getChildren()){
                            CartModel cartModel= cartSnapshot.getValue(CartModel.class);
                            cartModel.setKey(cartSnapshot.getKey());
                            cartModels.add(cartModel);

                        }
                        cartLoadListener.onCartLoadSuccess( cartModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        cartLoadListener.onCartLoadFailed(error.getMessage());

                    }
                });
    }
}