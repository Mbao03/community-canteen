package com.example.kitchen.service.impl;

import com.example.kitchen.dto.RecommendDishVO;
import com.example.kitchen.mapper.DishInfoMapper;
import com.example.kitchen.mapper.MealOrderMapper;
import com.example.kitchen.model.DishInfo;
import com.example.kitchen.model.MealOrder;
import com.example.kitchen.service.RecommendService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Resource
    private DishInfoMapper dishInfoMapper;

    @Resource
    private MealOrderMapper mealOrderMapper;

    @Override
    public List<RecommendDishVO> recommendByUserId(Integer userId, Integer limit) {
        int size = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);
        List<DishInfo> allDishes = dishInfoMapper.selectAll();
        List<MealOrder> allOrders = mealOrderMapper.selectAllForRecommend();
        if (allDishes == null || allDishes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, DishInfo> dishMap = allDishes.stream()
                .filter(d -> d.getDishId() != null)
                .collect(Collectors.toMap(DishInfo::getDishId, d -> d, (a, b) -> a));

        Set<Integer> userOrderedDishIds = allOrders.stream()
                .filter(o -> Objects.equals(userId, o.getUserid()))
                .map(MealOrder::getDishId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, Long> popularity = allOrders.stream()
                .map(MealOrder::getDishId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        if (userOrderedDishIds.isEmpty()) {
            return allDishes.stream()
                    .filter(this::isDishOrderable)
                    .sorted(Comparator.<DishInfo, Long>comparing(d -> popularity.getOrDefault(d.getDishId(), 0L)).reversed())
                    .limit(size)
                    .map(d -> toVO(d, popularity.getOrDefault(d.getDishId(), 0L), "社区热门菜品"))
                    .collect(Collectors.toList());
        }

        Map<Integer, Integer> typePreference = new HashMap<>();
        for (Integer dishId : userOrderedDishIds) {
            DishInfo dish = dishMap.get(dishId);
            if (dish != null && dish.getDishTypeId() != null) {
                typePreference.merge(dish.getDishTypeId(), 1, Integer::sum);
            }
        }

        List<ScoredDish> scored = new ArrayList<>();
        for (DishInfo candidate : allDishes) {
            Integer candidateId = candidate.getDishId();
            if (candidateId == null || userOrderedDishIds.contains(candidateId) || !isDishOrderable(candidate)) {
                continue;
            }

            double typeScore = typePreference.getOrDefault(candidate.getDishTypeId(), 0) * 2.0;
            double contentScore = computeContentScore(candidate, userOrderedDishIds, dishMap);
            double hotScore = popularity.getOrDefault(candidateId, 0L) * 0.3;
            double finalScore = typeScore + contentScore + hotScore;

            String reason = typeScore > 0 ? "符合您偏好的菜品分类" : "相似口味+当前热门";
            scored.add(new ScoredDish(candidate, finalScore, reason));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredDish::getScore).reversed())
                .limit(size)
                .map(item -> toVO(item.dish, item.score, item.reason))
                .collect(Collectors.toList());
    }

    private boolean isDishOrderable(DishInfo dish) {
        if (dish == null) {
            return false;
        }
        boolean available = dish.getIsAvailable() == null || dish.getIsAvailable() == 1;
        boolean hasStock = dish.getStockQty() == null || dish.getStockQty() > 0;
        return available && hasStock;
    }

    private double computeContentScore(DishInfo candidate, Set<Integer> orderedDishIds, Map<Integer, DishInfo> dishMap) {
        Set<String> candidateTokens = tokenize(candidate);
        if (candidateTokens.isEmpty()) {
            return 0;
        }

        double maxSim = 0;
        for (Integer orderedId : orderedDishIds) {
            DishInfo orderedDish = dishMap.get(orderedId);
            if (orderedDish == null) {
                continue;
            }
            Set<String> orderedTokens = tokenize(orderedDish);
            if (orderedTokens.isEmpty()) {
                continue;
            }
            double sim = jaccard(candidateTokens, orderedTokens);
            if (sim > maxSim) {
                maxSim = sim;
            }
        }
        return maxSim * 10.0;
    }

    private Set<String> tokenize(DishInfo dish) {
        String text = safe(dish.getDishName()) + " " + safe(dish.getChefName()) + " " + safe(dish.getDishTypeName()) + " " + safe(dish.getDishDesc());
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9\\u4e00-\\u9fa5]+"))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toSet());
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    private RecommendDishVO toVO(DishInfo dish, double score, String reason) {
        RecommendDishVO vo = new RecommendDishVO();
        vo.setDishId(dish.getDishId());
        vo.setDishName(dish.getDishName());
        vo.setChefName(dish.getChefName());
        vo.setDishImg(dish.getDishImg());
        vo.setDishPrice(dish.getDishPrice());
        vo.setDishTypeName(dish.getDishTypeName());
        vo.setDishDesc(dish.getDishDesc());
        vo.setScore(Math.round(score * 100.0) / 100.0);
        vo.setReason(reason);
        return vo;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static class ScoredDish {
        private final DishInfo dish;
        private final double score;
        private final String reason;

        private ScoredDish(DishInfo dish, double score, String reason) {
            this.dish = dish;
            this.score = score;
            this.reason = reason;
        }

        public double getScore() {
            return score;
        }
    }
}
