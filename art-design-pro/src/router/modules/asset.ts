import { AppRouteRecord } from '@/types/router'

export const assetRoutes: AppRouteRecord = {
  path: '/asset',
  name: 'Asset',
  component: '/index/index',
  meta: {
    title: '资产管理',
    icon: 'ri:archive-line'
  },
  children: [
    {
      path: 'category',
      name: 'AssetCategory',
      component: '/asset/category',
      meta: {
        title: '资产分类',
        keepAlive: true
      }
    },
    {
      path: 'location',
      name: 'AssetLocation',
      component: '/asset/location',
      meta: {
        title: '存放位置',
        keepAlive: true
      }
    },
    {
      path: 'info',
      name: 'AssetInfo',
      component: '/asset/info',
      meta: {
        title: '资产台账',
        keepAlive: true
      }
    },
    {
      path: 'order',
      name: 'AssetOrder',
      component: '/asset/order',
      meta: {
        title: '业务单据',
        keepAlive: true
      }
    },
    {
      path: 'inventory',
      name: 'AssetInventory',
      component: '/asset/inventory',
      meta: {
        title: '盘点任务',
        keepAlive: true
      }
    }
  ]
}
