package com.example.kitchen.mapper;

import com.example.kitchen.model.DishInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface DishInfoMapper {


    int deleteByPrimaryKey(Integer dishId);

    int insert(DishInfo record);

    int insertSelective(DishInfo record);

    DishInfo selectByPrimaryKey(Integer dishId);

    int updateByPrimaryKeySelective(DishInfo record);

    int updateByPrimaryKey(DishInfo record);

    List<DishInfo> selectAllByLimit(@Param("begin") Integer begin, @Param("size") Integer size);


    Integer selectCount();

    int selectCountBySearch(Map<String, Object> searchParam);

    List<DishInfo> selectBySearch(Map<String, Object> searchParam);

    List<DishInfo> selectAll();

    int selectCountByType(Map<String, Object> map);

    List<DishInfo> selectByType(Map<String, Object> map);
}
