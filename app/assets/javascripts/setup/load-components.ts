import {BackLink} from '../components/back-link';
import {Component} from '../components/component';
import {FileUpload} from '../components/file-upload';

export default function loadComponents(app): void {
  loadComponent(BackLink, '.app-back-link', app);
  loadComponent(FileUpload, '.file-upload', app);
}

function loadComponent(component: new(container: HTMLElement, app) => Component, selector: string, app): void {
  const containers: HTMLElement[] = Array.from(document.querySelectorAll(selector));

  containers.forEach(container => {
    new component(container, app);
  });
}
