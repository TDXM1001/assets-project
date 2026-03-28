(() => {
  if (window.__repairMockInstalled) return 'repair-mock-installed'

  const APP_VERSION = '3.0.1'
  const CURRENT_USER_KEY = `sys-v${APP_VERSION}-user`
  const LEGACY_USER_KEY = 'user'

  const repairRoutes = [
    {
      path: '/asset',
      name: 'Asset',
      component: 'Layout',
      meta: { title: '资产管理' },
      children: [
        {
          path: 'repair',
          name: 'AssetRepair',
          component: '/asset/repair/index',
          meta: { title: '维修管理', keepAlive: true }
        },
        {
          path: 'repair/create',
          name: 'AssetRepairCreate',
          component: '/asset/repair/create/index',
          meta: { title: '新建维修单', isHide: true, isHideTab: true }
        },
        {
          path: 'repair/edit/:repairId',
          name: 'AssetRepairEdit',
          component: '/asset/repair/edit/index',
          meta: { title: '编辑维修单', isHide: true, isHideTab: true }
        },
        {
          path: 'repair/detail/:repairId',
          name: 'AssetRepairDetail',
          component: '/asset/repair/detail/index',
          meta: { title: '维修详情', isHide: true, isHideTab: true }
        }
      ]
    }
  ]

  const repairListRows = [
    {
      repairId: 1001,
      repairNo: 'R-1001',
      assetCode: 'ASSET-001',
      assetName: '设备一',
      itemCount: 2,
      repairStatus: 'DRAFT',
      applyDeptName: '设备部',
      applyUserName: '张三',
      reportTime: '2026-03-28 09:00:00',
      vendorName: '华东维修厂',
      repairCost: 123.45,
      resultType: 'SUGGEST_DISPOSAL',
      faultDesc: '主机异常',
      beforeStatus: 'IN_USE',
      afterStatus: 'IN_USE'
    }
  ]

  const repairDetail = {
    repairId: 1001,
    repairNo: 'R-1001',
    assetCode: 'ASSET-001',
    assetName: '设备一',
    itemCount: 2,
    repairStatus: 'FINISHED',
    applyDeptName: '设备部',
    applyUserName: '张三',
    reportTime: '2026-03-28 09:00:00',
    vendorName: '华东维修厂',
    repairCost: 123.45,
    resultType: 'SUGGEST_DISPOSAL',
    faultDesc: '主机异常',
    remark: '备注',
    beforeStatus: 'IN_USE',
    afterStatus: 'IN_USE',
    approveUserName: '李四',
    approveTime: '2026-03-28 10:00:00',
    finishTime: '2026-03-28 11:30:00',
    sendRepairTime: '2026-03-28 09:30:00',
    repairMode: 'VENDOR',
    repairItems: [
      {
        repairItemId: 1,
        assetId: 1,
        assetCode: 'ASSET-001',
        assetName: '设备一',
        beforeStatus: 'IN_USE',
        afterStatus: 'IN_USE',
        resultType: 'SUGGEST_DISPOSAL',
        faultDesc: '主机异常',
        remark: '首条明细'
      },
      {
        repairItemId: 2,
        assetId: 2,
        assetCode: 'ASSET-002',
        assetName: '设备二',
        beforeStatus: 'IDLE',
        afterStatus: 'IDLE',
        resultType: 'TO_IDLE',
        faultDesc: '电源异常',
        remark: '次条明细'
      }
    ]
  }

  const ok = (data, msg = 'ok') => ({
    code: 200,
    data,
    msg
  })

  const writeVersionedUserState = () => {
    const userState = {
      language: 'zh',
      isLogin: true,
      isLock: false,
      lockPassword: '',
      info: {
        userId: 1,
        userName: 'admin',
        nickName: '超级管理员',
        deptId: 1,
        roles: ['R_SUPER']
      },
      searchHistory: [],
      accessToken: 'mock-token',
      refreshToken: 'mock-refresh-token',
      roles: ['R_SUPER'],
      permissions: ['*:*:*']
    }

    const payload = JSON.stringify(userState)
    const keysToWrite = new Set([CURRENT_USER_KEY, LEGACY_USER_KEY])

    try {
      for (let i = 0; i < window.localStorage.length; i += 1) {
        const key = window.localStorage.key(i)
        if (key && /^sys-v[^-]+-user$/.test(key)) {
          keysToWrite.add(key)
        }
      }
    } catch (error) {
      console.error('读取本地存储键失败:', error)
    }

    keysToWrite.forEach((key) => {
      try {
        window.localStorage.setItem(key, payload)
      } catch (error) {
        console.error(`写入登录态失败: ${key}`, error)
      }
    })

    return userState
  }

  const userState = writeVersionedUserState()

  const respondPayload = (xhr, payload) => {
    const responseText = JSON.stringify(payload)

    try {
      Object.defineProperty(xhr, 'status', { value: 200, configurable: true })
      Object.defineProperty(xhr, 'readyState', { value: 4, configurable: true })
      Object.defineProperty(xhr, 'responseURL', {
        value: String(xhr.__mockUrl || ''),
        configurable: true
      })

      if (xhr.responseType === 'blob') {
        const blob = new Blob([responseText], { type: 'application/json' })
        Object.defineProperty(xhr, 'response', { value: blob, configurable: true })
        Object.defineProperty(xhr, 'responseText', { value: '', configurable: true })
      } else {
        Object.defineProperty(xhr, 'responseText', { value: responseText, configurable: true })
        Object.defineProperty(xhr, 'response', { value: responseText, configurable: true })
      }
    } catch (error) {
      console.error('设置 XHR mock 响应失败:', error)
    }

    const dispatch = (type) => {
      const handlers = xhr.__listeners.get(type) || []
      handlers.forEach((handler) => {
        try {
          handler.call(xhr, new Event(type))
        } catch (error) {
          console.error('XHR mock 事件分发失败:', error)
        }
      })
    }

    setTimeout(() => {
      xhr.onreadystatechange && xhr.onreadystatechange()
      xhr.onload && xhr.onload()
      xhr.onloadend && xhr.onloadend()
      dispatch('readystatechange')
      dispatch('load')
      dispatch('loadend')
    }, 0)
  }

  const getMockPayload = (method, url) => {
    if (!url) return null

    const target = String(url)
    const upperMethod = String(method || '').toUpperCase()

    if (target.includes('/login') && upperMethod === 'POST') {
      return ok({
        token: 'mock-token',
        refreshToken: 'mock-refresh-token'
      })
    }

    if (target.includes('/getInfo')) {
      return ok({
        user: userState.info,
        roles: userState.roles,
        permissions: userState.permissions
      })
    }

    if (target.includes('/getRouters')) {
      return ok(repairRoutes)
    }

    if (target.includes('/asset/repair/list')) {
      return ok({
        rows: repairListRows,
        total: repairListRows.length,
        current: 1,
        size: 10
      })
    }

    if (/\/asset\/repair\/\d+$/.test(target) && upperMethod === 'GET') {
      return ok(repairDetail)
    }

    if (target.includes('/asset/repair/submit/')) {
      return ok({ repairId: repairDetail.repairId })
    }

    if (target.includes('/asset/repair/approve/')) {
      return ok({ repairId: repairDetail.repairId })
    }

    if (target.includes('/asset/repair/reject/')) {
      return ok({ repairId: repairDetail.repairId })
    }

    if (target.includes('/asset/repair/finish/')) {
      return ok({ repairId: repairDetail.repairId })
    }

    if (target.includes('/asset/repair/cancel/')) {
      return ok({ repairId: repairDetail.repairId })
    }

    if (target.includes('/asset/repair') && upperMethod === 'POST') {
      return ok({
        repairId: repairDetail.repairId,
        repairNo: repairDetail.repairNo
      })
    }

    if (target.includes('/asset/repair') && upperMethod === 'PUT') {
      return ok({
        repairId: repairDetail.repairId,
        repairNo: repairDetail.repairNo
      })
    }

    if (target.includes('/asset/repair/') && upperMethod === 'DELETE') {
      return ok({ repairId: repairDetail.repairId })
    }

    return null
  }

  class MockXHR {
    constructor() {
      this.readyState = 0
      this.status = 0
      this.responseText = ''
      this.response = ''
      this.responseURL = ''
      this.responseType = ''
      this.timeout = 0
      this.withCredentials = false
      this.onreadystatechange = null
      this.onload = null
      this.onloadend = null
      this.onerror = null
      this.onabort = null
      this.ontimeout = null
      this.__mockMethod = 'GET'
      this.__mockUrl = ''
      this.__listeners = new Map()
    }

    open(method, url) {
      this.__mockMethod = method
      this.__mockUrl = url
      this.responseURL = String(url)
      this.readyState = 1
    }

    setRequestHeader() {}

    overrideMimeType() {}

    addEventListener(type, handler) {
      const list = this.__listeners.get(type) || []
      list.push(handler)
      this.__listeners.set(type, list)
    }

    removeEventListener(type, handler) {
      const list = this.__listeners.get(type) || []
      this.__listeners.set(
        type,
        list.filter((item) => item !== handler)
      )
    }

    getAllResponseHeaders() {
      return 'content-type: application/json'
    }

    getResponseHeader(name) {
      if (String(name || '').toLowerCase() === 'content-type') {
        return 'application/json'
      }
      return null
    }

    abort() {
      this.readyState = 0
      this.onabort && this.onabort()
      this.__dispatch('abort')
    }

    __dispatch(type) {
      const handlers = this.__listeners.get(type) || []
      handlers.forEach((handler) => {
        try {
          handler.call(this, new Event(type))
        } catch (error) {
          console.error('XHR mock 事件分发失败:', error)
        }
      })
    }

    send() {
      const payload = getMockPayload(this.__mockMethod, this.__mockUrl)

      if (!payload) {
        respondPayload(this, {
          code: 500,
          msg: `Mock 未覆盖请求: ${this.__mockMethod} ${this.__mockUrl}`
        })
        return
      }

      respondPayload(this, payload)
    }
  }

  window.XMLHttpRequest = MockXHR

  if (window.__userStore?.setToken) {
    window.__userStore.setToken('mock-token', 'mock-refresh-token')
  }
  if (window.__userStore?.setLoginStatus) {
    window.__userStore.setLoginStatus(true)
  }

  try {
    window.localStorage.setItem(
      CURRENT_USER_KEY,
      JSON.stringify({
        isLogin: true,
        accessToken: 'mock-token',
        refreshToken: 'mock-refresh-token',
        info: userState.info,
        roles: userState.roles,
        permissions: userState.permissions,
        searchHistory: [],
        language: 'zh',
        isLock: false,
        lockPassword: ''
      })
    )
  } catch (error) {
    console.error('写入本地登录态失败:', error)
  }

  window.__repairMockInstalled = true
  return 'repair-mock-installed'
})()
