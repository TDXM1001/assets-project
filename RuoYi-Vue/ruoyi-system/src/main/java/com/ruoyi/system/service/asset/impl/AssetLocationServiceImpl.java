package com.ruoyi.system.service.asset.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetLocation;
import com.ruoyi.system.domain.asset.vo.AssetTreeSelect;
import com.ruoyi.system.mapper.asset.AssetLocationMapper;
import com.ruoyi.system.service.asset.IAssetLocationService;

/**
 * 资产位置服务实现
 */
@Service
public class AssetLocationServiceImpl implements IAssetLocationService
{
    @Autowired
    private AssetLocationMapper locationMapper;

    @Override
    public List<AssetLocation> selectLocationList(AssetLocation location)
    {
        return locationMapper.selectLocationList(location);
    }

    @Override
    public List<AssetTreeSelect> selectLocationTreeList(AssetLocation location)
    {
        List<AssetLocation> locations = selectLocationList(location);
        return buildLocationTree(locations).stream().map(AssetTreeSelect::new).collect(Collectors.toList());
    }

    @Override
    public AssetLocation selectLocationById(Long locationId)
    {
        return locationMapper.selectLocationById(locationId);
    }

    @Override
    public boolean checkLocationCodeUnique(AssetLocation location)
    {
        Long locationId = StringUtils.isNull(location.getLocationId()) ? -1L : location.getLocationId();
        AssetLocation info = locationMapper.checkLocationCodeUnique(location.getLocationCode());
        return StringUtils.isNull(info) || info.getLocationId().longValue() == locationId.longValue();
    }

    @Override
    public boolean checkLocationNameUnique(AssetLocation location)
    {
        Long locationId = StringUtils.isNull(location.getLocationId()) ? -1L : location.getLocationId();
        AssetLocation info = locationMapper.checkLocationNameUnique(location.getLocationName(), location.getParentId());
        return StringUtils.isNull(info) || info.getLocationId().longValue() == locationId.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertLocation(AssetLocation location)
    {
        if (location.getParentId() == null || location.getParentId().longValue() == 0L)
        {
            location.setParentId(0L);
            location.setAncestors("0");
        }
        else
        {
            AssetLocation parent = locationMapper.selectLocationById(location.getParentId());
            if (StringUtils.isNull(parent))
            {
                throw new ServiceException("上级位置不存在");
            }
            if (!UserConstants.NORMAL.equals(parent.getStatus()))
            {
                throw new ServiceException("上级位置已停用，不允许新增子位置");
            }
            location.setAncestors(parent.getAncestors() + "," + parent.getLocationId());
        }
        return locationMapper.insertLocation(location);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateLocation(AssetLocation location)
    {
        AssetLocation oldLocation = locationMapper.selectLocationById(location.getLocationId());
        if (StringUtils.isNull(oldLocation))
        {
            throw new ServiceException("位置不存在");
        }
        if (location.getLocationId().equals(location.getParentId()))
        {
            throw new ServiceException("上级位置不能是自己");
        }
        // 防止把上级位置设置为当前节点的子节点，避免树结构被破坏
        if (isParentInLocationSubtree(location.getLocationId(), location.getParentId()))
        {
            throw new ServiceException("上级位置不能选择当前位置的下级节点");
        }

        String newAncestors = "0";
        if (location.getParentId() != null && location.getParentId().longValue() != 0L)
        {
            AssetLocation newParent = locationMapper.selectLocationById(location.getParentId());
            if (StringUtils.isNull(newParent))
            {
                throw new ServiceException("上级位置不存在");
            }
            newAncestors = newParent.getAncestors() + "," + newParent.getLocationId();
        }
        location.setAncestors(newAncestors);
        updateLocationChildren(location.getLocationId(), newAncestors, oldLocation.getAncestors());
        return locationMapper.updateLocation(location);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLocationByIds(Long[] locationIds)
    {
        for (Long locationId : locationIds)
        {
            if (locationMapper.hasChildByLocationId(locationId) > 0)
            {
                throw new ServiceException("存在下级位置，不允许删除");
            }
            if (locationMapper.checkLocationExistAsset(locationId) > 0)
            {
                throw new ServiceException("位置下存在资产，不允许删除");
            }
        }
        return locationMapper.deleteLocationByIds(locationIds);
    }

    private List<AssetLocation> buildLocationTree(List<AssetLocation> locations)
    {
        List<AssetLocation> returnList = new ArrayList<>();
        List<Long> ids = locations.stream().map(AssetLocation::getLocationId).collect(Collectors.toList());
        for (AssetLocation location : locations)
        {
            if (!ids.contains(location.getParentId()))
            {
                recursionFn(locations, location);
                returnList.add(location);
            }
        }
        return returnList.isEmpty() ? locations : returnList;
    }

    private void recursionFn(List<AssetLocation> list, AssetLocation parent)
    {
        List<AssetLocation> childList = getChildList(list, parent);
        parent.setChildren(childList);
        for (AssetLocation child : childList)
        {
            if (hasChild(list, child))
            {
                recursionFn(list, child);
            }
        }
    }

    private List<AssetLocation> getChildList(List<AssetLocation> list, AssetLocation parent)
    {
        List<AssetLocation> childList = new ArrayList<>();
        Iterator<AssetLocation> it = list.iterator();
        while (it.hasNext())
        {
            AssetLocation location = it.next();
            if (StringUtils.isNotNull(location.getParentId()) && location.getParentId().longValue() == parent.getLocationId().longValue())
            {
                childList.add(location);
            }
        }
        return childList;
    }

    private boolean hasChild(List<AssetLocation> list, AssetLocation parent)
    {
        return !getChildList(list, parent).isEmpty();
    }

    private void updateLocationChildren(Long locationId, String newAncestors, String oldAncestors)
    {
        List<AssetLocation> children = locationMapper.selectChildrenLocationById(locationId);
        for (AssetLocation child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (!children.isEmpty())
        {
            locationMapper.updateLocationChildren(children);
        }
    }

    private boolean isParentInLocationSubtree(Long locationId, Long parentId)
    {
        if (parentId == null || parentId.longValue() == 0L)
        {
            return false;
        }
        // 仅判断当前节点的后代范围，避免产生循环引用
        List<AssetLocation> descendants = locationMapper.selectChildrenLocationById(locationId);
        for (AssetLocation descendant : descendants)
        {
            if (descendant.getLocationId() != null && descendant.getLocationId().longValue() == parentId.longValue())
            {
                return true;
            }
        }
        return false;
    }
}
