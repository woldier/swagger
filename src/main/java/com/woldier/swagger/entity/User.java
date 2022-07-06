package com.woldier.swagger.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "用户实体" ,description = "用户实体desc")
public class User {

    @ApiModelProperty(value = "主键")
    private int id;
    @ApiModelProperty(value = "名字")
    private String name;
    @ApiModelProperty(value = "年龄")
    private int age;
    @ApiModelProperty(value = "地址")
    private String address;
}