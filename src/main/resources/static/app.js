// ── Config ────────────────────────────────────────────────────
const BASE = '/api';

// ── Month/Year state (persisted in sessionStorage) ────────────
function getSelectedMonth() { return parseInt(sessionStorage.getItem('month') || new Date().getMonth() + 1); }
function getSelectedYear()  { return parseInt(sessionStorage.getItem('year')  || new Date().getFullYear()); }
function saveMonth(m, y)    { sessionStorage.setItem('month', m); sessionStorage.setItem('year', y); }

// ── API helpers ───────────────────────────────────────────────
async function apiFetch(path, options = {}) {
    const res = await fetch(BASE + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${res.status}`);
    }
    if (res.status === 204) return null;
    return res.json();
}

const api = {
    // Expenses
    getExpenses:  (params = {}) => apiFetch('/expenses?' + new URLSearchParams(params)),
    createExpense:(data)        => apiFetch('/expenses', { method: 'POST', body: JSON.stringify(data) }),
    updateExpense:(id, data)    => apiFetch(`/expenses/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteExpense:(id)          => apiFetch(`/expenses/${id}`, { method: 'DELETE' }),

    // Budgets
    getBudgets:  (month, year) => apiFetch(`/budgets?month=${month}&year=${year}`),
    setBudget:   (data)        => apiFetch('/budgets', { method: 'POST', body: JSON.stringify(data) }),
    deleteBudget:(id)          => apiFetch(`/budgets/${id}`, { method: 'DELETE' }),

    // Analytics
    getDashboard:(month, year) => apiFetch(`/analytics/dashboard?month=${month}&year=${year}`),
    getMonthly:  (months = 6)  => apiFetch(`/analytics/monthly?months=${months}`),
    getBudgetComparisons: (month, year) => apiFetch(`/analytics/budgets?month=${month}&year=${year}`),
};

// ── Formatting ────────────────────────────────────────────────
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount || 0);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    return `${months[parseInt(m)-1]} ${parseInt(d)}, ${y}`;
}

const MONTH_NAMES = ['January','February','March','April','May','June',
    'July','August','September','October','November','December'];

const CATEGORY_LABELS = {
    FOOD:'Food', TRANSPORT:'Transport', HOUSING:'Housing',
    ENTERTAINMENT:'Entertainment', HEALTHCARE:'Healthcare',
    SHOPPING:'Shopping', EDUCATION:'Education',
    UTILITIES:'Utilities', TRAVEL:'Travel', OTHER:'Other'
};

const CATEGORIES = Object.keys(CATEGORY_LABELS);

// ── Toast ─────────────────────────────────────────────────────
function toast(msg, type = 'success') {
    const container = document.getElementById('toast');
    const el = document.createElement('div');
    el.className = `toast-msg ${type}`;
    el.textContent = msg;
    container.appendChild(el);
    setTimeout(() => el.remove(), 3000);
}

// ── Modal helpers ─────────────────────────────────────────────
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

// ── Sidebar active link ───────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const page = location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href').split('/').pop();
        if (href === page) link.classList.add('active');
    });
});

// ── Month selector bootstrap ──────────────────────────────────
function initMonthSelector(onChangeCb) {
    const mSel = document.getElementById('selMonth');
    const ySel = document.getElementById('selYear');
    if (!mSel || !ySel) return;

    // Populate months
    MONTH_NAMES.forEach((name, i) => {
        const opt = new Option(name, i + 1);
        mSel.appendChild(opt);
    });
    mSel.value = getSelectedMonth();

    // Populate years
    const curYear = new Date().getFullYear();
    for (let y = curYear - 2; y <= curYear + 1; y++) {
        ySel.appendChild(new Option(y, y));
    }
    ySel.value = getSelectedYear();

    const onChange = () => {
        saveMonth(mSel.value, ySel.value);
        onChangeCb(parseInt(mSel.value), parseInt(ySel.value));
    };

    mSel.addEventListener('change', onChange);
    ySel.addEventListener('change', onChange);
}