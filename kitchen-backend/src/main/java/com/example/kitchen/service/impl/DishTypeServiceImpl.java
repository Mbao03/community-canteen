package com.example.kitchen.service.impl;

import com.example.kitchen.mapper.DishInfoMapper;
import com.example.kitchen.mapper.DishTypeMapper;
import com.example.kitchen.model.DishType;
import com.example.kitchen.service.DishTypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DishTypeServiceImpl implements DishTypeService {

    @Resource
    private DishTypeMapper DishTypeMapper;

    @Resource
    private DishInfoMapper DishInfoMapper;

    @Override
    public Integer getCount() {
        return DishTypeMapper.selectCount();
    }

    @Override
    public Integer getSearchCount(Map<String, Object> params) {
        return DishTypeMapper.selectCountBySearch(params);
    }

    @Override
    public List<DishType> searchDishTypesByPage(Map<String, Object> params) {
        return DishTypeMapper.selectBySearch(params);
    }

    @Override
    public Integer addDishType(DishType DishType) {
        return DishTypeMapper.insertSelective(DishType);
    }

    @Override
    public Integer deleteDishType(DishType DishType) {
        int count = 0;
        try{
            Map<String, Object> map = new HashMap<>();
            map.put("DishTypeid", DishType.getDishTypeid());
            if(DishInfoMapper.selectCountByType(map) > 0) {
                return -1;
            }
            count = DishTypeMapper.deleteByPrimaryKey(DishType.getDishTypeid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Integer deleteDishTypes(List<DishType> DishTypes) {
        int count = 0;
        for(DishType DishType : DishTypes) {
            count += deleteDishType(DishType);
        }
        return count;
    }

    @Override
    public Integer updateDishType(DishType DishType) {
        return DishTypeMapper.updateByPrimaryKeySelective(DishType);
    }

    @Override
    public List<DishType> queryDishTypes() {
        return DishTypeMapper.selectAll();
    }
}
