import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { HeaderComponent } from './features/common/components/header-component/header-component';
import { FooterComponent } from './features/common/components/footer-component/footer-component';
import { loadUser } from './features/user/state/user.actions';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  private store = inject(Store);

  ngOnInit(): void {
    if (localStorage.getItem('token')) {
      this.store.dispatch(loadUser());
    }
  }
}
