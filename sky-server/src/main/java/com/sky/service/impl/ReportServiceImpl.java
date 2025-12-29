package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库，获取营业数据（查询最近30天的运营数据）
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        // 获取概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 通过POI将数据写入Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            // 基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            // 获取第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            // 获取第3行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i ++) {
                row = sheet.getRow(i + 7);
                LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(dateBegin, LocalTime.MAX);
                // 查询某一天的营业数据
                BusinessDataVO data = workspaceService.getBusinessData(begin, end);

                // 填充数据
                row.getCell(1).setCellValue(String.valueOf(dateBegin));
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());

                dateBegin = dateBegin.plusDays(1);
            }

            // 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            // 关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
