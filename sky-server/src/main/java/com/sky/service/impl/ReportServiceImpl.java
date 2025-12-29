package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用于存放日期
        List<String> dates = new ArrayList<>();
        // 当前集合用于存放营业额数据，营业额是指：状态为“已完成”的订单金额合计
        List<Double> turnovers = new ArrayList<>();
        do {
            dates.add(begin.toString());
            LocalDateTime time1 = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime time2 = LocalDateTime.of(begin, LocalTime.MAX);
            Double sum = orderMapper.getByDate(time1, time2);
            if (sum == null) {
                turnovers.add(0.0);
            } else {
                turnovers.add(sum);
            }
            begin = begin.plusDays(1);
        } while (!begin.isAfter(end));

        String dateList = StringUtils.join(dates, ",");
        String turnoverList = StringUtils.join(turnovers, ",");

        TurnoverReportVO report = new TurnoverReportVO();
        report.setDateList(dateList);
        report.setTurnoverList(turnoverList);

        return report;
    }

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // dateList
        List<String> dates = new ArrayList<>();
        // totalUserList
        List<Integer> totals = new ArrayList<>();
        // newUserList
        List<Integer> newUsers = new ArrayList<>();

        do {
            dates.add(begin.toString());

            LocalDateTime time1 = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime time2 = LocalDateTime.of(begin, LocalTime.MAX);

            Integer total = userMapper.getCountByDate(null, time2);
            if (total == null) {
                total = 0;
            }

            Integer newUser = userMapper.getCountByDate(time1, time2);
            if (newUser == null) {
                newUser = 0;
            }

            totals.add(total);
            newUsers.add(newUser);

            begin = begin.plusDays(1);
        } while (!begin.isAfter(end));

        String dateList = StringUtils.join(dates, ",");
        String totalList = StringUtils.join(totals, ",");
        String newUserList = StringUtils.join(newUsers, ",");

        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setNewUserList(newUserList);
        userReportVO.setTotalUserList(totalList);
        userReportVO.setDateList(dateList);

        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //日期dateList
        List<String> dates = new ArrayList<>();
        //每日订单数orderCountList
        List<Integer> totals = new ArrayList<>();
        //每日有效订单数validOrderCountList
        List<Integer> validOrders = new ArrayList<>();

        //订单总数totalOrderCount
        Integer totalOrderCount = 0;
        //有效订单数validOrderCount
        Integer validOrderCount = 0;

        do {
            dates.add(begin.toString());
            LocalDateTime time1 = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime time2 = LocalDateTime.of(begin, LocalTime.MAX);
            Integer count = orderMapper.getCountByDate(time1, time2, null);
            Integer valid = orderMapper.getCountByDate(time1, time2, Orders.COMPLETED);

            if (count == null) {
                count = 0;
            }

            if (valid == null) {
                valid = 0;
            }

            totals.add(count);
            validOrders.add(valid);

            totalOrderCount += count;
            validOrderCount += valid;

            begin = begin.plusDays(1);
        } while (!begin.isAfter(end));

        String dateList = StringUtils.join(dates, ",");
        String orderCountList = StringUtils.join(totals, ",");
        String validOrderCountList = StringUtils.join(validOrders, ",");

        //订单完成率orderCompletionRate
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount * 1.0 / totalOrderCount;
        }

        OrderReportVO report = OrderReportVO.builder()
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(orderCountList)
                .validOrderCount(validOrderCount)
                .dateList(dateList)
                .validOrderCountList(validOrderCountList)
                .totalOrderCount(totalOrderCount).build();

        return report;
    }

    /**
     * 统计指定时间区间内销量排名Top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        // 商品名称列表nameList
        List<String> names = new ArrayList<>();
        // 销量列表numberList
        List<Integer> numbers = new ArrayList<>();

        List<GoodsSalesDTO> list = orderDetailMapper.getNameAndCount(begin, end, Orders.COMPLETED);
        for (GoodsSalesDTO goodsSalesDTO : list) {
            names.add(goodsSalesDTO.getName());
            numbers.add(goodsSalesDTO.getNumber());
        }

        String nameList = StringUtils.join(names, ",");
        String numberList = StringUtils.join(numbers, ",");

        SalesTop10ReportVO report = SalesTop10ReportVO.builder().nameList(nameList).numberList(numberList).build();

        return report;
    }
}
