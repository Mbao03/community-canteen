package com.example.kitchen.web;

import com.example.kitchen.model.DishType;
import com.example.kitchen.service.DishTypeService;
import com.example.kitchen.utils.PageUtils;
import com.example.kitchen.utils.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/dishType")
public class DishTypeController {

    @Resource
    private DishTypeService DishTypeService;

    @GetMapping(value = "/getCount")
    public Integer getCount() {
        return DishTypeService.getCount();
    }

    @GetMapping(value = "/queryDishTypes")
    public List<DishType> queryDishTypes() {
        return DishTypeService.queryDishTypes();
    }

    @GetMapping(value = "/queryDishTypesByPage")
    public Map<String, Object> queryDishTypesByPage(@RequestParam Map<String, Object> params) {
        PageUtils.parsePageParams(params);
        int count = DishTypeService.getSearchCount(params);
        List<DishType> dishTypes = DishTypeService.searchDishTypesByPage(params);
        return R.getListResultMap(0, "success", count, dishTypes);
    }

    @PostMapping(value = "/addDishType")
    public Integer addDishType(@RequestBody DishType dishType) {
        return DishTypeService.addDishType(dishType);
    }

    @DeleteMapping(value = "/deleteDishType")
    public Integer deleteDishType(@RequestBody DishType dishType) {
        return DishTypeService.deleteDishType(dishType);
    }

    @DeleteMapping(value = "/deleteDishTypes")
    public Integer deleteDishTypes(@RequestBody List<DishType> dishTypes) {
        return DishTypeService.deleteDishTypes(dishTypes);
    }

    @PutMapping(value = "/updateDishType")
    public Integer updateDishType(@RequestBody DishType dishType) {
        return DishTypeService.updateDishType(dishType);
    }
}
