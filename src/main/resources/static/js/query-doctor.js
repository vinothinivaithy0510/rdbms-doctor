// Query Doctor JavaScript Logic
document.addEventListener("DOMContentLoaded", () => {
    // Check if SQL query was passed via URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    const passedSql = urlParams.get('sql');
    if (passedSql) {
        document.getElementById('doctor-input').value = passedSql;
        analyzeQuery();
    }
});

const PRESETS = {
    1: "SELECT nam FROM students;",
    2: "SELECT s.name, d.department_name\nFROM students s\nJOIN departments d;",
    3: "SELECT * FROM students WHERE department_id = NULL;",
    4: "SELECT department_id, AVG(marks)\nFROM students\nWHERE AVG(marks) > 80\nGROUP BY department_id;",
    5: "SELECT name, age FROM student;"
};

function loadDoctorPreset(id) {
    if (PRESETS[id]) {
        document.getElementById('doctor-input').value = PRESETS[id];
        analyzeQuery();
    }
}

function clearDoctorInput() {
    document.getElementById('doctor-input').value = '';
    document.getElementById('diagnosis-container').style.display = 'none';
}

async function analyzeQuery() {
    const query = document.getElementById('doctor-input').value.trim();
    if (!query) {
        alert('Please enter an SQL query to analyze.');
        return;
    }

    const btn = document.getElementById('analyze-btn');
    btn.disabled = true;
    btn.innerHTML = `Analyzing <span class="spinner"></span>`;

    try {
        const response = await fetch('/api/query-doctor/analyze', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query: query })
        });

        if (!response.ok) throw new Error('Analysis failed');

        const data = await response.json();
        renderDiagnosis(data);

    } catch (err) {
        alert('Could not complete query diagnosis. Check backend server connection.');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `<span>Diagnose Query 🩺</span>`;
    }
}

function renderDiagnosis(data) {
    const container = document.getElementById('diagnosis-container');
    const statusCard = document.getElementById('status-card');
    const statusIcon = document.getElementById('status-icon');
    const statusTitle = document.getElementById('status-title');

    container.style.display = 'flex';

    // Status formatting
    if (data.status === 'LOOKS_GOOD') {
        statusCard.className = 'status-card status-good';
        statusIcon.textContent = '✓';
        statusTitle.textContent = 'QUERY LOOKS GOOD';
    } else if (data.status === 'POSSIBLE_ISSUE') {
        statusCard.className = 'status-card status-issue';
        statusIcon.textContent = '⚠';
        statusTitle.textContent = 'POSSIBLE ISSUE DETECTED';
    } else {
        statusCard.className = 'status-card status-error';
        statusIcon.textContent = '✗';
        statusTitle.textContent = 'SQL ERROR DETECTED';
    }

    // Populate Fields
    document.getElementById('problem-text').textContent = data.problem || 'No specific problem identified.';
    document.getElementById('corrected-query-box').textContent = data.correctedQuery || data.query || '';
    document.getElementById('explanation-text').textContent = data.explanation || '';
    document.getElementById('difficulty-badge').textContent = data.difficulty || 'Beginner';

    // Concept Tags
    const conceptsContainer = document.getElementById('concepts-container');
    const concepts = data.sqlConcepts || ['SQL'];
    conceptsContainer.innerHTML = concepts.map(c => `<span class="concept-tag">${escapeHtml(c)}</span>`).join('');

    // Scroll smoothly to results
    container.scrollIntoView({ behavior: 'smooth' });
}

function copyAndOpenPlayground() {
    const corrected = document.getElementById('corrected-query-box').textContent.trim();
    if (!corrected) return;
    window.location.href = `sql-playground.html?sql=${encodeURIComponent(corrected)}`;
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
