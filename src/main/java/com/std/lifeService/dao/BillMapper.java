package com.std.lifeService.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.std.lifeService.entity.Bill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 账单 Mapper
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    /**
     * 按账单类型汇总金额（支出/收入）
     */
    @Select("""
        SELECT type, COALESCE(SUM(amount), 0) AS total
        FROM bill
        WHERE user_id = #{userId}
          AND bill_time >= STR_TO_DATE(#{start}, '%Y-%m-%d %H:%i:%s')
          AND bill_time < STR_TO_DATE(#{end}, '%Y-%m-%d %H:%i:%s')
        GROUP BY type
    """)
    List<Map<String, Object>> sumByType(@Param("userId") Long userId,
                                         @Param("start") String start,
                                         @Param("end") String end);

    /**
     * 按分类汇总账单金额（支出或收入），关联分类表获取名称和图标
     */
    @Select("""
        SELECT c.id AS categoryId, c.name AS categoryName, c.icon,
               COALESCE(SUM(b.amount), 0) AS amount
        FROM bill b
        JOIN category c ON b.category_id = c.id
        WHERE b.user_id = #{userId} AND b.type = #{type}
          AND b.bill_time >= STR_TO_DATE(#{start}, '%Y-%m-%d %H:%i:%s')
          AND b.bill_time < STR_TO_DATE(#{end}, '%Y-%m-%d %H:%i:%s')
        GROUP BY c.id, c.name, c.icon
        ORDER BY amount DESC
    """)
    List<Map<String, Object>> sumByCategory(@Param("userId") Long userId,
                                             @Param("type") String type,
                                             @Param("start") String start,
                                             @Param("end") String end);

    /**
     * 按天汇总账单金额（支出/收入分组）
     */
    @Select("""
        SELECT DATE(bill_time) AS date, type, COALESCE(SUM(amount), 0) AS total
        FROM bill
        WHERE user_id = #{userId}
          AND bill_time >= STR_TO_DATE(#{start}, '%Y-%m-%d %H:%i:%s')
          AND bill_time < STR_TO_DATE(#{end}, '%Y-%m-%d %H:%i:%s')
        GROUP BY DATE(bill_time), type
        ORDER BY date
    """)
    List<Map<String, Object>> sumByDay(@Param("userId") Long userId,
                                        @Param("start") String start,
                                        @Param("end") String end);

    /**
     * 按月汇总账单金额（支出/收入分组），用于年度统计
     */
    @Select("""
        SELECT DATE_FORMAT(bill_time, '%Y-%m') AS month, type,
               COALESCE(SUM(amount), 0) AS total
        FROM bill
        WHERE user_id = #{userId}
          AND bill_time >= STR_TO_DATE(#{start}, '%Y-%m-%d %H:%i:%s')
          AND bill_time < STR_TO_DATE(#{end}, '%Y-%m-%d %H:%i:%s')
        GROUP BY DATE_FORMAT(bill_time, '%Y-%m'), type
        ORDER BY month
    """)
    List<Map<String, Object>> sumByMonth(@Param("userId") Long userId,
                                          @Param("start") String start,
                                          @Param("end") String end);
}
