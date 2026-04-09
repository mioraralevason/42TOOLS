import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserEntity } from '../models/user-entity.model';

@Injectable({
  providedIn: 'root'
})
export class UserEntityService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:5000/user-entities';

  getAll(): Observable<UserEntity[]> {
    return this.http.get<UserEntity[]>(this.apiUrl);
  }

  getById(id: number): Observable<UserEntity> {
    return this.http.get<UserEntity>(`${this.apiUrl}/${id}`);
  }

  create(userEntity: { userId: number; entityId: number }): Observable<UserEntity> {
    return this.http.post<UserEntity>(this.apiUrl, userEntity);
  }

  update(id: number, userEntity: { userId: number; entityId: number }): Observable<UserEntity> {
    return this.http.put<UserEntity>(`${this.apiUrl}/${id}`, userEntity);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
