// E-LOG 主应用逻辑

// 页面状态
let currentHdfsPath = '/';

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    setupForms();
});

// 检查认证状态
function checkAuth() {
    const token = localStorage.getItem('token');
    if (token) {
        authToken = token;
        showMain();
    } else {
        showAuth();
    }
}

// 显示认证页
function showAuth() {
    document.getElementById('authSection').style.display = 'flex';
    document.getElementById('mainSection').style.display = 'none';
}

// 显示主界面
function showMain() {
    document.getElementById('authSection').style.display = 'none';
    document.getElementById('mainSection').style.display = 'flex';
    loadHomeStats();
}

// Tab切换
function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById('loginForm').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('registerForm').style.display = tab === 'register' ? 'block' : 'none';
}

// 表单事件
function setupForms() {
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const password = document.getElementById('loginPassword').value;
        const result = await login(username, password);
        if (result.success && result.token) {
            localStorage.setItem('token', result.token);
            localStorage.setItem('username', username);
            authToken = result.token;
            showMain();
        } else {
            alert(result.message || '登录失败');
        }
    });

    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const user = {
            username: document.getElementById('regUsername').value,
            email: document.getElementById('regEmail').value,
            password: document.getElementById('regPassword').value
        };
        const result = await register(user);
        if (result.success) {
            alert('注册成功，请登录');
            switchTab('login');
            document.querySelectorAll('.tab-btn')[0].click();
        } else {
            alert(result.message || '注册失败');
        }
    });
}

// 退出登录
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    authToken = '';
    showAuth();
}

// 导航
document.querySelectorAll('.nav-item[data-page]').forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const page = item.dataset.page;
        switchPage(page);
    });
});

function switchPage(page) {
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    document.querySelector(`[data-page="${page}"]`)?.classList.add('active');
    document.querySelectorAll('.page').forEach(p => p.style.display = 'none');
    document.getElementById(`page-${page}`).style.display = 'block';
    
    if (page === 'datasource') loadDataSources();
    if (page === 'hdfs') loadHdfsFiles('/');
    if (page === 'tasks') loadTasks();
}

// 加载首页统计
async function loadHomeStats() {
    document.getElementById('datasourceCount').textContent = '0';
    document.getElementById('hdfsFileCount').textContent = '0';
    document.getElementById('taskCount').textContent = '0';
}

