// Dashboard JavaScript App logic
document.addEventListener("DOMContentLoaded", () => {
    fetchDashboardStats();
    fetchSchema();
});

async function fetchDashboardStats() {
    try {
        const response = await fetch('/api/dashboard/stats');
        if (!response.ok) throw new Error('Failed to fetch stats');
        const data = await response.json();

        // Update metrics
        document.getElementById('total-queries').textContent = data.totalQueriesExecuted || 0;
        document.getElementById('success-queries').textContent = data.successfulQueries || 0;
        document.getElementById('error-queries').textContent = data.sqlErrorsDetected || 0;
        document.getElementById('quiz-attempted').textContent = data.quizQuestionsAttempted || 0;
        document.getElementById('quiz-score').textContent = (data.quizScore || 0) + '%';

        // Render topic progress
        renderTopicProgress(data.topicProgress || []);

        // Render history table
        renderRecentHistory(data.recentQueryHistory || []);

    } catch (err) {
        console.error('Error fetching dashboard stats:', err);
    }
}

function renderTopicProgress(topics) {
    const container = document.getElementById('topic-progress-container');
    if (!container) return;

    if (topics.length === 0) {
        container.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem;">No progress data available.</p>`;
        return;
    }

    container.innerHTML = topics.map(t => `
        <div>
            <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 4px;">
                <span style="font-weight: 600; color: var(--text-primary);">${t.name}</span>
                <span style="color: var(--accent-cyan); font-weight: 700;">${t.percentage}%</span>
            </div>
            <div class="progress-bar-container">
                <div class="progress-fill" style="width: ${t.percentage}%;"></div>
            </div>
        </div>
    `).join('');
}

function renderRecentHistory(logs) {
    const tbody = document.getElementById('recent-history-tbody');
    const countBadge = document.getElementById('history-count');
    if (!tbody) return;

    if (logs.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">
                    No queries executed yet. Open the <a href="sql-playground.html" style="color: var(--accent-cyan);">SQL Playground</a> to start practicing!
                </td>
            </tr>
        `;
        if (countBadge) countBadge.textContent = '0 Queries Recorded';
        return;
    }

    if (countBadge) countBadge.textContent = `${logs.length} Recent Queries`;

    tbody.innerHTML = logs.map(log => {
        const isSuccess = log.is_success;
        const statusBadge = isSuccess
            ? `<span style="background: rgba(34,197,94,0.15); color: var(--accent-green); padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 700;">✓ SUCCESS</span>`
            : `<span style="background: rgba(239,68,68,0.15); color: var(--accent-red); padding: 4px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: 700;">✗ ERROR</span>`;

        const timeFormatted = log.created_at ? new Date(log.created_at).toLocaleTimeString() : 'Just now';

        return `
            <tr>
                <td>${statusBadge}</td>
                <td style="font-family: var(--font-mono); font-size: 0.85rem; color: #38bdf8;">${escapeHtml(log.query_text)}</td>
                <td style="font-size: 0.85rem; color: var(--text-muted);">${log.execution_time_ms} ms</td>
                <td style="font-size: 0.85rem; color: var(--text-muted);">${timeFormatted}</td>
            </tr>
        `;
    }).join('');
}

async function fetchSchema() {
    const container = document.getElementById('schema-preview-container');
    if (!container) return;

    try {
        const response = await fetch('/api/sql/schema');
        if (!response.ok) return;
        const data = await response.json();
        const tables = data.tables || {};

        container.innerHTML = Object.keys(tables).map(tableName => {
            const cols = tables[tableName];
            return `
                <div class="schema-table-card">
                    <div class="schema-table-title">📋 ${tableName}</div>
                    ${cols.map(c => `
                        <div class="schema-col-item">
                            <span class="schema-col-name">${c.name}</span>
                            <span class="schema-col-type">${c.type} ${c.key ? `(${c.key})` : ''}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }).join('');
    } catch (e) {
        console.error('Failed to load schema preview', e);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
