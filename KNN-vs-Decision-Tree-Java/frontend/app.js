let accuracyChartInstance = null;
let timeChartInstance     = null;


document.querySelectorAll('input[name="modelChoice"]').forEach(radio => {
    radio.addEventListener('change', updateParamVisibility);
});

function updateParamVisibility() {
    const model    = getSelectedModel();
    const knnParam = document.getElementById('knnParam');
    const dtParam  = document.getElementById('dtParam');

   
    knnParam.style.display = (model === 'dt')  ? 'none' : 'flex';
    dtParam.style.display  = (model === 'knn') ? 'none' : 'flex';

    
    document.querySelectorAll('.model-option').forEach(el => el.classList.remove('active'));
    const checked = document.querySelector('input[name="modelChoice"]:checked');
    if (checked) checked.closest('.model-option').classList.add('active');
}

function getSelectedModel() {
    return document.querySelector('input[name="modelChoice"]:checked')?.value ?? 'both';
}


document.getElementById('runBtn').addEventListener('click', () => {
    const btn   = document.getElementById('runBtn');
    const k     = document.getElementById('kValue').value;
    const depth = document.getElementById('depthValue').value;
    const model = getSelectedModel();

    btn.textContent = 'Modeller Eğitiliyor...';
    btn.classList.add('loading');
    btn.disabled = true;

    fetch(`http://localhost:8080/api/evaluate?k=${k}&depth=${depth}&model=${model}`)
        .then(res => res.json())
        .then(data => {
            if (data.error) {
                showError(btn, '❌ Sunucu Hatası');
                showErrorDetail(data.error);
                return;
            }
            hideErrorDetail();
            displayResults(data, model);
            renderCharts(data, model);
            btn.textContent = 'Yeniden Analiz Et';
            btn.classList.remove('loading');
            btn.disabled = false;
        })
        .catch(err => {
            console.error('API Hatasi:', err);
            showError(btn, '❌ Bağlantı Hatası');
            showErrorDetail("Sunucu yanıt vermedi. IntelliJ'de Main.java çalışıyor mu?");
        });
});


function displayResults(data, model) {
    const resultsSection = document.getElementById('resultsSection');
    const chartsSection  = document.getElementById('chartsSection');
    const knnCard        = document.getElementById('knnCard');
    const dtCard         = document.getElementById('dtCard');

    resultsSection.style.display = 'block';
    chartsSection.style.display  = 'block';

   
    if (model !== 'dt' && data.knn.accuracy > 0) {
        knnCard.style.display = '';
        document.getElementById('knnAcc').textContent  = data.knn.accuracy.toFixed(2) + '%';
        document.getElementById('knnTime').textContent = data.knn.time + ' ms';
        document.getElementById('knnErr').textContent  = (100 - data.knn.accuracy).toFixed(2) + '%';
        document.getElementById('knnMem').textContent  = formatMemory(data.knn.memoryKB);
    } else {
        knnCard.style.display = 'none';
    }

   
    if (model !== 'knn' && data.dt.accuracy > 0) {
        dtCard.style.display = '';
        document.getElementById('dtAcc').textContent  = data.dt.accuracy.toFixed(2) + '%';
        document.getElementById('dtTime').textContent = data.dt.time + ' ms';
        document.getElementById('dtErr').textContent  = (100 - data.dt.accuracy).toFixed(2) + '%';
        document.getElementById('dtMem').textContent  = formatMemory(data.dt.memoryKB);
    } else {
        dtCard.style.display = 'none';
    }

    
    renderConfusionMatrix(data, model);
}


function renderCharts(data, model) {
    const accCtx  = document.getElementById('accuracyChart').getContext('2d');
    const timeCtx = document.getElementById('timeChart').getContext('2d');

    const knnColor  = 'rgba(127,85,246,0.75)';
    const knnBorder = 'rgba(127,85,246,1)';
    const dtColor   = 'rgba(235,87,87,0.75)';
    const dtBorder  = 'rgba(235,87,87,1)';

    
    const labels = [];
    const accVals = [], timeVals = [];
    const bgColors = [], borderColors = [];

    if (model !== 'dt' && data.knn.accuracy > 0) {
        labels.push('KNN'); accVals.push(data.knn.accuracy); timeVals.push(data.knn.time);
        bgColors.push(knnColor); borderColors.push(knnBorder);
    }
    if (model !== 'knn' && data.dt.accuracy > 0) {
        labels.push('Karar Ağacı (DT)'); accVals.push(data.dt.accuracy); timeVals.push(data.dt.time);
        bgColors.push(dtColor); borderColors.push(dtBorder);
    }

    const commonOpts = {
        color: '#e0e0e0',
        scales: {
            x: { grid: { display: false }, ticks: { color: '#94a3b8' } },
            y: { grid: { color: 'rgba(255,255,255,0.07)' }, ticks: { color: '#94a3b8' } }
        },
        plugins: { legend: { labels: { color: '#fff', font: { family: 'Outfit' } } } }
    };

    if (accuracyChartInstance) accuracyChartInstance.destroy();
    if (timeChartInstance)     timeChartInstance.destroy();

    accuracyChartInstance = new Chart(accCtx, {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Doğruluk Oranı (%)',
                data: accVals,
                backgroundColor: bgColors,
                borderColor: borderColors,
                borderWidth: 2,
                borderRadius: 6
            }]
        },
        options: { ...commonOpts, scales: { ...commonOpts.scales, y: { ...commonOpts.scales.y, beginAtZero: true, max: 100 } } }
    });

    timeChartInstance = new Chart(timeCtx, {
        type: 'bar',
        data: {
            labels,
            datasets: [{
                label: 'Çalışma Süresi (ms)',
                data: timeVals,
                backgroundColor: bgColors,
                borderColor: borderColors,
                borderWidth: 2,
                borderRadius: 6
            }]
        },
        options: { ...commonOpts, scales: { ...commonOpts.scales, y: { ...commonOpts.scales.y, beginAtZero: true } } }
    });
}


