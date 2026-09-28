package com.example.kitchen.mapper;

import com.example.kitchen.model.MealOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MealOrderMapper {
    int deleteByPrimaryKey(Integer MealOrderid);

    int insert(MealOrder record);

    int insertSelective(MealOrder record);

    MealOrder selectByPrimaryKey(Integer MealOrderid);

    int updateByPrimaryKeySelective(MealOrder record);

    int updateByPrimaryKey(MealOrder record);

    List<MealOrder> selectAllByLimit(@Param("begin") Integer begin, @Param("size") Integer size);

    Integer selectCount();

    int selectCountBySearch(Map<String, Object> searchParam);

    List<MealOrder> selectBySearch(Map<String, Object> searchParam);

    Integer selectCountByReader(Integer userid);

    List<MealOrder> selectAllByLimitByReader(@Param("begin") Integer begin, @Param("size") Integer size, @Param("userid") Integer userid);

    List<MealOrder> selectAllForRecommend();
}
