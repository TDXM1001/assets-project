import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { assetRoutes } from './asset'
import { systemRoutes } from './system'
import { resultRoutes } from './result'
import { exceptionRoutes } from './exception'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  assetRoutes,
  systemRoutes,
  resultRoutes,
  exceptionRoutes
]
