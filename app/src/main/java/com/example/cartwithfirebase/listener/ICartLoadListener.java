package com.example.cartwithfirebase.listener;

import com.example.cartwithfirebase.model.CartModel;
import com.example.cartwithfirebase.model.DrinkModel;

import java.util.List;

public interface ICartLoadListener {
    void onCartLoadSuccess(List<CartModel> cartModelList);
    void onCartLoadFailed(String message);
}
