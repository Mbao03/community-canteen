package com.example.kitchen.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DishType {

    @JsonProperty("dishTypeId")
    @JsonAlias("DishTypeid")
    private Integer DishTypeid;

    @JsonProperty("dishTypeName")
    @JsonAlias("DishTypename")
    private String DishTypename;

    @JsonProperty("dishTypeDesc")
    @JsonAlias("DishTypedesc")
    private String DishTypedesc;
}
