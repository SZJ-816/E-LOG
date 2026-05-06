<template>
  <div class="app" ref="appRef">
    <ThreeBackground />

    <div class="scroll-container" ref="scrollContainer" @scroll="onScroll">
      <!-- Hero Section -->
      <section class="hero" ref="heroSection">
        <div class="hero-content" :style="heroParallax">
          <div class="hero-badge" :class="{ visible: mounted }">
            <span class="badge-dot"></span>
            <span>实时监控中</span>
          </div>
          <h1 class="hero-title" :class="{ visible: mounted }">
            <span class="title-line">E-LOG</span>
            <span class="title-line accent">企业级日志分析</span>
          </h1>
          <p class="hero-desc" :class="{ visible: mounted }">
            从海量数据中提炼世界的内在规律与模式<br/>
            实时洞察 · 智能分析 · 可视化决策
          </p>
          <div class="hero-actions" :class="{ visible: mounted }">
            <button class="btn-primary" @click="scrollToDashboard">
              进入控制台
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </button>
            <button class="btn-ghost" @click="scrollToFeatures">了解更多</button>
          </div>
        </div>
        <div class="scroll-hint" :class="{ visible: mounted }">
          <div class="scroll-line"></div>
          <span>SCROLL</span>
        </div>
      </section>

      <!-- Stats Overview -->
      <section class="stats-section" ref="statsSection">
        <div class="section-inner">
          <div class="stats-grid">
            <div v-for="(stat, i) in liveStats" :key="i" class="stat-card" :class="{ visible: statsVisible }" :style="{ transitionDelay: i * 100 + 'ms' }">
              <div class="stat-icon" :style="{ background: stat.bg }">{{ stat.icon }}</div>
              <div class="stat-body">
                <div class="stat-value">
                  <span class="stat-num">{{ stat.displayValue }}</span>
                  <span class="stat-unit" v-if="stat.unit">{{ stat.unit }}</span>
                </div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
              <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'" v-if="stat.trend">
                <svg width="12" height="12" viewBox="0 0 12 12"><path :d="stat.trend > 0 ? 'M6 2v8M3 5l3-3 3 3' : 'M6 10V2M3 7l3 3 3-3'" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                {{ Math.abs(stat.trend) }}%
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Alert Panel (NEW) -->
      <section class="alert-section" ref="alertSection" v-if="alerts.length > 0">
        <div class="section-inner">
          <div class="alert-panel" :class="{ visible: dashboardVisible }">
            <div class="alert-header">
              <h3>🚨 实时告警</h3>
              <button class="alert-clear" @click="alerts = []">清除全部</button>
            </div>
            <div class="alert-list">
              <div v-for="(alert, i) in alerts" :key="i" class="alert-item" :class="alert.type">
                <span class="alert-icon">{{ alert.type === 'critical' ? '🔴' : alert.type === 'warning' ? '🟡' : '🔵' }}</span>
                <div class="alert-body">
                  <span class="alert-title">{{ alert.title }}</span>
                  <span class="alert-desc">{{ alert.desc }}</span>
                </div>
                <span class="alert-time">{{ alert.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Dashboard -->
      <section class="dashboard-section" ref="dashboardSection">
        <div class="section-inner">
          <div class="section-header" :class="{ visible: dashboardVisible }">
            <div>
              <h2>数据全景</h2>
              <p>实时监控流量趋势与系统健康状态</p>
            </div>
            <button class="btn-export" @click="exportData">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M7 1v8M4 6l3 3 3-3M2 11h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
              导出报告
            </button>
          </div>

          <div class="charts-row" :class="{ visible: dashboardVisible }">
            <div class="chart-card wide">
              <div class="chart-header">
                <h3>流量趋势</h3>
                <div class="chart-tabs">
                  <button v-for="r in timeRanges" :key="r.value" :class="{ active: selectedRange === r.value }" @click="switchRange(r.value)">{{ r.label }}</button>
                </div>
              </div>
              <div ref="flowChartRef" class="chart-area"></div>
            </div>
            <div class="chart-card">
              <div class="chart-header"><h3>日志级别</h3></div>
              <div ref="statusChartRef" class="chart-area"></div>
            </div>
          </div>

          <div class="charts-row" :class="{ visible: dashboardVisible }" style="transition-delay: 200ms">
            <div class="chart-card">
              <div class="chart-header"><h3>系统健康</h3></div>
              <div ref="radarChartRef" class="chart-area"></div>
            </div>
            <div class="chart-card">
              <div class="chart-header"><h3>响应时间</h3></div>
              <div ref="responseChartRef" class="chart-area"></div>
            </div>
          </div>

          <!-- Comparison Row (NEW) -->
          <div class="compare-row" :class="{ visible: dashboardVisible }" style="transition-delay: 300ms">
            <div class="chart-card full">
              <div class="chart-header">
                <h3>同比环比分析</h3>
                <div class="chart-tabs">
                  <button :class="{ active: compareMode === 'day' }" @click="switchCompare('day')">日对比</button>
                  <button :class="{ active: compareMode === 'week' }" @click="switchCompare('week')">周对比</button>
                </div>
              </div>
              <div ref="compareChartRef" class="chart-area"></div>
            </div>
          </div>

          <div class="bottom-row" :class="{ visible: dashboardVisible }" style="transition-delay: 400ms">
            <div class="info-card">
              <div class="info-header"><h3>热门接口 TOP 5</h3></div>
              <div class="top-list">
                <div v-for="(api, i) in topApis" :key="i" class="top-item">
                  <span class="top-rank">{{ i + 1 }}</span>
                  <span class="top-path">{{ api.path }}</span>
                  <div class="top-bar-bg"><div class="top-bar" :style="{ width: api.barWidth + '%' }"></div></div>
                  <span class="top-count">{{ formatNumber(api.count) }}</span>
                </div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-header"><h3>最近错误</h3></div>
              <div class="log-list">
                <div v-for="(log, i) in recentLogs" :key="i" class="log-item">
                  <span class="log-level" :class="log.level.toLowerCase()">{{ log.level }}</span>
                  <span class="log-service">{{ log.service }}</span>
                  <span class="log-msg">{{ log.message }}</span>
                  <span class="log-time">{{ log.time }}</span>
                </div>
              </div>
            </div>
            <div class="info-card">
              <div class="info-header"><h3>服务状态</h3></div>
              <div class="server-list">
                <div v-for="(srv, i) in servers" :key="i" class="server-item">
                  <div class="srv-head">
                    <span class="srv-icon">{{ srv.icon }}</span>
                    <span class="srv-name">{{ srv.name }}</span>
                    <span class="srv-status" :class="srv.status">{{ srv.status === 'online' ? '在线' : '离线' }}</span>
                  </div>
                  <div class="srv-metrics">
                    <div class="srv-metric"><span>CPU</span><div class="srv-bar-bg"><div class="srv-bar" :style="{ width: srv.cpu + '%', background: getMetricColor(srv.cpu) }"></div></div><span class="srv-val">{{ srv.cpu.toFixed(0) }}%</span></div>
                    <div class="srv-metric"><span>MEM</span><div class="srv-bar-bg"><div class="srv-bar" :style="{ width: srv.memory + '%', background: getMetricColor(srv.memory) }"></div></div><span class="srv-val">{{ srv.memory.toFixed(0) }}%</span></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Footer -->
      <footer class="footer">
        <div class="footer-inner">
          <span>E-LOG v2.0</span>
          <span>企业级日志分析系统</span>
          <span>{{ currentTime }}</span>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { logApi } from './api/index.js'
import ThreeBackground from './components/ThreeBackground.vue'

const appRef = ref(null)
const scrollContainer = ref(null)
const heroSection = ref(null)
const statsSection = ref(null)
const alertSection = ref(null)
const dashboardSection = ref(null)
const flowChartRef = ref(null)
const statusChartRef = ref(null)
const radarChartRef = ref(null)
const responseChartRef = ref(null)
const compareChartRef = ref(null)

const mounted = ref(false)
const statsVisible = ref(false)
const dashboardVisible = ref(false)
const selectedRange = ref('today')
const compareMode = ref('day')
const heroParallax = reactive({ transform: 'translateY(0px)', opacity: 1 })

const alerts = ref([
  { type: 'critical', title: 'CPU 使用率超过 90%', desc: 'payment-service 节点 192.168.1.12', time: '2分钟前' },
  { type: 'warning', title: 'Kafka 消费延迟增加', desc: 'consumer-group-lag: 12,500 条', time: '5分钟前' },
  { type: 'info', title: 'HDFS 存储使用率达 75%', desc: '建议扩容或清理历史数据', time: '15分钟前' }
])

const timeRanges = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' }
]

const liveStats = reactive([
  { icon: '👁', label: '页面浏览', value: 0, displayValue: '0', unit: '', trend: 12.5, bg: 'rgba(99,102,241,0.15)' },
  { icon: '👤', label: '独立访客', value: 0, displayValue: '0', unit: '', trend: 8.3, bg: 'rgba(168,85,247,0.15)' },
  { icon: '⚠', label: '今日错误', value: 0, displayValue: '0', unit: '', trend: -5.2, bg: 'rgba(244,63,94,0.15)' },
  { icon: '⚡', label: '平均响应', value: 0, displayValue: '0', unit: 'ms', trend: 3.1, bg: 'rgba(34,211,238,0.15)' }
])

const topApis = ref([
  { path: '/api/v1/users', count: 45230, barWidth: 100 },
  { path: '/api/v1/orders', count: 38910, barWidth: 86 },
  { path: '/api/v1/products', count: 32150, barWidth: 71 },
  { path: '/api/v1/payments', count: 28760, barWidth: 64 },
  { path: '/api/v1/auth/login', count: 25430, barWidth: 56 }
])

const recentLogs = ref([
  { time: '19:41', level: 'ERROR', service: 'payment', message: 'Connection timeout' },
  { time: '19:34', level: 'ERROR', service: 'order', message: 'Database deadlock' },
  { time: '19:28', level: 'WARN', service: 'gateway', message: 'Rate limit exceeded' },
  { time: '19:21', level: 'ERROR', service: 'user', message: 'JWT token expired' },
  { time: '19:14', level: 'ERROR', service: 'search', message: 'ES health check failed' }
])

const servers = ref([
  { icon: '🖥', name: 'payment-service', status: 'online', cpu: 42.5, memory: 58.3 },
  { icon: '🖥', name: 'order-service', status: 'online', cpu: 35.2, memory: 45.7 },
  { icon: '🖥', name: 'user-service', status: 'online', cpu: 28.9, memory: 52.1 },
  { icon: '🖥', name: 'search-service', status: 'online', cpu: 55.3, memory: 67.8 },
  { icon: '🖥', name: 'gateway', status: 'online', cpu: 18.6, memory: 32.4 }
])

let flowChart, statusChart, radarChart, responseChart, compareChart
let currentTime = ref('')
let timerInterval

function formatNumber(n) {
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K'
  return String(n)
}

function getMetricColor(val) {
  if (val > 80) return '#f43f5e'
  if (val > 60) return '#f59e0b'
  return '#10b981'
}

function animateValue(stat, target) {
  const duration = 1200
  const start = stat.value
  const diff = target - start
  const startTime = performance.now()
  function step(now) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    const current = Math.round(start + diff * eased)
    stat.value = current
    stat.displayValue = formatNumber(current)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

function onScroll() {
  const el = scrollContainer.value
  if (!el) return
  const scrollY = el.scrollTop
  const vh = window.innerHeight

  heroParallax.transform = `translateY(${scrollY * 0.3}px)`
  heroParallax.opacity = Math.max(0, 1 - scrollY / (vh * 0.6))

  if (!statsVisible.value && statsSection.value) {
    const rect = statsSection.value.getBoundingClientRect()
    if (rect.top < vh * 0.8) statsVisible.value = true
  }
  if (!dashboardVisible.value && dashboardSection.value) {
    const rect = dashboardSection.value.getBoundingClientRect()
    if (rect.top < vh * 0.8) dashboardVisible.value = true
  }
}

function scrollToDashboard() {
  dashboardSection.value?.scrollIntoView({ behavior: 'smooth' })
}

function scrollToFeatures() {
  statsSection.value?.scrollIntoView({ behavior: 'smooth' })
}

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
}

function generateFlowData(range) {
  const pvBase = [12500,8900,5600,3200,2800,4500,8900,15600,28900,35200,41500,38700,42300,39800,36100,33400,29800,25600,31200,28900,24500,19800,15200,11800]
  const uvBase = [3200,2100,1500,800,700,1200,2300,4100,7500,9200,10800,10100,11200,10400,9500,8800,7800,6700,8200,7600,6400,5200,4000,3100]

  if (range === 'today') {
    return {
      labels: Array.from({ length: 24 }, (_, i) => `${String(i).padStart(2, '0')}:00`),
      pv: pvBase,
      uv: uvBase
    }
  } else if (range === 'week') {
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return {
      labels: days,
      pv: [312000, 345000, 298000, 367000, 389000, 245000, 198000],
      uv: [82000, 91000, 78000, 96000, 102000, 64000, 52000]
    }
  } else {
    const weeks = Array.from({ length: 4 }, (_, i) => `第${i + 1}周`)
    return {
      labels: weeks,
      pv: [2150000, 2380000, 2450000, 2680000],
      uv: [560000, 620000, 640000, 700000]
    }
  }
}

function switchRange(range) {
  selectedRange.value = range
  if (flowChart) {
    const data = generateFlowData(range)
    flowChart.setOption({
      xAxis: { data: data.labels },
      series: [{ data: data.pv }, { data: data.uv }]
    })
  }
}

function switchCompare(mode) {
  compareMode.value = mode
  if (compareChart) {
    compareChart.setOption(getCompareOption(mode))
  }
}

function exportData() {
  const data = {
    exportTime: new Date().toISOString(),
    range: selectedRange.value,
    stats: liveStats.map(s => ({ label: s.label, value: s.value })),
    topApis: topApis.value,
    alerts: alerts.value,
    recentLogs: recentLogs.value
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `e-log-report-${new Date().toISOString().slice(0, 10)}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function initCharts() {
  if (flowChartRef.value) {
    flowChart = echarts.init(flowChartRef.value, null, { renderer: 'canvas' })
    flowChart.setOption(getFlowOption())
  }
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value, null, { renderer: 'canvas' })
    statusChart.setOption(getStatusOption())
  }
  if (radarChartRef.value) {
    radarChart = echarts.init(radarChartRef.value, null, { renderer: 'canvas' })
    radarChart.setOption(getRadarOption())
  }
  if (responseChartRef.value) {
    responseChart = echarts.init(responseChartRef.value, null, { renderer: 'canvas' })
    responseChart.setOption(getResponseOption())
  }
  if (compareChartRef.value) {
    compareChart = echarts.init(compareChartRef.value, null, { renderer: 'canvas' })
    compareChart.setOption(getCompareOption('day'))
  }
}

async function initAndFetch() {
  await nextTick()
  initCharts()
  await nextTick()
  if (flowChart) flowChart.resize()
  if (statusChart) statusChart.resize()
  if (radarChart) radarChart.resize()
  if (responseChart) responseChart.resize()
  if (compareChart) compareChart.resize()
  await fetchData()
}

function getFlowOption() {
  const data = generateFlowData('today')
  return {
    backgroundColor: 'transparent',
    grid: { top: 30, right: 20, bottom: 30, left: 50 },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,10,10,0.9)', borderColor: '#333', textStyle: { color: '#fff', fontSize: 12 } },
    xAxis: { type: 'category', data: data.labels, axisLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#666', fontSize: 10 }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#1a1a1a' } }, axisLabel: { color: '#666', fontSize: 10 } },
    series: [
      { name: 'PV', type: 'line', smooth: true, data: data.pv, lineStyle: { color: '#6366f1', width: 2 }, itemStyle: { color: '#6366f1' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(99,102,241,0.3)' }, { offset: 1, color: 'rgba(99,102,241,0)' }]) }, symbol: 'none' },
      { name: 'UV', type: 'line', smooth: true, data: data.uv, lineStyle: { color: '#a855f7', width: 2 }, itemStyle: { color: '#a855f7' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(168,85,247,0.2)' }, { offset: 1, color: 'rgba(168,85,247,0)' }]) }, symbol: 'none' }
    ]
  }
}

function getStatusOption() {
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: 'rgba(10,10,10,0.9)', borderColor: '#333', textStyle: { color: '#fff' } },
    series: [{
      type: 'pie', radius: ['55%', '80%'], center: ['50%', '50%'],
      label: { show: false },
      data: [
        { value: 7, name: 'ERROR', itemStyle: { color: '#f43f5e' } },
        { value: 3, name: 'WARN', itemStyle: { color: '#f59e0b' } },
        { value: 450, name: 'INFO', itemStyle: { color: '#6366f1' } }
      ],
      emphasis: { scale: true, scaleSize: 6 }
    }]
  }
}

function getRadarOption() {
  return {
    backgroundColor: 'transparent',
    radar: { indicator: [{ name: 'CPU', max: 100 }, { name: '内存', max: 100 }, { name: '磁盘', max: 100 }, { name: '网络', max: 100 }], shape: 'circle', splitNumber: 4, axisName: { color: '#666', fontSize: 11 }, splitLine: { lineStyle: { color: '#1a1a1a' } }, splitArea: { show: false }, axisLine: { lineStyle: { color: '#222' } } },
    series: [{ type: 'radar', data: [{ value: [42, 58, 45, 35], name: '当前', areaStyle: { color: 'rgba(99,102,241,0.2)' }, lineStyle: { color: '#6366f1', width: 2 }, itemStyle: { color: '#6366f1' } }] }]
  }
}

function getResponseOption() {
  const categories = ['0-50ms', '50-100ms', '100-200ms', '200-500ms', '500ms+']
  return {
    backgroundColor: 'transparent',
    grid: { top: 20, right: 20, bottom: 30, left: 80 },
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,10,10,0.9)', borderColor: '#333', textStyle: { color: '#fff' } },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#1a1a1a' } }, axisLabel: { color: '#666', fontSize: 10 } },
    yAxis: { type: 'category', data: categories, axisLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#888', fontSize: 10 }, axisTick: { show: false } },
    series: [{ type: 'bar', data: [{ value: 3520, itemStyle: { color: '#10b981' } }, { value: 2840, itemStyle: { color: '#6366f1' } }, { value: 1560, itemStyle: { color: '#a855f7' } }, { value: 420, itemStyle: { color: '#f59e0b' } }, { value: 85, itemStyle: { color: '#f43f5e' } }], barWidth: 12, itemStyle: { borderRadius: [0, 4, 4, 0] } }]
  }
}

function getCompareOption(mode) {
  if (mode === 'day') {
    const hours = Array.from({ length: 24 }, (_, i) => `${String(i).padStart(2, '0')}:00`)
    const today = [12500,8900,5600,3200,2800,4500,8900,15600,28900,35200,41500,38700,42300,39800,36100,33400,29800,25600,31200,28900,24500,19800,15200,11800]
    const yesterday = today.map(v => Math.round(v * (0.75 + Math.random() * 0.3)))
    return {
      backgroundColor: 'transparent',
      grid: { top: 30, right: 20, bottom: 30, left: 50 },
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,10,10,0.9)', borderColor: '#333', textStyle: { color: '#fff', fontSize: 12 } },
      legend: { data: ['今日', '昨日'], textStyle: { color: '#666', fontSize: 11 }, top: 0 },
      xAxis: { type: 'category', data: hours, axisLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#666', fontSize: 10 }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#1a1a1a' } }, axisLabel: { color: '#666', fontSize: 10 } },
      series: [
        { name: '今日', type: 'bar', data: today, itemStyle: { color: '#6366f1', borderRadius: [2, 2, 0, 0] }, barWidth: 6 },
        { name: '昨日', type: 'bar', data: yesterday, itemStyle: { color: 'rgba(99,102,241,0.25)', borderRadius: [2, 2, 0, 0] }, barWidth: 6 }
      ]
    }
  } else {
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const thisWeek = [312000, 345000, 298000, 367000, 389000, 245000, 198000]
    const lastWeek = thisWeek.map(v => Math.round(v * (0.7 + Math.random() * 0.35)))
    return {
      backgroundColor: 'transparent',
      grid: { top: 30, right: 20, bottom: 30, left: 60 },
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(10,10,10,0.9)', borderColor: '#333', textStyle: { color: '#fff', fontSize: 12 } },
      legend: { data: ['本周', '上周'], textStyle: { color: '#666', fontSize: 11 }, top: 0 },
      xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#666', fontSize: 10 }, axisTick: { show: false } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#1a1a1a' } }, axisLabel: { color: '#666', fontSize: 10 } },
      series: [
        { name: '本周', type: 'bar', data: thisWeek, itemStyle: { color: '#a855f7', borderRadius: [2, 2, 0, 0] }, barWidth: 12 },
        { name: '上周', type: 'bar', data: lastWeek, itemStyle: { color: 'rgba(168,85,247,0.25)', borderRadius: [2, 2, 0, 0] }, barWidth: 12 }
      ]
    }
  }
}

async function fetchData() {
  try {
    const [overviewRes, topApisRes, errorsRes, pvuvRes, logLevelsRes, healthRes, serversRes] = await Promise.allSettled([
      logApi.getOverview(),
      logApi.getTopApis(null, 5),
      logApi.getErrors(5),
      logApi.getPvUvStats(),
      logApi.getLogLevelStats(),
      logApi.getSystemHealth(),
      logApi.getServerMetrics()
    ])

    const getData = (result, fallback = null) => {
      if (result.status === 'fulfilled' && result.value?.code === 200) return result.value.data
      return fallback
    }

    const overviewData = getData(overviewRes)
    if (overviewData) {
      animateValue(liveStats[0], overviewData.totalPv || 0)
      animateValue(liveStats[1], overviewData.totalUv || 0)
      animateValue(liveStats[2], overviewData.todayErrors || 0)
      animateValue(liveStats[3], Math.round(overviewData.avgResponseTime || 0))
    }

    const topApisData = getData(topApisRes, [])
    if (topApisData.length > 0) {
      const maxCount = Math.max(...topApisData.map(a => a.callCount))
      topApis.value = topApisData.slice(0, 5).map(a => ({
        path: a.apiPath,
        count: a.callCount,
        barWidth: (a.callCount / maxCount * 100).toFixed(0)
      }))
    }

    const errorsData = getData(errorsRes, [])
    if (errorsData.length > 0) {
      recentLogs.value = errorsData.slice(0, 5).map(e => ({
        time: e.timestamp ? e.timestamp.substring(11, 16) : '',
        level: e.level || 'ERROR',
        service: e.service || '',
        message: e.message || ''
      }))
    }

    const pvuvData = getData(pvuvRes, [])
    if (pvuvData.length > 0 && flowChart && selectedRange.value === 'today') {
      const hours = pvuvData.map(d => `${String(d.hour).padStart(2, '0')}:00`)
      flowChart.setOption({ xAxis: { data: hours }, series: [{ data: pvuvData.map(d => d.pv) }, { data: pvuvData.map(d => d.uv) }] })
    }

    const logLevelsData = getData(logLevelsRes, [])
    if (logLevelsData.length > 0 && statusChart) {
      const colors = { ERROR: '#f43f5e', WARN: '#f59e0b', INFO: '#6366f1', DEBUG: '#22d3ee' }
      statusChart.setOption({
        series: [{
          data: logLevelsData.map(d => ({
            value: d.count,
            name: d.log_level,
            itemStyle: { color: colors[d.log_level] || '#6366f1' }
          }))
        }]
      }, false, true)
    }

    const healthData = getData(healthRes)
    if (healthData && radarChart) {
      radarChart.setOption({
        series: [{
          data: [{
            value: [healthData.cpuUsage || 0, healthData.memoryUsage || 0, healthData.diskUsage || 0, 35],
            name: '当前'
          }]
        }]
      }, false, true)
    }

    const serversData = getData(serversRes, [])
    if (serversData.length > 0) {
      servers.value = serversData
    }
  } catch (e) {
    console.warn('API fetch error, using fallback data:', e.message)
  }
}

onMounted(() => {
  setTimeout(() => { mounted.value = true }, 100)
  setTimeout(() => {
    initAndFetch()
    const loading = document.getElementById('loading-screen')
    if (loading) loading.classList.add('hidden')
  }, 300)

  updateTime()
  timerInterval = setInterval(updateTime, 1000)
  setInterval(fetchData, 30000)

  window.addEventListener('resize', () => {
    flowChart?.resize()
    statusChart?.resize()
    radarChart?.resize()
    responseChart?.resize()
    compareChart?.resize()
  })
})

onUnmounted(() => {
  clearInterval(timerInterval)
  flowChart?.dispose()
  statusChart?.dispose()
  radarChart?.dispose()
  responseChart?.dispose()
  compareChart?.dispose()
})
</script>

<style scoped>
.app { width: 100%; height: 100vh; position: relative; overflow: hidden; }

.scroll-container {
  width: 100%; height: 100vh; overflow-y: auto; overflow-x: hidden;
  position: relative; z-index: 2;
  scroll-behavior: smooth;
}

.hero {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  position: relative; padding: 0 40px;
}
.hero-content { text-align: center; max-width: 800px; }
.hero-badge {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 6px 16px; border-radius: 100px;
  background: rgba(99,102,241,0.1); border: 1px solid rgba(99,102,241,0.2);
  font-size: 13px; color: rgba(255,255,255,0.7); margin-bottom: 32px;
  opacity: 0; transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1);
}
.hero-badge.visible { opacity: 1; transform: translateY(0); }
.badge-dot { width: 6px; height: 6px; border-radius: 50%; background: #10b981; animation: pulse 2s infinite; }

.hero-title {
  font-size: clamp(48px, 8vw, 96px); font-weight: 800; line-height: 1.05;
  letter-spacing: -0.03em; margin-bottom: 24px;
}
.title-line { display: block; opacity: 0; transform: translateY(40px); transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1); }
.title-line.accent {
  background: linear-gradient(135deg, #6366f1, #a855f7, #22d3ee);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;
}
.hero-title.visible .title-line { opacity: 1; transform: translateY(0); }
.hero-title.visible .title-line.accent { transition-delay: 100ms; }

.hero-desc {
  font-size: 18px; line-height: 1.8; color: var(--text-secondary);
  margin-bottom: 40px; opacity: 0; transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.2s;
}
.hero-desc.visible { opacity: 1; transform: translateY(0); }

.hero-actions { display: flex; gap: 16px; justify-content: center; opacity: 0; transform: translateY(20px); transition: all 0.8s cubic-bezier(0.16, 1, 0.3, 1) 0.3s; }
.hero-actions.visible { opacity: 1; transform: translateY(0); }

.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 14px 28px; border-radius: 12px; border: none;
  background: var(--indigo); color: #fff; font-size: 15px; font-weight: 600;
  cursor: pointer; transition: all var(--transition-normal); font-family: var(--font-sans);
}
.btn-primary:hover { background: #4f46e5; transform: translateY(-2px); box-shadow: 0 8px 30px rgba(99,102,241,0.4); }

.btn-ghost {
  padding: 14px 28px; border-radius: 12px;
  border: 1px solid var(--border-hover); background: transparent;
  color: var(--text-secondary); font-size: 15px; font-weight: 500;
  cursor: pointer; transition: all var(--transition-normal); font-family: var(--font-sans);
}
.btn-ghost:hover { border-color: rgba(255,255,255,0.3); color: #fff; }

.scroll-hint { position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%); display: flex; flex-direction: column; align-items: center; gap: 8px; opacity: 0; transition: all 0.8s ease 0.5s; }
.scroll-hint.visible { opacity: 0.4; }
.scroll-line { width: 1px; height: 40px; background: linear-gradient(to bottom, #fff, transparent); animation: pulse 2s infinite; }
.scroll-hint span { font-size: 10px; letter-spacing: 3px; color: var(--text-muted); }

.stats-section { padding: 80px 40px; }
.section-inner { max-width: 1200px; margin: 0 auto; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card {
  background: var(--bg-card); border: 1px solid var(--border);
  border-radius: var(--radius-lg); padding: 24px;
  display: flex; align-items: flex-start; gap: 16px;
  opacity: 0; transform: translateY(30px); transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.stat-card.visible { opacity: 1; transform: translateY(0); }
.stat-card:hover { border-color: var(--border-hover); background: var(--bg-card-hover); transform: translateY(-2px); }
.stat-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.stat-num { font-size: 28px; font-weight: 700; font-family: var(--font-mono); letter-spacing: -0.02em; }
.stat-unit { font-size: 14px; color: var(--text-secondary); margin-left: 2px; }
.stat-label { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.stat-trend { font-size: 12px; font-weight: 600; margin-top: 4px; display: flex; align-items: center; gap: 2px; }
.stat-trend.up { color: #10b981; }
.stat-trend.down { color: #f43f5e; }

.alert-section { padding: 0 40px 20px; }
.alert-panel {
  background: var(--bg-card); border: 1px solid rgba(244,63,94,0.2);
  border-radius: var(--radius-lg); padding: 20px;
  opacity: 0; transform: translateY(20px); transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
.alert-panel.visible { opacity: 1; transform: translateY(0); }
.alert-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.alert-header h3 { font-size: 15px; font-weight: 600; color: var(--text-secondary); }
.alert-clear { padding: 4px 12px; border-radius: 6px; border: 1px solid var(--border); background: transparent; color: var(--text-muted); font-size: 12px; cursor: pointer; font-family: var(--font-sans); transition: all var(--transition-fast); }
.alert-clear:hover { border-color: rgba(244,63,94,0.4); color: #f43f5e; }
.alert-list { display: flex; flex-direction: column; gap: 10px; }
.alert-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 8px; background: rgba(255,255,255,0.02); }
.alert-item.critical { border-left: 3px solid #f43f5e; }
.alert-item.warning { border-left: 3px solid #f59e0b; }
.alert-item.info { border-left: 3px solid #6366f1; }
.alert-icon { font-size: 14px; flex-shrink: 0; }
.alert-body { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.alert-title { font-size: 13px; font-weight: 500; color: var(--text); }
.alert-desc { font-size: 11px; color: var(--text-muted); }
.alert-time { font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); flex-shrink: 0; }

.dashboard-section { padding: 40px 40px 80px; }
.section-header { margin-bottom: 40px; display: flex; justify-content: space-between; align-items: flex-end; }
.section-header h2 { font-size: 32px; font-weight: 700; letter-spacing: -0.02em; margin-bottom: 8px; }
.section-header p { color: var(--text-secondary); font-size: 16px; }
.section-header.visible { animation: fadeInUp 0.6s ease forwards; }

.btn-export {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px; border: 1px solid var(--border);
  background: transparent; color: var(--text-secondary); font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all var(--transition-normal); font-family: var(--font-sans);
}
.btn-export:hover { border-color: var(--indigo); color: var(--indigo); }

.charts-row { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 16px; opacity: 0; transform: translateY(30px); transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1); }
.charts-row.visible { opacity: 1; transform: translateY(0); }

.compare-row { margin-bottom: 16px; opacity: 0; transform: translateY(30px); transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1); }
.compare-row.visible { opacity: 1; transform: translateY(0); }

.chart-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 20px; transition: all var(--transition-normal); }
.chart-card:hover { border-color: var(--border-hover); }
.chart-card.wide { grid-column: span 1; }
.chart-card.full { width: 100%; }

.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.chart-header h3 { font-size: 15px; font-weight: 600; color: var(--text-secondary); }

.chart-tabs { display: flex; gap: 4px; }
.chart-tabs button { padding: 4px 12px; border-radius: 6px; border: none; background: transparent; color: var(--text-muted); font-size: 12px; cursor: pointer; transition: all var(--transition-fast); font-family: var(--font-sans); }
.chart-tabs button.active { background: rgba(99,102,241,0.15); color: var(--indigo); }
.chart-tabs button:hover { color: var(--text-secondary); }

.chart-area { width: 100%; height: 240px; }

.bottom-row { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; opacity: 0; transform: translateY(30px); transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1); }
.bottom-row.visible { opacity: 1; transform: translateY(0); }

.info-card { background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 20px; transition: all var(--transition-normal); }
.info-card:hover { border-color: var(--border-hover); }
.info-header { margin-bottom: 16px; }
.info-header h3 { font-size: 15px; font-weight: 600; color: var(--text-secondary); }

.top-list { display: flex; flex-direction: column; gap: 12px; }
.top-item { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.top-rank { width: 20px; height: 20px; border-radius: 6px; background: rgba(99,102,241,0.15); color: var(--indigo); display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
.top-path { color: var(--text-secondary); width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; font-family: var(--font-mono); font-size: 11px; }
.top-bar-bg { flex: 1; height: 4px; background: rgba(255,255,255,0.05); border-radius: 2px; overflow: hidden; }
.top-bar { height: 100%; background: var(--indigo); border-radius: 2px; transition: width 1s ease; }
.top-count { color: var(--text-muted); font-family: var(--font-mono); font-size: 11px; min-width: 40px; text-align: right; }

.log-list { display: flex; flex-direction: column; gap: 10px; }
.log-item { display: flex; align-items: center; gap: 8px; font-size: 12px; padding: 6px 0; border-bottom: 1px solid var(--border); }
.log-item:last-child { border-bottom: none; }
.log-level { padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 700; font-family: var(--font-mono); }
.log-level.error { background: rgba(244,63,94,0.15); color: #f43f5e; }
.log-level.warn { background: rgba(245,158,11,0.15); color: #f59e0b; }
.log-service { color: var(--indigo); font-family: var(--font-mono); min-width: 60px; }
.log-msg { color: var(--text-secondary); flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.log-time { color: var(--text-muted); font-family: var(--font-mono); }

.server-list { display: flex; flex-direction: column; gap: 12px; }
.server-item { padding: 10px 0; border-bottom: 1px solid var(--border); }
.server-item:last-child { border-bottom: none; }
.srv-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.srv-icon { font-size: 16px; }
.srv-name { font-size: 13px; font-weight: 500; flex: 1; }
.srv-status { font-size: 11px; padding: 2px 8px; border-radius: 100px; }
.srv-status.online { background: rgba(16,185,129,0.15); color: #10b981; }
.srv-status.offline { background: rgba(244,63,94,0.15); color: #f43f5e; }
.srv-metrics { display: flex; flex-direction: column; gap: 6px; }
.srv-metric { display: flex; align-items: center; gap: 8px; font-size: 11px; color: var(--text-muted); }
.srv-bar-bg { flex: 1; height: 3px; background: rgba(255,255,255,0.05); border-radius: 2px; overflow: hidden; }
.srv-bar { height: 100%; border-radius: 2px; transition: width 0.5s ease; }
.srv-val { font-family: var(--font-mono); min-width: 32px; text-align: right; }

.footer { padding: 40px; border-top: 1px solid var(--border); }
.footer-inner { max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; font-size: 13px; color: var(--text-muted); }

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
  .bottom-row { grid-template-columns: 1fr; }
  .section-header { flex-direction: column; align-items: flex-start; gap: 16px; }
}
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 40px; }
  .hero-actions { flex-direction: column; align-items: center; }
}
</style>
