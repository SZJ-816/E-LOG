// E-LOG 移动端监控App
const API_BASE = 'http://192.168.146.128:8080/api';
let authToken = localStorage.getItem('token') || '';
let currentPage = 'home';
let hdfsPath = '/';

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    updateTime();
    setInterval(updateTime, 1000);
    checkAuth();
    setupNavigation();
    // 每30秒刷新一次
    setInterval(refreshCurrentPage, 30000);
});

// 更新时间
function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.getHours().toString().padStart(2, '0') + ':' + 
        now.getMinutes().toString().padStart(2, '0');
}

// 检查认证
function checkAuth() {
    if (!authToken) {
        showToast('请先登录');
        setTimeout(() => window.location.href = 'index.html', 1000);
        return;
    }
    loadDashboard();
    setStatus('online', '在线');
}

// 设置状态
function setStatus(status, text) {
    const dot = document.getElementById('statusDot');
    const statusText = document.getElementById('statusText');
    dot.className = 'status-dot ' + (status === 'online' ? '' : status);
    statusText.textContent = text;
}

// 导航
function setupNavigation() {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const page = item.dataset.page;
            switchPage(page);
        });
    });
}

function switchPage(page) {
    currentPage = page;
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.querySelector(`[data-page="${page}"]`).classList.add('active');
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById('page-' + page).classList.add('active');
    refreshCurrentPage();
}

function refreshCurrentPage() {
    switch (currentPage) {
        case 'home': loadDashboard(); break;
        case 'datasource': loadDatasources(); break;
        case 'tasks': loadTasks(); break;
        case 'hdfs': loadHdfsFiles(hdfsPath); break;
        case 'profile': loadProfile(); break;
    }
}

// Toast提示
function showToast(msg) {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2000);
}

// 获取请求封装
async function fetchAPI(url, options = {}) {
    try {
        const res = await fetch(API_BASE + url, {
            ...options,
            headers: {
                'Authorization': 'Bearer ' + authToken,
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        return await res.json();
    } catch (e) {
        setStatus('offline', '连接失败');
        return { success: false, message: '网络错误' };
    }
}

// 加载仪表盘
async function loadDashboard() {
    const result = await fetchAPI('/system/dashboard');
    if (result.success) {
        const data = result.data;
        document.getElementById('datasourceCount').textContent = data.datasourceCount || 0;
        document.getElementById('taskCount').textContent = data.taskCount || 0;
        document.getElementById('hdfsStatus').textContent = data.hdfsConnected ? '✅' : '❌';
        setStatus(data.mysqlConnected ? 'online' : 'warning', data.mysqlConnected ? '在线' : '部分离线');
    }
    
    // 加载最近任务
    const tasks = await fetchAPI('/task/list');
    if (tasks.success && tasks.data && tasks.data.length > 0) {
        const recent = tasks.data.slice(0, 3);
        document.getElementById('recentTasks').innerHTML = recent.map(t => `
            <div class="list-item">
                <div>
                    <div class="title">${t.name}</div>
                    <div class="desc">${t.lastRunTime ? new Date(t.lastRunTime).toLocaleString() : '从未运行'}</div>
                </div>
                <span class="badge ${t.lastRunStatus === 'SUCCESS' ? 'success' : 'error'}">${t.lastRunStatus || '待运行'}</span>
            </div>
        `).join('');
    }
    
    // 加载最近数据源
    const ds = await fetchAPI('/datasource/list');
    if (ds.success && ds.data && ds.data.length > 0) {
        const recent = ds.data.slice(0, 3);
        document.getElementById('recentDatasources').innerHTML = recent.map(d => `
            <div class="list-item">
                <div>
                    <div class="title">${d.name}</div>
                    <div class="desc">${d.type} · ${d.description || '无描述'}</div>
                </div>
                <span class="badge info">${d.type}</span>
            </div>
        `).join('');
    }
}

// 加载数据源列表
async function loadDatasources() {
    const result = await fetchAPI('/datasource/list');
    const list = document.getElementById('datasourceList');
    if (result.success && result.data && result.data.length > 0) {
        list.innerHTML = result.data.map(d => `
            <div class="list-item">
                <div>
                    <div class="title">${d.name}</div>
                    <div class="desc">${d.description || '无描述'} · ${d.createTime ? new Date(d.createTime).toLocaleDateString() : ''}</div>
                </div>
                <span class="badge info">${d.type}</span>
            </div>
        `).join('');
    } else {
        list.innerHTML = '<p class="empty">暂无数据源</p>';
    }
}

// 加载任务列表
async function loadTasks() {
    const result = await fetchAPI('/task/list');
    const list = document.getElementById('taskList');
    if (result.success && result.data && result.data.length > 0) {
        list.innerHTML = result.data.map(t => `
            <div class="list-item">
                <div>
                    <div class="title">${t.name}</div>
                    <div class="desc">Cron: ${t.cronExpr || '-'} → ${t.targetPath || '-'}</div>
                </div>
                <div style="text-align:right;">
                    <span class="badge ${t.lastRunStatus === 'SUCCESS' ? 'success' : t.lastRunStatus === 'FAILED' ? 'error' : 'warning'}">${t.status === 1 ? '启用' : '停用'}</span>
                </div>
            </div>
        `).join('');
    } else {
        list.innerHTML = '<p class="empty">暂无任务</p>';
    }
}

// 加载HDFS文件
async function loadHdfsFiles(path) {
    hdfsPath = path;
    document.getElementById('hdfsPath').textContent = path;
    const result = await fetchAPI('/hdfs/list?path=' + encodeURIComponent(path));
    const list = document.getElementById('hdfsList');
    if (result.success && result.data && result.data.length > 0) {
        list.innerHTML = result.data.map(f => `
            <div class="list-item" onclick="${f.isDirectory ? `loadHdfsFiles('${f.path}')` : ''}">
                <div>
                    <div class="title">${f.isDirectory ? '📁' : '📄'} ${f.fileName}</div>
                    <div class="desc">${f.isDirectory ? '目录' : formatSize(f.fileSize)} · ${new Date(f.modificationTime).toLocaleDateString()}</div>
                </div>
                ${f.isDirectory ? '<span class="arrow">›</span>' : ''}
            </div>
        `).join('');
    } else {
        list.innerHTML = '<p class="empty">目录为空</p>';
    }
}

function formatSize(bytes) {
    if (!bytes || bytes === 0) return '-';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

// 加载个人资料
function loadProfile() {
    const username = localStorage.getItem('username') || '-';
    const email = localStorage.getItem('email') || '-';
    const loginTime = localStorage.getItem('loginTime') || '-';
    document.getElementById('username').textContent = username;
    document.getElementById('email').textContent = email;
    document.getElementById('loginTime').textContent = loginTime;
}

// 退出登录
function logout() {
    if (confirm('确定退出?')) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
}