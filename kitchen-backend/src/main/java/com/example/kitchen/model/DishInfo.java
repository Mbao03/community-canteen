package com.example.kitchen.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DishInfo {

    @JsonProperty("dishId")
    private Integer dishId;

    @JsonProperty("dishName")
    private String dishName;

    @JsonProperty("chefName")
    private String chefName;

    @JsonProperty("dishPrice")
    private BigDecimal dishPrice;

    @JsonProperty("dishTypeId")
    private Integer dishTypeId;

    @JsonProperty("dishTypeName")
    private String dishTypeName;

    @JsonProperty("dishDesc")
    private String dishDesc;

    @JsonProperty("isReserved")
    private Byte isReserved;

    @JsonProperty("dishImg")
    private String dishImg;

    private String kitchenName;

    @JsonProperty("stockQty")
    private Integer stockQty;

    @JsonProperty("isAvailable")
    private Byte isAvailable;

    private Integer prepMinutes;
    private Byte spiceLevel;
    private String allergens;
    private BigDecimal caloriesKcal;
    private BigDecimal proteinG;
    private BigDecimal fatG;
    private BigDecimal carbG;
    private Date createdAt;
    private Date updatedAt;
}
