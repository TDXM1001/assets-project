import { AppRouteRecord } from '@/types/router'

export const assetRoutes: AppRouteRecord = {
  path: '/asset',
  name: 'Asset',
  component: '/index/index',
  meta: {
    title: '资产管理',
    icon: 'ri:briefcase-4-line'
  },
  children: [
    {
      path: 'repair',
      name: 'AssetRepair',
      component: '/asset/repair/index',
      meta: {
        title: '维修管理',
        keepAlive: true
      }
    },
    {
      path: 'repair/create',
      name: 'AssetRepairCreate',
      component: '/asset/repair/create/index',
      meta: {
        title: '新建维修单',
        isHide: true,
        isHideTab: true
      }
    },
    {
      path: 'repair/edit/:repairId',
      name: 'AssetRepairEdit',
      component: '/asset/repair/edit/index',
      meta: {
        title: '编辑维修单',
        isHide: true,
        isHideTab: true
      }
    },
    {
      path: 'repair/detail/:repairId',
      name: 'AssetRepairDetail',
      component: '/asset/repair/detail/index',
      meta: {
        title: '维修详情',
        isHide: true,
        isHideTab: true
      }
    }
  ]
}
