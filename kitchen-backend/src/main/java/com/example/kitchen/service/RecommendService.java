package com.example.kitchen.service;

import com.example.kitchen.dto.RecommendDishVO;

import java.util.List;

public interface RecommendService {
    List<RecommendDishVO> recommendByUserId(Integer userId, Integer limit);
}
