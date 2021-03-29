import {HistoryManager} from '../tools/history-manager';

declare const GOVUKFrontend: { initAll };
declare const HMRCFrontend: { initAll };

export default function init(app): void {
  GOVUKFrontend.initAll();
  HMRCFrontend.initAll();

  document.cookie = 'jsenabled=true; path=/';

  app.historyManager = new HistoryManager();
}
