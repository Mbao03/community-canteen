package com.example.kitchen.service;

import com.example.kitchen.model.DishType;

import java.util.List;
import java.util.Map;


public interface DishTypeService {

    Integer getCount();

    Integer getSearchCount(Map<String, Object> params);

    List<DishType> searchDishTypesByPage(Map<String, Object> params);

    Integer addDishType(DishType DishType);

    Integer deleteDishType(DishType DishType);

    Integer deleteDishTypes(List<DishType> DishTypes);

    Integer updateDishType(DishType DishType);

    List<DishType> queryDishTypes();

}
