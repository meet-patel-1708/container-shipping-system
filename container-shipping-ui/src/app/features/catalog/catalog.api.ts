import {HttpClient,HttpContext,HttpParams} from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { ListParams, Product, ProductRequest } from './catalog.model';
import { catchError, map, Observable, of } from 'rxjs';
import { SKIP_ERROR_TOAST } from '../../core/interceptors/error-interceptor';
import { PagedResponse } from '../../core/auth/auth.models';
const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({providedIn:'root'})
export class CatalogApi{
    private readonly http = inject(HttpClient);
    private readonly base = `${environment.apiUrl}/api/products`;
    list(p:ListParams) :Observable<PagedResponse<Product>>{
        const q = p.query.trim();
        let params = new HttpParams().set('page',p.page).set('size', p.size).set('sort', p.sort);
        if (q) {
            params = params.set('name', q);
            return this.http.get<PagedResponse<Product>>(`${this.base}/search`, { params });
        }
        return this.http.get<PagedResponse<Product>>(this.base, { params });
    }
    create(body:ProductRequest) : Observable<Product>{
        return this.http.post<Product>(this.base,body,{context:silent()});
    }
    
update(id: number, body: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.base}/${id}`, body, { context: silent() });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  /** 200 = taken, 404 = free. Any other failure returns false: the server's 409 is the final check. */
  codeExists(code: string): Observable<boolean> {
    return this.http
      .get<Product>(`${this.base}/code/${encodeURIComponent(code)}`, { context: silent() })
      .pipe(
        map(() => true),
        catchError(() => of(false)),
      );
  }
}