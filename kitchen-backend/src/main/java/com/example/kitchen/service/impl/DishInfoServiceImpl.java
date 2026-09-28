package com.example.kitchen.service.impl;

import com.example.kitchen.mapper.DishInfoMapper;
import com.example.kitchen.mapper.MealOrderMapper;
import com.example.kitchen.model.DishInfo;
import com.example.kitchen.service.DishInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DishInfoServiceImpl implements DishInfoService {

    @Resource
    private DishInfoMapper DishInfoMapper;

    @Resource
    private MealOrderMapper MealOrderMapper;

    @Override
    public Integer getCount() {
        return DishInfoMapper.selectCount();
    }

    @Override
    public List<DishInfo> queryDishInfos() {
        return DishInfoMapper.selectAll();
    }

    @Override
    public DishInfo queryDishInfoById(Integer dishId) {
        return DishInfoMapper.selectByPrimaryKey(dishId);
    }

    @Override
    public Integer getSearchCount(Map<String, Object> params) {
        return DishInfoMapper.selectCountBySearch(params);
    }

    @Override
    public List<DishInfo> searchDishInfosByPage(Map<String, Object> params) {
        return DishInfoMapper.selectBySearch(params);
    }

    @Override
    public Integer addDishInfo(DishInfo DishInfo) {
        return DishInfoMapper.insertSelective(DishInfo);
    }

    @Override
    public Integer deleteDishInfo(DishInfo DishInfo) {
        int count = 0;
        try{
            Map<String, Object> map = new HashMap<>();
            map.put("dishId", DishInfo.getDishId());
            if(MealOrderMapper.selectCountBySearch(map) > 0) {
                return -1;
            }
            count = DishInfoMapper.deleteByPrimaryKey(DishInfo.getDishId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Integer deleteDishInfos(List<DishInfo> DishInfos) {
        int count = 0;
        for(DishInfo DishInfo : DishInfos) {
            count += deleteDishInfo(DishInfo);
        }
        return count;
    }

    @Override
    public Integer updateDishInfo(DishInfo DishInfo) {
        return DishInfoMapper.updateByPrimaryKeySelective(DishInfo);
    }

}