// 数据源管理
async function loadDataSources() {
    const result = await getDataSources();
    const tbody = document.getElementById('datasourceTable');
    if (result.success && result.data.length > 0) {
        tbody.innerHTML = result.data.map(ds => `
            <tr>
                <td>${ds.name}</td>
                <td>${ds.type}</td>
                <td>${ds.description || '-'}</td>
                <td>${ds.createTime || '-'}</td>
                <td class="actions">
                    <button class="btn-danger" onclick="removeDataSource(${ds.id})">删除</button>
                </td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#999;">暂无数据</td></tr>';
    }
}

async function removeDataSource(id) {
    if (confirm('确定删除?')) {
        await deleteDataSource(id);
        loadDataSources();
    }
}

function showAddDataSource() {
    const name = prompt('数据源名称:');
    if (name) {
        const type = prompt('数据源类型 (MYSQL/POSTGRESQL/HDFS/API):');
        const description = prompt('描述:');
        addDataSource({ name, type, description, config: '{}' }).then(loadDataSources);
    }
}

// HDFS文件管理
async function loadHdfsFiles(path) {
    currentHdfsPath = path;
    document.getElementById('currentPath').textContent = path;
    const result = await getHdfsFiles(path);
    const tbody = document.getElementById('hdfsTable');
    if (result.success && result.data.length > 0) {
        tbody.innerHTML = result.data.map(f => `
            <tr>
                <td>${f.isDirectory ? '📁' : '📄'} ${f.fileName}</td>
                <td>${f.isDirectory ? '-' : formatSize(f.fileSize)}</td>
                <td>${f.isDirectory ? '目录' : '文件'}</td>
                <td>${new Date(f.modificationTime).toLocaleString()}</td>
                <td class="actions">
                    ${!f.isDirectory ? `<button class="btn-secondary" onclick="downloadFile('${f.path}')">下载</button>` : ''}
                    <button class="btn-danger" onclick="removeFile('${f.path}')">删除</button>
                </td>
            </tr>
        `).join('');
    } else {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#999;">目录为空</td></tr>';
    }
}

function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

async function removeFile(path) {
    if (confirm('确定删除?')) {
        await deleteHdfs(path);
        loadHdfsFiles(currentHdfsPath);
    }
}

async function downloadFile(path) {
    const blob = await downloadHdfs(path);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = path.split('/').pop();
    a.click();
}

function showMkdirDialog() {
    const name = prompt('目录名称:');
    if (name) {
        const newPath = currentHdfsPath === '/' ? '/' + name : currentHdfsPath + '/' + name;
        mkdirHdfs(newPath).then(() => loadHdfsFiles(currentHdfsPath));
    }
}

function refreshHdfs() {
    loadHdfsFiles(currentHdfsPath);
}

// 文件上传
document.getElementById('fileInput').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (file) {
        await uploadHdfsFile(currentHdfsPath, file);
        loadHdfsFiles(currentHdfsPath);
    }
});

// 任务调度
let tasks = [];

async function loadTasks() {
    const res = await fetch('http://192.168.146.128:8080/api/task/list', {
        headers: { 'Authorization': 'Bearer ' + authToken }
    });
    const result = await res.json();
    tasks = result.data || [];
    const list = document.getElementById('taskList');
    if (tasks.length > 0) {
        list.innerHTML = tasks.map(t => `
            <div class="task-item">
                <div>
                    <strong>${t.name}</strong>
                    <p style="color:#999;font-size:12px;margin-top:4px;">
                        数据源: ${t.sourceId || '-'} → ${t.targetPath || '-'} | 
                        Cron: ${t.cronExpr || '-'}
                    </p>
                    <p style="color:#666;font-size:12px;">
                        上次运行: ${t.lastRunTime ? new Date(t.lastRunTime).toLocaleString() : '从未'} | 
                        状态: <span style="color:${t.lastRunStatus === 'SUCCESS' ? 'green' : 'red'}">${t.lastRunStatus || '-'}</span>
                    </p>
                </div>
                <div class="actions">
                    <button class="btn-secondary" onclick="runTask(${t.id})">执行</button>
                    <button class="btn-secondary" onclick="toggleTask(${t.id})">${t.status === 1 ? '停用' : '启用'}</button>
                    <button class="btn-danger" onclick="delTask(${t.id})">删除</button>
                </div>
            </div>
        `).join('');
    } else {
        list.innerHTML = '<p class="empty-tip">暂无定时任务</p>';
    }
}

function showAddTask() {
    const name = prompt('任务名称:');
    if (!name) return;
    const sourceId = prompt('数据源ID (输入数字):');
    const targetPath = prompt('目标HDFS路径:');
    const cronExpr = prompt('Cron表达式 (如: 0 0 * * * ?):');
    
    fetch('http://192.168.146.128:8080/api/task/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + authToken },
        body: JSON.stringify({ name, sourceId: parseInt(sourceId), targetPath, cronExpr })
    }).then(r => r.json()).then(res => {
        if (res.success) loadTasks();
        else alert(res.message);
    });
}

async function runTask(id) {
    const res = await fetch(`http://192.168.146.128:8080/api/task/${id}/execute`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + authToken }
    });
    const result = await res.json();
    alert(result.message);
    loadTasks();
}

async function toggleTask(id) {
    const res = await fetch(`http://192.168.146.128:8080/api/task/${id}/toggle`, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + authToken }
    });
    const result = await res.json();
    if (result.success) loadTasks();
}

async function delTask(id) {
    if (!confirm('确定删除?')) return;
    const res = await fetch(`http://192.168.146.128:8080/api/task/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + authToken }
    });
    const result = await res.json();
    if (result.success) loadTasks();
}