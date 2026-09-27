// SQL Quiz JavaScript Logic
let questionsList = [];
let currentIdx = 0;
let selectedOption = -1;
let answeredState = {}; // questionId -> QuizResult

document.addEventListener("DOMContentLoaded", () => {
    fetchQuestions();
});

async function fetchQuestions() {
    try {
        const response = await fetch('/api/quiz/questions');
        if (!response.ok) throw new Error('Failed to load questions');
        questionsList = await response.json();

        if (questionsList.length > 0) {
            renderCurrentQuestion();
        } else {
            document.getElementById('q-text').textContent = 'No quiz questions available.';
        }
    } catch (e) {
        document.getElementById('q-text').textContent = 'Error loading quiz questions. Ensure backend server is running.';
    }
}

function renderCurrentQuestion() {
    if (currentIdx < 0 || currentIdx >= questionsList.length) return;

    const q = questionsList[currentIdx];
    selectedOption = -1;

    document.getElementById('q-category').textContent = `Category: ${q.category} (${q.difficulty})`;
    document.getElementById('q-counter').textContent = `Question ${currentIdx + 1} of ${questionsList.length}`;
    document.getElementById('q-text').textContent = q.question;

    const optionsContainer = document.getElementById('options-container');
    optionsContainer.innerHTML = '';

    const alreadyAnswered = answeredState[q.id];

    q.options.forEach((optText, i) => {
        const btn = document.createElement('button');
        btn.className = 'option-btn';
        btn.innerHTML = `<span style="font-weight: 700; color: var(--accent-cyan); width: 24px;">${String.fromCharCode(65 + i)}.</span> ${escapeHtml(optText)}`;

        if (alreadyAnswered) {
            btn.disabled = true;
            if (i === alreadyAnswered.correctOptionIndex) {
                btn.classList.add('correct-ans');
            } else if (i === alreadyAnswered.selectedOptionIndex && !alreadyAnswered.correct) {
                btn.classList.add('wrong-ans');
            }
        } else {
            btn.onclick = () => selectOption(i, btn);
        }

        optionsContainer.appendChild(btn);
    });

    // Control buttons & Explanation box
    const expBox = document.getElementById('explanation-container');
    const submitBtn = document.getElementById('submit-btn');
    const nextBtn = document.getElementById('next-btn');
    const prevBtn = document.getElementById('prev-btn');

    prevBtn.disabled = (currentIdx === 0);

    if (alreadyAnswered) {
        expBox.style.display = 'block';
        document.getElementById('explanation-title').textContent = alreadyAnswered.correct ? '✅ Correct Answer!' : '❌ Incorrect Answer';
        document.getElementById('explanation-title').style.color = alreadyAnswered.correct ? 'var(--accent-green)' : 'var(--accent-red)';
        document.getElementById('explanation-text').textContent = alreadyAnswered.explanation;

        submitBtn.style.display = 'none';
        nextBtn.style.display = 'inline-flex';
    } else {
        expBox.style.display = 'none';
        submitBtn.style.display = 'inline-flex';
        submitBtn.disabled = true;
        nextBtn.style.display = 'none';
    }
}

function selectOption(idx, element) {
    selectedOption = idx;
    const allOptions = document.querySelectorAll('.option-btn');
    allOptions.forEach(btn => btn.classList.remove('selected'));
    element.classList.add('selected');

    document.getElementById('submit-btn').disabled = false;
}

async function submitSelectedAnswer() {
    if (selectedOption === -1) return;
    const q = questionsList[currentIdx];

    const submitBtn = document.getElementById('submit-btn');
    submitBtn.disabled = true;

    try {
        const response = await fetch('/api/quiz/submit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ questionId: q.id, selectedOptionIndex: selectedOption })
        });

        if (!response.ok) throw new Error('Submission failed');

        const result = await response.json();
        answeredState[q.id] = result;

        // Update score badge
        if (result.currentScorePercentage !== undefined) {
            document.getElementById('quiz-score-badge').textContent = `${result.currentScorePercentage}%`;
        }

        renderCurrentQuestion();

    } catch (e) {
        alert('Failed to submit answer. Check server connection.');
        submitBtn.disabled = false;
    }
}

function nextQuestion() {
    if (currentIdx < questionsList.length - 1) {
        currentIdx++;
        renderCurrentQuestion();
    }
}

function prevQuestion() {
    if (currentIdx > 0) {
        currentIdx--;
        renderCurrentQuestion();
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
