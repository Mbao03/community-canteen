package com.example.kitchen.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MealOrder {

    @JsonProperty("orderId")
    @JsonAlias("MealOrderid")
    private Integer MealOrderid;

    @JsonProperty("residentId")
    @JsonAlias("userid")
    private Integer userid;

    @JsonProperty("residentName")
    @JsonAlias("username")
    private String username;

    @JsonProperty("dishId")
    private Integer dishId;

    @JsonProperty("dishName")
    private String dishName;

    @JsonProperty("orderTime")
    @JsonAlias("MealOrdertime")
    private Date MealOrdertime;

    @JsonProperty("orderTimeStr")
    @JsonAlias("MealOrdertimestr")
    private String MealOrdertimestr;

    @JsonProperty("completeTime")
    @JsonAlias("returntime")
    private Date returntime;

    @JsonProperty("completeTimeStr")
    @JsonAlias("returntimestr")
    private String returntimestr;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String orderStatus;
    private Date mealDate;
    private String mealSlot;
    private String deliveryType;
    private String pickupCode;
    private String contactPhone;
    private String deliveryAddress;
    private String remark;
    private String payStatus;
    private Date paidAt;
    private String cancelReason;
    private Date createdAt;
    private Date updatedAt;
}
