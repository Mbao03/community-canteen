package com.example.kitchen.web;

import com.example.kitchen.model.DishInfo;
import com.example.kitchen.service.DishInfoService;
import com.example.kitchen.utils.PageUtils;
import com.example.kitchen.utils.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/dishInfo")
public class DishInfoController {

    @Resource
    private DishInfoService DishInfoService;

    @GetMapping(value = "/getCount")
    public Integer getCount() {
        return DishInfoService.getCount();
    }

    @GetMapping(value = "/queryDishes")
    public List<DishInfo> queryDishes() {
        return DishInfoService.queryDishInfos();
    }

    @GetMapping(value = "/queryDishesByPage")
    public Map<String, Object> queryDishesByPage(@RequestParam Map<String, Object> params) {
        PageUtils.parsePageParams(params);
        int count = DishInfoService.getSearchCount(params);
        List<DishInfo> dishes = DishInfoService.searchDishInfosByPage(params);
        return R.getListResultMap(0, "success", count, dishes);
    }

    @PostMapping(value = "/addDish")
    public Integer addDish(@RequestBody DishInfo dish) {
        return DishInfoService.addDishInfo(dish);
    }

    @DeleteMapping(value = "/deleteDish")
    public Integer deleteDish(@RequestBody DishInfo dish) {
        return DishInfoService.deleteDishInfo(dish);
    }

    @DeleteMapping(value = "/deleteDishes")
    public Integer deleteDishes(@RequestBody List<DishInfo> dishes) {
        return DishInfoService.deleteDishInfos(dishes);
    }

    @PutMapping(value = "/updateDish")
    public Integer updateDish(@RequestBody DishInfo dish) {
        return DishInfoService.updateDishInfo(dish);
    }
}
