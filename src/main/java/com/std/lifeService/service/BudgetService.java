package com.std.lifeService.service;

import com.std.lifeService.vo.BudgetVO;
import java.util.List;

public interface BudgetService {
    List<BudgetVO> list(Long userId, String budgetMonth);
    void save(Long userId, String budgetMonth, List<BudgetVO> items);
    List<BudgetVO> comparison(Long userId, String budgetMonth);
}
