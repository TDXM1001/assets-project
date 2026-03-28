(() => {
  if (window.__assetOrderMockInstalled) return 'asset-order-mock-installed'

  const APP_VERSION = '3.0.1'
  const CURRENT_USER_KEY = `sys-v${APP_VERSION}-user`
  const LEGACY_USER_KEY = 'user'

  const userState = {
    language: 'zh',
    isLogin: true,
    isLock: false,
    lockPassword: '',
    info: {
      userId: 1,
      userName: 'admin',
      nickName: 'admin',
      deptId: 1,
      roles: ['R_SUPER']
    },
    searchHistory: [],
    accessToken: 'mock-token',
    refreshToken: 'mock-refresh-token',
    roles: ['R_SUPER'],
    permissions: ['*:*:*']
  }

  const orderRoutes = [
    {
      path: '/asset',
      name: 'Asset',
      component: 'Layout',
      meta: { title: '资产管理' },
      children: [
        {
          path: 'order',
          name: 'AssetOrder',
          component: '/asset/order/index',
          meta: { title: '业务单据', keepAlive: true }
        },
        {
          path: 'order/create',
          name: 'AssetOrderCreate',
          component: '/asset/order/create/index',
          meta: { title: '新增业务单据', isHide: true, isHideTab: true }
        }
      ]
    }
  ]

  const orderListRows = [
    {
      orderId: 2001,
      orderNo: 'O-2001',
      orderType: 'INBOUND',
      orderStatus: 'DRAFT',
      applyDeptName: '设备部',
      applyUserName: '张三',
      bizDate: '2026-03-28 09:00:00',
      fromDeptName: '资产中心',
      toDeptName: '设备部',
      fromUserName: '李四',
      toUserName: '张三',
      fromLocationName: '一楼仓库',
      toLocationName: '二楼机房',
      expectedReturnDate: '2026-04-01',
      disposalReason: '',
      disposalAmount: null,
      approveUserName: '',
      approveTime: '',
      itemCount: 2,
      remark: '入库单据示例'
    },
    {
      orderId: 2002,
      orderNo: 'O-2002',
      orderType: 'DISPOSAL',
      orderStatus: 'APPROVED',
      applyDeptName: '资产中心',
      applyUserName: '王五',
      bizDate: '2026-03-27 15:30:00',
      fromDeptName: '资产中心',
      toDeptName: '资产中心',
      fromUserName: '王五',
      toUserName: '',
      fromLocationName: '三楼库房',
      toLocationName: '',
      expectedReturnDate: '',
      disposalReason: '设备老化',
      disposalAmount: 5800,
      approveUserName: '李四',
      approveTime: '2026-03-27 16:00:00',
      itemCount: 1,
      remark: '报废单据示例'
    }
  ]

  const orderDetail = {
    orderId: 2001,
    orderNo: 'O-2001',
    orderType: 'INBOUND',
    orderStatus: 'DRAFT',
    bizDate: '2026-03-28 09:00:00',
    applyDeptId: 1,
    applyDeptName: '设备部',
    applyUserId: 1,
    applyUserName: '张三',
    fromDeptId: 2,
    fromDeptName: '资产中心',
    toDeptId: 1,
    toDeptName: '设备部',
    fromUserId: 2,
    fromUserName: '李四',
    toUserId: 1,
    toUserName: '张三',
    fromLocationId: 10,
    fromLocationName: '一楼仓库',
    toLocationId: 11,
    toLocationName: '二楼机房',
    expectedReturnDate: '2026-04-01',
    disposalReason: '',
    disposalAmount: null,
    attachmentCount: 1,
    approveResult: '',
    remark: '入库单据示例',
    itemList: [
      {
        assetId: 101,
        assetCode: 'ASSET-101',
        assetName: '测试资产 A',
        beforeStatus: 'IDLE',
        afterStatus: 'IN_USE',
        afterUserId: 1,
        afterLocationId: 11,
        itemResult: '示例明细'
      },
      {
        assetId: 102,
        assetCode: 'ASSET-102',
        assetName: '测试资产 B',
        beforeStatus: 'IDLE',
        afterStatus: 'IN_USE',
        afterUserId: 1,
        afterLocationId: 11,
        itemResult: '示例明细'
      }
    ]
  }

  const deptTree = [
    {
      id: 1,
      label: '设备部',
      children: [
        { id: 11, label: '运维组', children: [] }
      ]
    },
    {
      id: 2,
      label: '资产中心',
      children: []
    }
  ]

  const locationTree = [
    {
      id: 10,
      label: '一楼仓库',
      children: []
    },
    {
      id: 11,
      label: '二楼机房',
      children: []
    }
  ]

  const users = [
    { userId: 1, nickName: '张三', userName: 'zhangsan' },
    { userId: 2, nickName: '李四', userName: 'lisi' },
    { userId: 3, nickName: '王五', userName: 'wangwu' }
  ]

  const assets = [
    {
      assetId: 101,
      assetCode: 'ASSET-101',
      assetName: '测试资产 A',
      assetStatus: 'IDLE'
    },
    {
      assetId: 102,
      assetCode: 'ASSET-102',
      assetName: '测试资产 B',
      assetStatus: 'IDLE'
    },
    {
      assetId: 103,
      assetCode: 'ASSET-103',
      assetName: '测试资产 C',
      assetStatus: 'IN_USE'
    }
  ]

  const dictData = {
    asset_order_type: [
      { dictLabel: '入库', dictValue: 'INBOUND' },
      { dictLabel: '领用', dictValue: 'ASSIGN' },
      { dictLabel: '借用', dictValue: 'BORROW' },
      { dictLabel: '归还', dictValue: 'RETURN' },
      { dictLabel: '调拨', dictValue: 'TRANSFER' },
      { dictLabel: '报废', dictValue: 'DISPOSAL' }
    ],
    asset_order_status: [
      { dictLabel: '草稿', dictValue: 'DRAFT' },
      { dictLabel: '待提交', dictValue: 'SUBMITTED' },
      { dictLabel: '审批中', dictValue: 'APPROVING' },
      { dictLabel: '已通过', dictValue: 'APPROVED' },
      { dictLabel: '已驳回', dictValue: 'REJECTED' },
      { dictLabel: '已完成', dictValue: 'FINISHED' },
      { dictLabel: '已取消', dictValue: 'CANCELED' }
    ],
    asset_status: [
      { dictLabel: '空闲', dictValue: 'IDLE' },
      { dictLabel: '在用', dictValue: 'IN_USE' },
      { dictLabel: '维修中', dictValue: 'REPAIRING' },
      { dictLabel: '已报废', dictValue: 'DISPOSED' }
    ]
  }

  const ok = (data, msg = 'ok') => ({
    code: 200,
    data,
    msg
  })

  const writeUserState = () => {
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
      console.error('读取本地存储失败:', error)
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

  const userSnapshot = writeUserState()

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
        user: userSnapshot.info,
        roles: userSnapshot.roles,
        permissions: userSnapshot.permissions
      })
    }

    if (target.includes('/getRouters')) {
      return ok(orderRoutes)
    }

    if (target.includes('/system/dict/data/type/')) {
      const dictType = target.split('/system/dict/data/type/')[1]
      return ok(dictData[dictType] || [])
    }

    if (target.includes('/system/user/deptTree')) {
      return ok(deptTree)
    }

    if (target.includes('/system/user/list')) {
      return ok({
        rows: users,
        total: users.length,
        current: 1,
        size: 500
      })
    }

    if (target.includes('/asset/location/treeSelect')) {
      return ok(locationTree)
    }

    if (target.includes('/asset/info/list')) {
      return ok({
        rows: assets,
        total: assets.length,
        current: 1,
        size: 20
      })
    }

    if (/\/asset\/info\/\d+$/.test(target) && upperMethod === 'GET') {
      const assetId = Number(target.split('/').pop())
      return ok(assets.find((item) => item.assetId === assetId) || assets[0])
    }

    if (target.includes('/asset/order/list')) {
      return ok({
        rows: orderListRows,
        total: orderListRows.length,
        current: 1,
        size: 10
      })
    }

    if (target.includes('/asset/order/linked')) {
      return ok({
        orderId: orderDetail.orderId,
        orderNo: orderDetail.orderNo
      })
    }

    if (/\/asset\/order\/\d+$/.test(target) && upperMethod === 'GET') {
      return ok(orderDetail)
    }

    if (target.includes('/asset/order/submit/')) {
      return ok({ orderId: orderDetail.orderId, orderNo: orderDetail.orderNo })
    }

    if (target.includes('/asset/order/approve/')) {
      return ok({ orderId: orderDetail.orderId, orderNo: orderDetail.orderNo })
    }

    if (target.includes('/asset/order/reject/')) {
      return ok({ orderId: orderDetail.orderId, orderNo: orderDetail.orderNo })
    }

    if (target.includes('/asset/order/finish/')) {
      return ok({ orderId: orderDetail.orderId, orderNo: orderDetail.orderNo })
    }

    if (target.includes('/asset/order/cancel/')) {
      return ok({ orderId: orderDetail.orderId, orderNo: orderDetail.orderNo })
    }

    if (target.includes('/asset/order') && upperMethod === 'POST') {
      return ok({
        orderId: orderDetail.orderId,
        orderNo: orderDetail.orderNo
      })
    }

    if (target.includes('/asset/order') && upperMethod === 'PUT') {
      return ok({
        orderId: orderDetail.orderId,
        orderNo: orderDetail.orderNo
      })
    }

    if (target.includes('/asset/order/') && upperMethod === 'DELETE') {
      return ok({ orderId: orderDetail.orderId })
    }

    if (target.includes('/asset/order/export')) {
      return ok({ exported: true })
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
          console.error('XHR mock 事件处理失败:', error)
        }
      })
    }

    send() {
      const payload = getMockPayload(this.__mockMethod, this.__mockUrl)

      if (!payload) {
        respondPayload(this, {
          code: 500,
          msg: `Mock 未覆盖: ${this.__mockMethod} ${this.__mockUrl}`
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
        info: userSnapshot.info,
        roles: userSnapshot.roles,
        permissions: userSnapshot.permissions,
        searchHistory: [],
        language: 'zh',
        isLock: false,
        lockPassword: ''
      })
    )
  } catch (error) {
    console.error('写入本地登录态失败:', error)
  }

  window.__assetOrderMockInstalled = true
  return 'asset-order-mock-installed'
})()
