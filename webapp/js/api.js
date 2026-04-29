// E-LOG API模块
const API_BASE = 'http://192.168.146.128:8080/api';

let authToken = localStorage.getItem('token') || '';

// 设置认证头
function getHeaders() {
    return authToken ? { 'Authorization': 'Bearer ' + authToken, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };
}

// 认证API
async function login(username, password) {
    const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    return res.json();
}

async function register(user) {
    const res = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user)
    });
    return res.json();
}

// 数据源API
async function getDataSources() {
    const res = await fetch(`${API_BASE}/datasource/list`, { headers: getHeaders() });
    return res.json();
}

async function addDataSource(dataSource) {
    const res = await fetch(`${API_BASE}/datasource/add`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(dataSource)
    });
    return res.json();
}

async function deleteDataSource(id) {
    const res = await fetch(`${API_BASE}/datasource/${id}`, {
        method: 'DELETE',
        headers: getHeaders()
    });
    return res.json();
}

// HDFS API
async function getHdfsFiles(path = '/') {
    const res = await fetch(`${API_BASE}/hdfs/list?path=${encodeURIComponent(path)}`, { headers: getHeaders() });
    return res.json();
}

async function uploadHdfsFile(path, file) {
    const formData = new FormData();
    formData.append('path', path);
    formData.append('file', file);
    const res = await fetch(`${API_BASE}/hdfs/upload`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + authToken },
        body: formData
    });
    return res.json();
}

async function deleteHdfs(path) {
    const res = await fetch(`${API_BASE}/hdfs/delete?path=${encodeURIComponent(path)}`, {
        method: 'DELETE',
        headers: getHeaders()
    });
    return res.json();
}

async function mkdirHdfs(path) {
    const res = await fetch(`${API_BASE}/hdfs/mkdir?path=${encodeURIComponent(path)}`, {
        method: 'POST',
        headers: getHeaders()
    });
    return res.json();
}

async function downloadHdfs(path) {
    const res = await fetch(`${API_BASE}/hdfs/download?path=${encodeURIComponent(path)}`, {
        headers: getHeaders()
    });
    return res.blob();
}