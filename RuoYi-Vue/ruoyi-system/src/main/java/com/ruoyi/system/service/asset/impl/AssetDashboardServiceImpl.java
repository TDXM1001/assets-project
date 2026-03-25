package com.ruoyi.system.service.asset.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.vo.AssetDashboardQueryVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardStatusCountVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardSummaryVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTodoItemVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTodoVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTrendPointVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTrendStatVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTrendVo;
import com.ruoyi.system.mapper.asset.AssetDashboardMapper;
import com.ruoyi.system.service.asset.IAssetDashboardService;

/**
 * 资产看板服务实现
 */
@Service
public class AssetDashboardServiceImpl implements IAssetDashboardService
{
    private static final int DEFAULT_TREND_DAYS = 7;

    private static final String TREND_METRIC_EVENT = "EVENT";

    private static final String TREND_METRIC_ORDER = "ORDER";

    private static final String TREND_METRIC_INVENTORY = "INVENTORY";

    private static final String TREND_METRIC_DIFF = "DIFF";

    private static final String PERM_INFO_LIST = "asset:info:list";

    private static final String PERM_ORDER_LIST = "asset:order:list";

    private static final String PERM_INVENTORY_LIST = "asset:inventory:list";

    private static final String PERM_EVENT_LIST = "asset:event:list";

    @Autowired
    private AssetDashboardMapper assetDashboardMapper;

    @Autowired
    @Lazy
    private IAssetDashboardService assetDashboardServiceProxy;

    @Override
    public AssetDashboardSummaryVo selectDashboardSummary()
    {
        AssetDashboardQueryVo query = buildRecentQuery(DEFAULT_TREND_DAYS);
        AssetDashboardSummaryVo summary = new AssetDashboardSummaryVo();
        summary.setRecentEventDays(query.getDays());
        summary.setStatusList(new ArrayList<>());

        if (SecurityUtils.hasPermi(PERM_INFO_LIST))
        {
            summary.setAssetTotal(defaultCount(assetDashboardServiceProxy.selectVisibleAssetTotal(query)));
            summary.setStatusList(buildStatusDistribution(assetDashboardServiceProxy.selectVisibleAssetStatusCountList(query)));
        }
        if (SecurityUtils.hasPermi(PERM_ORDER_LIST))
        {
            summary.setOrderTotal(defaultCount(assetDashboardServiceProxy.selectVisibleOrderTotal(query)));
        }
        if (SecurityUtils.hasPermi(PERM_INVENTORY_LIST))
        {
            summary.setInventoryTaskTotal(defaultCount(assetDashboardServiceProxy.selectVisibleInventoryTaskTotal(query)));
        }
        if (SecurityUtils.hasPermi(PERM_EVENT_LIST))
        {
            summary.setRecentEventTotal(defaultCount(assetDashboardServiceProxy.selectVisibleRecentEventTotal(query)));
        }
        return summary;
    }

    @Override
    public AssetDashboardTodoVo selectDashboardTodo()
    {
        AssetDashboardQueryVo query = buildRecentQuery(DEFAULT_TREND_DAYS);
        AssetDashboardTodoVo todo = new AssetDashboardTodoVo();
        List<AssetDashboardTodoItemVo> itemList = new ArrayList<>();
        itemList.add(buildTodoItem("unfinishedOrder", "待处理单据", "/asset/order",
            SecurityUtils.hasPermi(PERM_ORDER_LIST),
            SecurityUtils.hasPermi(PERM_ORDER_LIST) ? assetDashboardServiceProxy.selectTodoUnfinishedOrderCount(query) : 0L));
        itemList.add(buildTodoItem("unfinishedInventory", "待完成盘点", "/asset/inventory",
            SecurityUtils.hasPermi(PERM_INVENTORY_LIST),
            SecurityUtils.hasPermi(PERM_INVENTORY_LIST) ? assetDashboardServiceProxy.selectTodoUnfinishedInventoryTaskCount(query) : 0L));
        itemList.add(buildTodoItem("pendingDiff", "待处理差异", "/asset/inventory",
            SecurityUtils.hasPermi(PERM_INVENTORY_LIST),
            SecurityUtils.hasPermi(PERM_INVENTORY_LIST) ? assetDashboardServiceProxy.selectTodoPendingDiffCount(query) : 0L));
        todo.setItemList(itemList);
        return todo;
    }

