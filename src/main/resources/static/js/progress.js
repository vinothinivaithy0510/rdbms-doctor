// Progress Page JavaScript Logic
document.addEventListener("DOMContentLoaded", () => {
    fetchProgressData();
});

async function fetchProgressData() {
    try {
        const response = await fetch('/api/dashboard/stats');
        if (!response.ok) throw new Error('Failed to fetch stats');
        const data = await response.json();

        // Total execution logs
        const total = data.totalQueriesExecuted || 0;
        const success = data.successfulQueries || 0;
        const successRate = total > 0 ? Math.round((success / total) * 100) : 0;

        document.getElementById('p-total-queries').textContent = total;
        document.getElementById('p-success-rate').textContent = `${successRate}%`;
        document.getElementById('p-quiz-score').textContent = `${data.quizScore || 0}%`;

        // Render Topics Breakdown
        renderTopicBreakdown(data.topicProgress || []);

        // Render History Table
        renderProgressLogs(data.recentQueryHistory || []);

    } catch (e) {
        console.error('Error loading progress stats:', e);
    }
}

function renderTopicBreakdown(topics) {
    const container = document.getElementById('progress-topics-list');
    if (!container) return;

    if (topics.length === 0) {
        container.innerHTML = `<p style="color: var(--text-muted);">No topic progress recorded yet.</p>`;
        return;
    }

    container.innerHTML = topics.map(t => {
        let statusBadgeClass = 'rgba(56,189,248,0.2)';
        let statusColor = 'var(--accent-cyan)';

        if (t.status === 'Mastered') {
            statusBadgeClass = 'rgba(34,197,94,0.2)';
            statusColor = 'var(--accent-green)';
        } else if (t.status === 'In Progress') {
            statusBadgeClass = 'rgba(245,158,11,0.2)';
            statusColor = 'var(--accent-amber)';
        }

        return `
            <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--border-color); padding: 18px 20px; border-radius: var(--radius-sm);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                    <div>
                        <strong style="font-size: 1rem; color: var(--text-primary);">${t.name}</strong>
                        <span style="display: block; font-size: 0.8rem; color: var(--text-muted); margin-top: 2px;">Key Syntax: ${t.details}</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <span style="background: ${statusBadgeClass}; color: ${statusColor}; padding: 4px 12px; border-radius: 12px; font-size: 0.78rem; font-weight: 700;">${t.status}</span>
                        <span style="font-weight: 800; color: var(--text-primary); font-size: 1.1rem; min-width: 45px; text-align: right;">${t.percentage}%</span>
                    </div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-fill" style="width: ${t.percentage}%;"></div>
                </div>
            </div>
        `;
    }).join('');
}

function renderProgressLogs(logs) {
    const tbody = document.getElementById('p-history-tbody');
    const logCount = document.getElementById('p-log-count');
    if (!tbody) return;

    if (logs.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 20px;">
                    No query activity recorded yet. Run queries in the <a href="sql-playground.html" style="color: var(--accent-cyan);">SQL Playground</a>.
                </td>
            </tr>
        `;
        if (logCount) logCount.textContent = '0 Records';
        return;
    }

    if (logCount) logCount.textContent = `${logs.length} Total Logs`;

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

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
