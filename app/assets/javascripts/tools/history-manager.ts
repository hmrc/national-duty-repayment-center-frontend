export class HistoryManager {
  private config;
  private history: string[];

  constructor() {
    this.config = {
      storageKey: 'historyStack'
    };

    this.init();
    this.update();
  }

  private init(): void {
    this.history = JSON.parse(sessionStorage.getItem(this.config.storageKey)) || [];
  }

  private isLastInHistory(path: string): boolean {
    const length = this.history.length;

    return length && this.history[length - 1] === path;
  }

  private update(): void {
    const path = window.location.pathname;

    if (!this.isLastInHistory(path)) {
      this.history.push(path);
      this.save();
    }
  }

  public removeLast(): void {
    this.history.pop();
    this.save();
  }

  public getPreviousPageUrl(): string {
    return this.history[this.history.length - 1];
  }

  private save(): void {
    sessionStorage.setItem(this.config.storageKey, JSON.stringify(this.history));
  }
}
