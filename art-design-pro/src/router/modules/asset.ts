import { AppRouteRecord } from '@/types/router'

export const assetRoutes: AppRouteRecord = {
  path: '/asset',
  name: 'Asset',
  component: '/index/index',
  meta: {
    title: '璧勪骇绠＄悊',
    icon: 'ri:archive-line'
  },
  children: [
    {
      path: 'category',
      name: 'AssetCategory',
      component: '/asset/category',
      meta: {
        title: '璧勪骇鍒嗙被',
        keepAlive: true
      }
    },
    {
      path: 'location',
      name: 'AssetLocation',
      component: '/asset/location',
      meta: {
        title: '瀛樻斁浣嶇疆',
        keepAlive: true
      }
    },
    {
      path: 'info',
      name: 'AssetInfo',
      component: '/asset/info',
      meta: {
        title: '璧勪骇鍙拌处',
        keepAlive: true
      }
    }
  ]
}
