import { Component } from './component';

export class BackLink extends Component {
  constructor(form: HTMLFormElement, app) {
    super(form, app);

    this.bindEvents();
  }

  bindEvents() {
    this.container.addEventListener('click', this.handleClick.bind(this));
  }

  handleClick(e: MouseEvent) {
    e.preventDefault();

    if (document.referrer.indexOf(window.location.host) === -1) {
      return;
    }

    this.app.historyManager.removeLast();

    const previousPageUrl = this.app.historyManager.getPreviousPageUrl();

    if (previousPageUrl) {
      window.location.href = previousPageUrl;
    }
  }
}
