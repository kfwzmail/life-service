package com.std.lifeService.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.std.lifeService.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
