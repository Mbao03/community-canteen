package com.example.kitchen.mapper;

import com.example.kitchen.model.DishType;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface DishTypeMapper {

    int deleteByPrimaryKey(Integer DishTypeid);

    int insert(DishType record);

    int insertSelective(DishType record);

    DishType selectByPrimaryKey(Integer DishTypeid);

    int updateByPrimaryKeySelective(DishType record);

    int updateByPrimaryKey(DishType record);

    List<DishType> selectAllByLimit(@Param("begin") Integer begin, @Param("size") Integer size);

    Integer selectCount();

    List<DishType> selectAll();

    int selectCountBySearch(Map<String, Object> searchParam);

    List<DishType> selectBySearch(Map<String, Object> searchParam);
}
