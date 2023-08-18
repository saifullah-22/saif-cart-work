package com.example.cartwithfirebase.listener;

import com.example.cartwithfirebase.model.DrinkModel;

import java.util.List;

public interface IDrinkLoadlistener {
    void onDrinkLoadSuccess(List<DrinkModel> drinkModelList);
    void onDrinkLoadFailed(String message);
}
