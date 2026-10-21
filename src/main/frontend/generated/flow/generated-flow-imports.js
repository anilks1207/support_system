import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/app-layout/src/vaadin-app-layout.js';
import '@vaadin/horizontal-layout/src/vaadin-horizontal-layout.js';
import '@vaadin/app-layout/src/vaadin-drawer-toggle.js';
import '@vaadin/button/src/vaadin-button.js';
import '@vaadin/tooltip/src/vaadin-tooltip.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/side-nav/src/vaadin-side-nav.js';
import '@vaadin/side-nav/src/vaadin-side-nav-item.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/icon/src/vaadin-icon.js';
import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/text-area/src/vaadin-text-area.js';
import '@vaadin/notification/src/vaadin-notification.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import 'Frontend/generated/jar-resources/flow-component-directive.js';
import '@vaadin/grid/src/vaadin-grid.js';
import '@vaadin/grid/src/vaadin-grid-column.js';
import '@vaadin/grid/src/vaadin-grid-sorter.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/vaadin-grid/gridConnector.ts';
import 'Frontend/generated/jar-resources/vaadin-grid/vaadin-grid-flow-selection-column.ts';
import '@vaadin/grid/src/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/context-menu/src/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/vaadin-context-menu/contextMenuConnector.ts';
import 'Frontend/generated/jar-resources/vaadin-context-menu/contextMenuTargetConnector.ts';
import '@vaadin/combo-box/src/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/vaadin-combo-box/comboBoxConnector.ts';
import '@vaadin/multi-select-combo-box/src/vaadin-multi-select-combo-box.js';
import '@vaadin/number-field/src/vaadin-number-field.js';
import '@vaadin/select/src/vaadin-select.js';
import '@vaadin/login/src/vaadin-login-form.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '169ea0c536ac407fbead11c94bbafa0ec3572b194fee810d7c4a7cc8aee0fc01') {
    pending.push(import('./chunks/chunk-d10f4205fd74bd8f365302be694fdc5754da3699149ff239adaf78c0dd087c5c.js'));
  }
  if (key === '58012629578571fa2f36c3110f9ae31c8183191c0558e9cdc7a95b5f419c7987') {
    pending.push(import('./chunks/chunk-2634e693894af0ed4daebbf9c38e04c553fc9979e09aa7e06e1f13468ac88980.js'));
  }
  if (key === 'a4b9ede15682ea0e81589a9b4e7df6bf02ac6c1d578fa209cba4bb55271f5b87') {
    pending.push(import('./chunks/chunk-c31fd3e60baf1c133aab54404b9e9af3bbf9ce206103d31dcb2dcbbee272d1e5.js'));
  }
  if (key === '77ee5eb6dc7d08f19042cf64de21d85715243bee289e4f36b69f710f34cf1863') {
    pending.push(import('./chunks/chunk-d5e5136315be8291548fde62658190f8e005aceb8f7470dee4bcba8c37130564.js'));
  }
  if (key === '9f8d8e13370a511cf3f0fecabcdf0b16df7400c8385be46b6bf544b4090348c1') {
    pending.push(import('./chunks/chunk-02ab7ef1db47cae381f3e5dc33c500e7682f408636d8a2c30eca64513d42cf9e.js'));
  }
  if (key === '41cfc95a5c0a017b1b47a69cc77d56b0542d2d2ba4b91a9020d78d290fc85588') {
    pending.push(import('./chunks/chunk-2634e693894af0ed4daebbf9c38e04c553fc9979e09aa7e06e1f13468ac88980.js'));
  }
  if (key === '8a4d63df0cf889211c8ef0050dceebe24111cf727f2c47c51eb70eaf0650a1c6') {
    pending.push(import('./chunks/chunk-fa1aa6bf5c41f1f44bd48e93e0e8537bd97e6aaecc1e13ff37e3330be79e68e5.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}