(async () => {
  const root = document.querySelector('.drag_verify');
  const handler = root?.querySelector('.dv_handler');
  if (!root || !handler) return 'missing';
  const rr = root.getBoundingClientRect();
  const hr = handler.getBoundingClientRect();
  const startX = Math.round(hr.left + hr.width / 2);
  const y = Math.round(hr.top + hr.height / 2);
  const endX = Math.round(rr.right - hr.width / 2 - 2);
  const mk = (type, x, buttons) => {
    const ev = new MouseEvent(type, { bubbles: true, cancelable: true, clientX: x, clientY: y, screenX: x, screenY: y, buttons });
    Object.defineProperty(ev, 'pageX', { value: x });
    Object.defineProperty(ev, 'pageY', { value: y });
    return ev;
  };
  handler.dispatchEvent(mk('mousedown', startX, 1));
  for (let i = 1; i <= 40; i++) {
    const x = Math.round(startX + (endX - startX) * (i / 40));
    root.dispatchEvent(mk('mousemove', x, 1));
  }
  root.dispatchEvent(mk('mouseup', endX, 0));
  await new Promise((resolve) => setTimeout(resolve, 300));
  return JSON.stringify({ text: root.innerText, left: handler.style.left, bar: root.querySelector('.dv_progress_bar')?.getAttribute('style') });
})()
