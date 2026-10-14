(function () {
  // -------------------- STATE --------------------
  const alertsLocal = [];              // local (in-memory) alerts for immediate feedback
  let html5Scanner = null;             // Html5QrcodeScanner instance
  let foodEntryInitialized = false;
  let cachedShelfLifeDays = null;
  let wasteChart;

  // -------------------- NAV / SECTIONS --------------------
  function showSection(id){
    const wasFoodEntryActive = document.getElementById('food-entry')?.classList.contains('active');

    // Switch visible section
    document.querySelectorAll('.section').forEach(sec=>sec.classList.remove('active'));
    document.getElementById(id).classList.add('active');

    // Stop camera if leaving Food Entry
    if (wasFoodEntryActive && id !== 'food-entry') stopScanner();

    // Section hooks
    if (id === 'dashboard'){ onEnterDashboard(); }
    if (id === 'food-entry'){
      if (!foodEntryInitialized) { initFoodEntryBindings(); foodEntryInitialized = true; }
      startScanner();
    }
    if (id === 'food-list'){ loadFoodList(); }
    if (id === 'iot-devices'){ loadDevices(); }
    if (id === 'reports'){ drawWasteChart(); }
    if (id === 'alert-history'){ loadAlerts(0); }
  }
  window.showSection = showSection;

  document.addEventListener('DOMContentLoaded', () => {
    // Force Dashboard as default on first render
    document.querySelectorAll('.section').forEach(sec=>sec.classList.remove('active'));
    const dash = document.getElementById('dashboard');
    if (dash) dash.classList.add('active');
    stopScanner();
    // Kick off dashboard sensors
    updateSensors();
    setInterval(updateSensors, 5000);
  });

  // -------------------- ALERTS (LOCAL UI FEEDBACK) --------------------
  function logAlert(msg, type){
    const ts = new Date().toLocaleString();
    const entry = `${ts} → ${msg}`;
    alertsLocal.unshift({msg: entry, type});
    // If you want to show local alerts inside the Alerts tab along with server alerts,
    // you can append them after loadAlerts() result. For now, we keep them separate.
  }
  window.logAlert = logAlert;

  // -------------------- ALERTS (SERVER-PERSISTED API) --------------------
  async function loadAlerts(page = 0) {
    try {
      const res = await fetch(`/api/alerts?page=${page}&size=50&onlyActive=true`);
      if (!res.ok) throw new Error('alerts fetch failed');
      const data = await res.json();
      const ul = document.getElementById("alert-log");
      if (!ul) return;

      if (!data.content || data.content.length === 0) {
        ul.innerHTML = '<li class="ok">🔔 No active alerts</li>';
        return;
      }

      ul.innerHTML = data.content.map(a => {
        const cls = a.type === 'critical' ? 'critical' : 'ok';
        const when = a.createdAt?.replace('T',' ').slice(0,16) || '';
        const food = a.foodName ? ` – <i>${escapeHtml(a.foodName)}</i>` : '';
        return `<li class="${cls}">
                  🔔 ${escapeHtml(a.message)}${food}
                  <small style="opacity:.7"> [${when}]</small>
                  <button data-id="${a.id}" class="alert-dismiss" style="float:right">Dismiss</button>
                </li>`;
      }).join("");

      // Wire dismiss buttons
      ul.querySelectorAll('.alert-dismiss').forEach(btn => {
        btn.addEventListener('click', () => dismissAlert(btn.getAttribute('data-id')));
      });

    } catch (e) { console.error(e); }
  }
  async function dismissAlert(id){
    try{
      const headerName = document.getElementById("csrfHeaderName3")?.value
                       || document.getElementById("csrfHeaderName")?.value;
      const token = document.getElementById("csrfToken3")?.value
                 || document.getElementById("csrfToken")?.value;

      const res = await fetch(`/api/alerts/dismiss/${id}`, {
        method: 'POST',
        headers: headerName && token ? { [headerName]: token } : {}
      });
      if (res.ok) loadAlerts(0);
    }catch(e){ console.error(e); }
  }
  // (expose only if needed outside)
  // window.loadAlerts = loadAlerts; window.dismissAlert = dismissAlert;

  // -------------------- DASHBOARD SENSORS --------------------
  async function updateSensors(){
    try{
      const res = await fetch('/api/sensors');
      if (!res.ok) return;
      const data = await res.json();
      document.getElementById("temp").textContent = data.temperature.toFixed(1) + " °C";
      document.getElementById("humidity").textContent = data.humidity + " %";
      document.getElementById("expiry").textContent = data.nearExpiry;
    }catch(e){ console.error(e); }
  }
  let expiryCheckTimer = null;
  function onEnterDashboard(){
    // periodic background check (optional future use)
    if (!expiryCheckTimer) {
      expiryCheckTimer = setInterval(() => {
        // could poll /api/alerts or /food to trigger client-side notices if you want
      }, 6 * 60 * 60 * 1000);
    }
  }

  // -------------------- BARCODE LOOKUP (OFF + LOCAL CATALOG) --------------------
  async function lookupByBarcode(payload){
    if(!payload) return;
    try{
      const res = await fetch('/barcode/lookup?payload=' + encodeURIComponent(payload));
      if(!res.ok) return;
      const data = await res.json();

      if (data.barcode) document.getElementById('barcode').value = data.barcode;
      if (data.name) document.getElementById('foodName').value = data.name;
      if (data.ingredients) document.getElementById('ingredients').value = data.ingredients;
      if (data.expiryDate) document.getElementById('expiryDate').value = data.expiryDate; // GS1/off case

      if (typeof data.shelfLifeDays === 'number') {
        cachedShelfLifeDays = data.shelfLifeDays;
        document.getElementById('shelfLifeDays').value = data.shelfLifeDays;
      } else {
        cachedShelfLifeDays = null;
      }

      maybeAutoCalcExpiry(); // if we have entry date + shelf-life
    }catch(e){ console.error(e); }
  }

  function maybeAutoCalcExpiry(){
    const entry = document.getElementById('entryDate').value;
    const expiry = document.getElementById('expiryDate').value;
    const shelf = parseInt(document.getElementById('shelfLifeDays').value || cachedShelfLifeDays || 0, 10);
    if (entry && !expiry && shelf > 0) {
      const d = new Date(entry);
      d.setDate(d.getDate() + shelf);
      const yyyy = d.getFullYear();
      const mm = String(d.getMonth()+1).padStart(2,'0');
      const dd = String(d.getDate()).padStart(2,'0');
      document.getElementById('expiryDate').value = `${yyyy}-${mm}-${dd}`;
    }
  }

  // -------------------- FOOD ENTRY BINDINGS --------------------
  function initFoodEntryBindings(){
    // barcode input listeners
    const barcodeInput = document.getElementById('barcode');
    if (barcodeInput) {
      barcodeInput.addEventListener('change', () => lookupByBarcode(barcodeInput.value));
      barcodeInput.addEventListener('blur', () => lookupByBarcode(barcodeInput.value));
    }

    // entry/shelf-life → auto-calc expiry
    document.getElementById('entryDate')?.addEventListener('change', maybeAutoCalcExpiry);
    document.getElementById('shelfLifeDays')?.addEventListener('input', maybeAutoCalcExpiry);

    // form submit
    const foodForm = document.getElementById('foodForm');
    if (foodForm) {
      foodForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById("foodName").value;
        const barcode = document.getElementById("barcode").value;
        const expiry = document.getElementById("expiryDate").value;
        const entryDate = document.getElementById("entryDate").value;
        const ingredients = document.getElementById("ingredients").value;
        const saveDefault = document.getElementById("saveDefault").checked;
        const shelfLifeDays = document.getElementById("shelfLifeDays").value;

        const headerName = document.getElementById("csrfHeaderName").value;
        const token = document.getElementById("csrfToken").value;

        const params = new URLSearchParams();
        params.append("name", name);
        params.append("barcode", barcode);
        params.append("entryDate", entryDate);
        if (expiry) params.append("expiryDate", expiry);
        if (ingredients) params.append("ingredients", ingredients);
        params.append("saveDefault", saveDefault ? "true" : "false");
        if (shelfLifeDays) params.append("shelfLifeDays", shelfLifeDays);

        try{
          const res = await fetch('/food', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded', [headerName]: token},
            body: params.toString()
          });
          if(res.ok){
            logAlert(`Added ${name}${expiry ? ' (Expiry: ' + expiry + ')' : ''}`,"ok");
            foodForm.reset();
            cachedShelfLifeDays = null;
            const payloadBox = document.getElementById('scanPayload');
            if (payloadBox) payloadBox.value = '';
            showSection("food-list");
          } else { alert("Failed to add item"); }
        }catch(err){ console.error(err); }
      });
    }
  }

  // -------------------- SCANNER CONTROL (ONLY ON FOOD ENTRY) --------------------
  function onScanSuccess(decodedText) {
    const input = document.getElementById("barcode");
    if (input) input.value = decodedText;
    const box = document.getElementById("scanPayload");
    if (box) box.value = decodedText; // show raw payload
    lookupByBarcode(decodedText);
  }
  function startScanner(){
    if (!window.Html5QrcodeScanner) return;
    if (html5Scanner) return;
    const reader = document.getElementById("reader");
    if (!reader || reader.offsetParent === null) return;
    html5Scanner = new Html5QrcodeScanner("reader",{fps:10,qrbox:200});
    html5Scanner.render(onScanSuccess);
  }
  function stopScanner(){
    if (html5Scanner && html5Scanner.clear) {
      html5Scanner.clear().catch(()=>{});
    }
    html5Scanner = null;
  }

  // -------------------- FOOD LIST --------------------
  async function loadFoodList(){
    try{
      const res = await fetch('/food');
      const data = await res.json();
      const table = document.getElementById("foodTable");
      table.innerHTML = "<tr><th>Name</th><th>Barcode</th><th>Expiry</th></tr>";
      data.forEach(item=>{
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${escapeHtml(item.name)}</td>
                        <td>${escapeHtml(item.barcode || '')}</td>
                        <td>${item.expiryDate || ''}</td>`;
        table.appendChild(tr);
      });
    }catch(e){ console.error(e); }
  }

  // -------------------- DEVICES --------------------
  async function connectDevice(){
    const deviceType = document.getElementById("deviceType").value;
    const headerName = document.getElementById("csrfHeaderName2").value;
    const token = document.getElementById("csrfToken2").value;

    const params = new URLSearchParams();
    params.append("deviceType", deviceType);

    try{
      const res = await fetch('/devices/connect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded', [headerName]: token },
        body: params.toString()
      });
      if(res.ok){
        logAlert(`${deviceType} connected successfully!`,"ok");
        loadDevices();
      } else { alert("Failed to connect device"); }
    }catch(e){ console.error(e); }
  }
  window.connectDevice = connectDevice;

  async function loadDevices(){
    try{
      const res = await fetch('/devices');
      const data = await res.json();
      const ul = document.getElementById("deviceList");
      ul.innerHTML = "";
      data.forEach(d=>{
        const li = document.createElement('li');
        li.textContent = `${d.deviceType} - Connected ✅ (${new Date(d.connectedAt).toLocaleString()})`;
        ul.appendChild(li);
      });
    }catch(e){ console.error(e); }
  }

  // -------------------- REPORTS --------------------
  async function drawWasteChart(){
    try{
      const res = await fetch('/api/reports/waste');
      const data = await res.json();
      const ctx = document.getElementById('wasteChart');
      if (wasteChart) { wasteChart.destroy(); }
      wasteChart = new Chart(ctx, {
        type:'bar',
        data:{ labels:data.labels, datasets:[{label:'Food Waste (kg)', data:data.values,
          backgroundColor:['#6a5acd','#9370db','#a9a9a9','#483d8b']}] }
      });
    }catch(e){ console.error(e); }
  }

  // -------------------- UTILS --------------------
  function escapeHtml(str){
    return String(str || '').replace(/[&<>"'`=\/]/g, s => ({
      '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;','/':'&#x2F;','`':'&#x60;','=':'&#x3D;'
    }[s]));
  }
})();
