package com.spendsense.dashboard.projection;

import java.math.BigDecimal;

public interface MonthlyExpenseProjection {

    String getMonth();

    BigDecimal getAmount();

}
