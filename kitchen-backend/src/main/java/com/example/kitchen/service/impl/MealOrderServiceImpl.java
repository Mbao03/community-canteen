package com.example.kitchen.service.impl;

import com.example.kitchen.mapper.MealOrderMapper;
import com.example.kitchen.model.MealOrder;
import com.example.kitchen.service.MealOrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
public class MealOrderServiceImpl implements MealOrderService {

    @Resource
    private MealOrderMapper mealOrderMapper;

    @Override
    public Integer getCount() {
        return mealOrderMapper.selectCount();
    }

    @Override
    public Integer getSearchCount(Map<String, Object> params) {
        return mealOrderMapper.selectCountBySearch(params);
    }

    @Override
    public List<MealOrder> searchMealOrdersByPage(Map<String, Object> params) {
        List<MealOrder> orders = mealOrderMapper.selectBySearch(params);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (MealOrder order : orders) {
            if (order.getMealOrdertime() != null) {
                order.setMealOrdertimestr(sdf.format(order.getMealOrdertime()));
            }
            if (order.getReturntime() != null) {
                order.setReturntimestr(sdf.format(order.getReturntime()));
            }
        }
        return orders;
    }

    @Override
    public Integer addMealOrder(MealOrder order) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (order.getMealOrdertimestr() != null && !order.getMealOrdertimestr().trim().isEmpty()) {
                order.setMealOrdertime(sdf.parse(order.getMealOrdertimestr()));
            }
            if (order.getReturntimestr() != null && !order.getReturntimestr().trim().isEmpty()) {
                order.setReturntime(sdf.parse(order.getReturntimestr()));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return mealOrderMapper.insertSelective(order);
    }

    @Override
    public Integer addMealOrder2(MealOrder order) {
        return mealOrderMapper.insertSelective(order);
    }

    @Override
    public Integer deleteMealOrder(MealOrder order) {
        MealOrder db = mealOrderMapper.selectByPrimaryKey(order.getMealOrderid());
        if (db == null || db.getReturntime() == null) {
            return 0;
        }
        return mealOrderMapper.deleteByPrimaryKey(order.getMealOrderid());
    }

    @Override
    public Integer deleteMealOrders(List<MealOrder> orders) {
        int count = 0;
        for (MealOrder order : orders) {
            count += deleteMealOrder(order);
        }
        return count;
    }

    @Override
    public Integer updateMealOrder(MealOrder order) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (order.getMealOrdertimestr() != null && !order.getMealOrdertimestr().trim().isEmpty()) {
                order.setMealOrdertime(sdf.parse(order.getMealOrdertimestr()));
            }
            if (order.getReturntimestr() != null && !order.getReturntimestr().trim().isEmpty()) {
                order.setReturntime(sdf.parse(order.getReturntimestr()));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return mealOrderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public Integer updateMealOrder2(MealOrder order) {
        return mealOrderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public MealOrder queryMealOrdersById(Integer mealOrderId) {
        return mealOrderMapper.selectByPrimaryKey(mealOrderId);
    }
}
