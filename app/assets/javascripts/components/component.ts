export abstract class Component {
  protected container: HTMLElement;
  protected app;

  protected constructor(container: HTMLElement, app) {
    this.container = container;
    this.app = app;
  }
}
