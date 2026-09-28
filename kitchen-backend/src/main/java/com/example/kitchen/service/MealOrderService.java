package com.example.kitchen.service;

import com.example.kitchen.model.MealOrder;

import java.util.List;
import java.util.Map;

public interface MealOrderService {
    Integer getCount();

    Integer getSearchCount(Map<String, Object> params);

    List<MealOrder> searchMealOrdersByPage(Map<String, Object> params);

    Integer addMealOrder(MealOrder MealOrder);

    Integer addMealOrder2(MealOrder MealOrder);

    Integer deleteMealOrder(MealOrder MealOrder);

    Integer deleteMealOrders(List<MealOrder> MealOrders);

    Integer updateMealOrder(MealOrder MealOrder);

    Integer updateMealOrder2(MealOrder MealOrder);

    MealOrder queryMealOrdersById(Integer MealOrderid);
}
