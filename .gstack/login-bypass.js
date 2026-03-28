(async () => {
  const drag = document.querySelector('.drag_verify');
  if (!drag || !drag.__vueParentComponent) return 'missing-drag';
  const login = drag.__vueParentComponent.parent?.parent;
  if (!login?.setupState?.handleSubmit) return 'missing-login';
  try {
    if (drag.__vueParentComponent.setupState?.passVerify) {
      drag.__vueParentComponent.setupState.passVerify();
    }
    login.setupState.isPassing = true;
    await new Promise((resolve) => setTimeout(resolve, 150));
    await login.setupState.handleSubmit();
    await new Promise((resolve) => setTimeout(resolve, 1500));
    return JSON.stringify({ href: location.href, title: document.title });
  } catch (error) {
    return 'error:' + String(error);
  }
})()