    @Override
    public AssetDashboardTrendVo selectDashboardTrend()
    {
        AssetDashboardQueryVo query = buildRecentQuery(DEFAULT_TREND_DAYS);
        AssetDashboardTrendVo trendVo = new AssetDashboardTrendVo();
        trendVo.setDays(query.getDays());

        Map<String, AssetDashboardTrendPointVo> pointMap = initTrendPointMap(query.getDays());
        if (SecurityUtils.hasPermi(PERM_EVENT_LIST))
        {
            mergeTrendStat(pointMap, assetDashboardServiceProxy.selectEventTrendStatList(query), TREND_METRIC_EVENT);
        }
        if (SecurityUtils.hasPermi(PERM_ORDER_LIST))
        {
            mergeTrendStat(pointMap, assetDashboardServiceProxy.selectOrderTrendStatList(query), TREND_METRIC_ORDER);
        }
        if (SecurityUtils.hasPermi(PERM_INVENTORY_LIST))
        {
            mergeTrendStat(pointMap, assetDashboardServiceProxy.selectInventoryTrendStatList(query), TREND_METRIC_INVENTORY);
            mergeTrendStat(pointMap, assetDashboardServiceProxy.selectDiffTrendStatList(query), TREND_METRIC_DIFF);
        }
        trendVo.setPointList(new ArrayList<>(pointMap.values()));
        return trendVo;
    }

