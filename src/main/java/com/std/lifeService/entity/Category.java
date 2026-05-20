package com.std.lifeService.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private String icon;
    private Integer sortOrder;
    private Integer isDefault;
    private Long userId;
    private LocalDateTime createdAt;
}
