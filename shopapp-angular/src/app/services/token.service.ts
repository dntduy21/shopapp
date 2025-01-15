import { Inject, Injectable } from '@angular/core';
import { CommonModule, DOCUMENT } from '@angular/common';
import { JwtHelperService } from '@auth0/angular-jwt';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root',
})
export class TokenService {
    private readonly TOKEN_KEY = 'access_token';
    private jwtHelperService = new JwtHelperService();
    localStorage?: Storage;

    constructor(private http: HttpClient, @Inject(DOCUMENT) private document: Document) {
        this.localStorage = document.defaultView?.localStorage;
    }
    //getter/setter
    getToken(): string {
        return this.localStorage?.getItem(this.TOKEN_KEY) ?? '';
    }
    setToken(token: string): void {
        this.localStorage?.setItem(this.TOKEN_KEY, token);
    }
    getUserId(): number {
        let token = this.getToken();
        if (!token) {
            return 0;
        }
        let userObject = this.jwtHelperService.decodeToken(token);
        return 'userId' in userObject ? parseInt(userObject['userId']) : 0;
    }

    removeToken(): void {
        this.localStorage?.removeItem(this.TOKEN_KEY);
    }
    isTokenExpired(): boolean {
        if (this.getToken() == null) {
            return false;
        }
        return this.jwtHelperService.isTokenExpired(this.getToken()!);
    }
}
