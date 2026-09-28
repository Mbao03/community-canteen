package com.example.kitchen.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecommendDishVO {
    private Integer dishId;
    private String dishName;
    private String chefName;
    private BigDecimal dishPrice;
    private Integer dishTypeId;
    private String dishTypeName;
    private String dishDesc;
    private Byte isReserved;
    private String dishImg;
    private Double score;
    private String reason;
}
