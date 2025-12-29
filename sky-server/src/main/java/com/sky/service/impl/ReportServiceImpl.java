package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

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
}
