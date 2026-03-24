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
    }
  ]
}
