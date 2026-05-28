package com.std.lifeService.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.std.lifeService.entity.Budget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预算 Mapper
 */
@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {
}
