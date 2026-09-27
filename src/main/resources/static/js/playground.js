// SQL Playground JavaScript Logic
document.addEventListener("DOMContentLoaded", () => {
    loadSchema();

    // Check if query was passed from query-doctor or dashboard URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const passedSql = urlParams.get('sql');
    if (passedSql) {
        document.getElementById('sql-input').value = passedSql;
        runQuery();
    }
});

const EXAMPLES = {
    1: "SELECT * FROM students;",
    2: "SELECT name, marks FROM students WHERE marks > 80;",
    3: "SELECT s.student_id, s.name, d.department_name\nFROM students s\nLEFT JOIN departments d ON s.department_id = d.department_id;",
    4: "SELECT d.department_name, AVG(s.marks) AS avg_marks\nFROM students s\nJOIN departments d ON s.department_id = d.department_id\nGROUP BY d.department_name\nHAVING AVG(s.marks) > 80;",
    5: "SELECT s.name, c.course_name, e.grade\nFROM enrollments e\nJOIN students s ON e.student_id = s.student_id\nJOIN courses c ON e.course_id = c.course_id;"
};

function loadExample(id) {
    if (EXAMPLES[id]) {
        document.getElementById('sql-input').value = EXAMPLES[id];
    }
}

function clearEditor() {
    document.getElementById('sql-input').value = '';
    resetResultsView();
}

function sendToDoctor() {
    const query = document.getElementById('sql-input').value.trim();
    if (!query) {
        alert('Please write an SQL query in the editor first!');
        return;
    }
    window.location.href = `query-doctor.html?sql=${encodeURIComponent(query)}`;
}

async function runQuery() {
    const query = document.getElementById('sql-input').value.trim();
    if (!query) {
        alert('Please enter an SQL query to execute.');
        return;
    }

    const runBtn = document.getElementById('run-btn');
    const statusBadge = document.getElementById('status-badge');
    const errorBox = document.getElementById('error-box');
    const errorText = document.getElementById('error-message-text');

    runBtn.disabled = true;
    runBtn.innerHTML = `Running <span class="spinner"></span>`;
    statusBadge.textContent = 'Executing...';
    statusBadge.style.color = 'var(--accent-amber)';
    errorBox.style.display = 'none';

    try {
        const response = await fetch('/api/sql/execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query: query })
        });

        const data = await response.json();

        document.getElementById('execution-time-pill').textContent = `${data.executionTimeMs || 0} ms`;

        if (data.success) {
            statusBadge.textContent = '✓ SUCCESS';
            statusBadge.style.color = 'var(--accent-green)';
            document.getElementById('row-count-pill').textContent = `${data.rowCount} Rows`;

            renderTable(data.columns, data.rows);
        } else {
            statusBadge.textContent = '✗ FAILED';
            statusBadge.style.color = 'var(--accent-red)';
            document.getElementById('row-count-pill').textContent = '0 Rows';

            errorText.textContent = data.errorMessage || 'Unknown execution error occurred.';
            errorBox.style.display = 'block';
            renderEmptyTable('Query failed. Inspect error message above.');
        }
    } catch (err) {
        statusBadge.textContent = '✗ SERVER ERROR';
        statusBadge.style.color = 'var(--accent-red)';
        errorText.textContent = 'Failed to connect to backend server. Make sure Spring Boot application is running.';
        errorBox.style.display = 'block';
    } finally {
        runBtn.disabled = false;
        runBtn.innerHTML = `<span>Run Query ▶</span>`;
    }
}

function renderTable(columns, rows) {
    const tableHead = document.getElementById('table-head');
    const tableBody = document.getElementById('table-body');

    if (!columns || columns.length === 0) {
        renderEmptyTable('Query returned 0 columns or empty result set.');
        return;
    }

    // Render Headers
    tableHead.innerHTML = `
        <tr>
            ${columns.map(col => `<th>${escapeHtml(col)}</th>`).join('')}
        </tr>
    `;

    // Render Rows
    if (!rows || rows.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="${columns.length}" style="text-align: center; color: var(--text-muted); padding: 24px;">
                    Query executed successfully. 0 rows returned matching the criteria.
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = rows.map(row => `
        <tr>
            ${columns.map(col => {
                const val = row[col];
                if (val === null || val === undefined) {
                    return `<td><span class="null-value">NULL</span></td>`;
                }
                return `<td>${escapeHtml(String(val))}</td>`;
            }).join('')}
        </tr>
    `).join('');
}

function renderEmptyTable(msg) {
    document.getElementById('table-head').innerHTML = `<tr><th>Columns</th></tr>`;
    document.getElementById('table-body').innerHTML = `
        <tr>
            <td style="text-align: center; color: var(--text-muted); padding: 24px;">${escapeHtml(msg)}</td>
        </tr>
    `;
}

function resetResultsView() {
    document.getElementById('status-badge').textContent = 'Ready';
    document.getElementById('status-badge').style.color = 'var(--text-secondary)';
    document.getElementById('row-count-pill').textContent = '0 Rows';
    document.getElementById('execution-time-pill').textContent = '0 ms';
    document.getElementById('error-box').style.display = 'none';
    renderEmptyTable('Enter an SQL query above and click Run Query to display results.');
}

async function loadSchema() {
    const container = document.getElementById('playground-schema-grid');
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
                    <div class="schema-table-title" onclick="insertTableName('${tableName}')" style="cursor: pointer;" title="Click to insert table name into query">
                        📋 ${tableName} 
                    </div>
                    ${cols.map(c => `
                        <div class="schema-col-item">
                            <span class="schema-col-name" onclick="insertColName('${c.name}')" style="cursor: pointer;" title="Click to insert column">${c.name}</span>
                            <span class="schema-col-type">${c.type}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }).join('');
    } catch (e) {
        console.error('Failed to load schema', e);
    }
}

function insertTableName(name) {
    const editor = document.getElementById('sql-input');
    editor.value += ` ${name}`;
    editor.focus();
}

function insertColName(name) {
    const editor = document.getElementById('sql-input');
    editor.value += ` ${name}`;
    editor.focus();
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
