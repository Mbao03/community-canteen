package com.example.kitchen.service;

import com.example.kitchen.model.DishInfo;

import java.util.List;
import java.util.Map;


public interface DishInfoService {


    Integer getCount();


    List<DishInfo> queryDishInfos();

    DishInfo queryDishInfoById(Integer dishId);


    Integer getSearchCount(Map<String, Object> params);


    List<DishInfo> searchDishInfosByPage(Map<String, Object> params);


    Integer addDishInfo(DishInfo DishInfo);


    Integer deleteDishInfo(DishInfo DishInfo);


    Integer deleteDishInfos(List<DishInfo> DishInfos);


    Integer updateDishInfo(DishInfo DishInfo);
}
