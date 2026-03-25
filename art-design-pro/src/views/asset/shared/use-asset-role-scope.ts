import { computed } from 'vue'
import { useUserStore } from '@/store/modules/user'

const SUPER_ADMIN_ROLE = 'admin'
const ASSET_ADMIN_ROLE = 'asset_admin'
const ASSET_DEPT_MANAGER_ROLE = 'asset_dept_manager'
const ASSET_USER_ROLE = 'asset_user'
const ASSET_AUDITOR_ROLE = 'asset_auditor'

/**
 * 统一收敛资产模块的角色口径，避免每个页面各自拼接角色判断。
 */
export function useAssetRoleScope() {
  const userStore = useUserStore()
  const roles = computed(() => userStore.roles || [])

  const hasRole = (roleKey: string) =>
    roles.value.includes(SUPER_ADMIN_ROLE) || roles.value.includes(roleKey)

  const isAssetAdmin = computed(() => hasRole(ASSET_ADMIN_ROLE))
  const isAssetDeptManager = computed(() => hasRole(ASSET_DEPT_MANAGER_ROLE))
  const isAssetUser = computed(() => hasRole(ASSET_USER_ROLE))
  const isAssetAuditor = computed(() => hasRole(ASSET_AUDITOR_ROLE))

  /**
   * 按分类盘点一期只开放给资产管理员和审计角色。
   */
  const canUseCategoryInventoryScope = computed(() => isAssetAdmin.value || isAssetAuditor.value)

  /**
   * 资产使用人只保留“我的数据”视角，不再展示可扩范围的人选筛选。
   */
  const isSelfScopedAssetUser = computed(
    () =>
      isAssetUser.value && !isAssetAdmin.value && !isAssetDeptManager.value && !isAssetAuditor.value
  )

  return {
    roles,
    hasRole,
    isAssetAdmin,
    isAssetDeptManager,
    isAssetUser,
    isAssetAuditor,
    canUseCategoryInventoryScope,
    isSelfScopedAssetUser
  }
}