    @Override
    @DataScope(permission = PERM_INFO_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public Long selectVisibleAssetTotal(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectVisibleAssetTotal(query);
    }

    @Override
    @DataScope(permission = PERM_INFO_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetDashboardStatusCountVo> selectVisibleAssetStatusCountList(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectVisibleAssetStatusCountList(query);
    }

    @Override
    @DataScope(permission = PERM_ORDER_LIST, deptAlias = "scope_dept", userAlias = "apply_user")
    public Long selectVisibleOrderTotal(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectVisibleOrderTotal(query);
    }

    @Override
    @DataScope(permission = PERM_INVENTORY_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public Long selectVisibleInventoryTaskTotal(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectVisibleInventoryTaskTotal(query);
    }

    @Override
    @DataScope(permission = PERM_EVENT_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public Long selectVisibleRecentEventTotal(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectVisibleRecentEventTotal(query);
    }

    @Override
    @DataScope(permission = PERM_ORDER_LIST, deptAlias = "scope_dept", userAlias = "apply_user")
    public Long selectTodoUnfinishedOrderCount(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectTodoUnfinishedOrderCount(query);
    }

    @Override
    @DataScope(permission = PERM_INVENTORY_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public Long selectTodoUnfinishedInventoryTaskCount(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectTodoUnfinishedInventoryTaskCount(query);
    }

    @Override
    @DataScope(permission = PERM_INVENTORY_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public Long selectTodoPendingDiffCount(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectTodoPendingDiffCount(query);
    }

    @Override
    @DataScope(permission = PERM_EVENT_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetDashboardTrendStatVo> selectEventTrendStatList(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectEventTrendStatList(query);
    }

    @Override
    @DataScope(permission = PERM_ORDER_LIST, deptAlias = "scope_dept", userAlias = "apply_user")
    public List<AssetDashboardTrendStatVo> selectOrderTrendStatList(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectOrderTrendStatList(query);
    }

    @Override
    @DataScope(permission = PERM_INVENTORY_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetDashboardTrendStatVo> selectInventoryTrendStatList(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectInventoryTrendStatList(query);
    }

    @Override
    @DataScope(permission = PERM_INVENTORY_LIST, deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetDashboardTrendStatVo> selectDiffTrendStatList(AssetDashboardQueryVo query)
    {
        return assetDashboardMapper.selectDiffTrendStatList(query);
    }

    /**
     * 组装最近 N 天的聚合查询窗口。
     */
    private AssetDashboardQueryVo buildRecentQuery(int days)
    {
        int safeDays = days <= 0 ? DEFAULT_TREND_DAYS : days;
        LocalDate today = LocalDate.now();
        LocalDate beginDate = today.minusDays(safeDays - 1L);
        LocalDateTime beginDateTime = LocalDateTime.of(beginDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(today.plusDays(1L), LocalTime.MIN);

        AssetDashboardQueryVo query = new AssetDashboardQueryVo();
        query.setDays(safeDays);
        query.setBeginTime(toDate(beginDateTime));
        query.setEndTime(toDate(endDateTime));
        return query;
    }

    /**
     * 统一补齐状态分布，让前端拿到稳定顺序和 0 值。
     */
    private List<AssetDashboardStatusCountVo> buildStatusDistribution(List<AssetDashboardStatusCountVo> dbList)
    {
        Map<String, String> labelMap = new LinkedHashMap<>();
        labelMap.put("IDLE", "闲置");
        labelMap.put("IN_USE", "在用");
        labelMap.put("BORROWED", "借用中");
        labelMap.put("IN_TRANSFER", "调拨中");
        labelMap.put("IN_INVENTORY", "盘点中");
        labelMap.put("PENDING_DISPOSAL", "待报废");
        labelMap.put("DISPOSED", "已报废");
        labelMap.put("DRAFT", "草稿");

        Map<String, Long> countMap = new LinkedHashMap<>();
        if (dbList != null)
        {
            for (AssetDashboardStatusCountVo item : dbList)
            {
                if (item == null || StringUtils.isEmpty(item.getStatus()))
                {
                    continue;
                }
                countMap.put(item.getStatus(), defaultCount(item.getTotalCount()));
            }
        }

        List<AssetDashboardStatusCountVo> result = new ArrayList<>();
        labelMap.forEach((status, label) -> result.add(buildStatusVo(status, label, countMap.getOrDefault(status, 0L))));
        countMap.forEach((status, totalCount) -> {
            if (!labelMap.containsKey(status))
            {
                result.add(buildStatusVo(status, status, totalCount));
            }
        });
        return result;
    }

    /**
     * 预建 7 天折线点，避免没有数据的日期在前端断档。
     */
    private Map<String, AssetDashboardTrendPointVo> initTrendPointMap(int days)
    {
        Map<String, AssetDashboardTrendPointVo> pointMap = new LinkedHashMap<>();
        LocalDate beginDate = LocalDate.now().minusDays(days - 1L);
        for (int i = 0; i < days; i++)
        {
            String statDate = beginDate.plusDays(i).toString();
            AssetDashboardTrendPointVo point = new AssetDashboardTrendPointVo();
            point.setStatDate(statDate);
            pointMap.put(statDate, point);
        }
        return pointMap;
    }

    /**
     * 合并数据库返回的趋势统计。
     */
    private void mergeTrendStat(Map<String, AssetDashboardTrendPointVo> pointMap,
        List<AssetDashboardTrendStatVo> statList, String metricType)
    {
        if (statList == null)
        {
            return;
        }
        for (AssetDashboardTrendStatVo stat : statList)
        {
            if (stat == null || StringUtils.isEmpty(stat.getStatDate()) || !pointMap.containsKey(stat.getStatDate()))
            {
                continue;
            }
            AssetDashboardTrendPointVo point = pointMap.get(stat.getStatDate());
            long totalCount = defaultCount(stat.getTotalCount());
            if (StringUtils.equals(metricType, TREND_METRIC_EVENT))
            {
                point.setEventCount(totalCount);
            }
            else if (StringUtils.equals(metricType, TREND_METRIC_ORDER))
            {
                point.setOrderCount(totalCount);
            }
            else if (StringUtils.equals(metricType, TREND_METRIC_INVENTORY))
            {
                point.setInventoryCount(totalCount);
            }
            else if (StringUtils.equals(metricType, TREND_METRIC_DIFF))
            {
                point.setDiffCount(totalCount);
            }
        }
    }

    private AssetDashboardStatusCountVo buildStatusVo(String status, String label, Long totalCount)
    {
        AssetDashboardStatusCountVo statusVo = new AssetDashboardStatusCountVo();
        statusVo.setStatus(status);
        statusVo.setLabel(label);
        statusVo.setTotalCount(defaultCount(totalCount));
        return statusVo;
    }

    private AssetDashboardTodoItemVo buildTodoItem(String key, String label, String routePath, boolean permitted, Long count)
    {
        AssetDashboardTodoItemVo item = new AssetDashboardTodoItemVo();
        item.setKey(key);
        item.setLabel(label);
        item.setRoutePath(routePath);
        item.setPermitted(permitted);
        item.setCount(defaultCount(count));
        return item;
    }

    private Long defaultCount(Long count)
    {
        return count == null ? 0L : count;
    }

    private Date toDate(LocalDateTime localDateTime)
    {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
