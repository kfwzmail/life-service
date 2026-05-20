package com.std.lifeService.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.std.lifeService.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    @Select("""
        SELECT type, COALESCE(SUM(amount), 0) AS total
        FROM bill
        WHERE user_id = #{userId} AND bill_time >= #{start} AND bill_time < #{end}
        GROUP BY type
    """)
    List<Map<String, Object>> sumByType(@Param("userId") Long userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Select("""
        SELECT c.id AS categoryId, c.name AS categoryName, c.icon,
               COALESCE(SUM(b.amount), 0) AS amount
        FROM bill b
        JOIN category c ON b.category_id = c.id
        WHERE b.user_id = #{userId} AND b.type = #{type}
          AND b.bill_time >= #{start} AND b.bill_time < #{end}
        GROUP BY c.id, c.name, c.icon
        ORDER BY amount DESC
    """)
    List<Map<String, Object>> sumByCategory(@Param("userId") Long userId,
                                             @Param("type") String type,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