function renderConfusionMatrix(data, model) {
    const section   = document.getElementById('confusionSection');
    const container = document.getElementById('confusionContainer');
    container.innerHTML = '';

    const showKnn = (model !== 'dt')  && data.knn && data.knn.categories && data.knn.categories.length > 0;
    const showDt  = (model !== 'knn') && data.dt  && data.dt.categories  && data.dt.categories.length  > 0;

    if (!showKnn && !showDt) { section.style.display = 'none'; return; }
    section.style.display = 'block';

    if (showKnn) container.appendChild(buildMatrixBlock('KNN — Hata Matrisi',          data.knn, 'knn'));
    if (showDt)  container.appendChild(buildMatrixBlock('Karar Ağacı (DT) — Hata Matrisi', data.dt,  'dt'));
}

function buildMatrixBlock(title, result, type) {
    const cats   = result.categories;
    const matrix = result.matrix;
    const maxOff = Math.max(1, ...matrix.flatMap((row, i) => row.filter((_, j) => i !== j)));

    const block = document.createElement('div');
    block.className = 'confusion-block';

    const h4 = document.createElement('h4');
    h4.textContent = title;
    block.appendChild(h4);

    const scroll = document.createElement('div');
    scroll.className = 'cm-scroll';

    const table = document.createElement('table');
    table.className = 'confusion-table';

    
    const thead = document.createElement('thead');
    const hRow  = document.createElement('tr');
    const corner = document.createElement('th');
    corner.className = 'corner';
    corner.textContent = 'Gerçek ↓ / Tahmin →';
    hRow.appendChild(corner);
    cats.forEach(cat => {
        const th = document.createElement('th');
        th.textContent = cat;
        th.title = cat;
        hRow.appendChild(th);
    });
    thead.appendChild(hRow);
    table.appendChild(thead);

    
    const tbody = document.createElement('tbody');
    cats.forEach((actualCat, i) => {
        const tr = document.createElement('tr');

        const rowTd = document.createElement('td');
        rowTd.className = 'row-label';
        rowTd.textContent = actualCat;
        rowTd.title = actualCat;
        tr.appendChild(rowTd);

        cats.forEach((predCat, j) => {
            const td  = document.createElement('td');
            const val = (matrix[i] && matrix[i][j] != null) ? matrix[i][j] : 0;
            td.textContent = val;

            if (i === j) {
                td.classList.add('diagonal');            
            } else if (val === 0) {
                td.classList.add('zero');              
            } else {
                const intensity = Math.min(val / maxOff, 1);
                td.style.background = `rgba(235,87,87,${(intensity * 0.45).toFixed(2)})`;
                td.style.color = intensity > 0.35 ? '#fca5a5' : '#94a3b8';
            }
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    scroll.appendChild(table);
    block.appendChild(scroll);
    return block;
}


function formatMemory(kb) {
    if (!kb || kb <= 0) return '< 1 KB';
    if (kb >= 1024) return (kb / 1024).toFixed(1) + ' MB';
    return kb + ' KB';
}


function showError(btn, msg) {
    btn.textContent = msg;
    btn.classList.remove('loading');
    btn.disabled = false;
}

function showErrorDetail(detail) {
    let box = document.getElementById('errorBox');
    if (!box) {
        box = document.createElement('div');
        box.id = 'errorBox';
        box.style.cssText = 'background:rgba(235,87,87,.12);border:1px solid rgba(235,87,87,.4);border-radius:12px;padding:14px 18px;margin-top:12px;color:#f87171;font-size:.9rem;word-break:break-word;';
        document.getElementById('paramSection').appendChild(box);
    }
    box.textContent = '⚠ ' + detail;
    box.style.display = 'block';
}

function hideErrorDetail() {
    const box = document.getElementById('errorBox');
    if (box) box.style.display = 'none';
}


updateParamVisibility();
