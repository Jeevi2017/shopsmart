import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { ProductDTO } from '../models/product.model';

interface BulkUploadResultDTO {
  totalProcessed: number;
  addedCount: number;
  skippedCount: number;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  
  private baseUrl = 'http://localhost:8080/api/products';
  private apiUrl = 'http://localhost:8080/api';
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  constructor() {}

  private getAuthHeaders(): HttpHeaders {
    const accessToken = this.authService.getToken();
    let headers = new HttpHeaders();

    // Add authorization header if a token exists
    if (accessToken) {
      headers = headers.set('Authorization', `Bearer ${accessToken}`);
    }

    // Set Content-Type for JSON payloads
    return headers.set('Content-Type', 'application/json');
  }

  getAllProducts(): Observable<ProductDTO[]> {
    // This endpoint may not require authentication, but for consistency, we'll use headers
    const headers = this.getAuthHeaders();
    return this.http.get<ProductDTO[]>(this.baseUrl, { headers });
  }

  getProductById(id: number): Observable<ProductDTO> {
    const headers = this.getAuthHeaders();
    return this.http.get<ProductDTO>(`${this.baseUrl}/${id}`, { headers });
  }

  createProduct(product: ProductDTO): Observable<ProductDTO> {
    const headers = this.getAuthHeaders();
    return this.http.post<ProductDTO>(this.baseUrl, product, { headers });
  }

  updateProduct(id: number, product: ProductDTO): Observable<ProductDTO> {
    const headers = this.getAuthHeaders();
    return this.http.put<ProductDTO>(`${this.baseUrl}/${id}`, product, {
      headers,
    });
  }

  deleteProduct(id: number): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { headers });
  }

  getProductsByCategoryId(categoryId: number): Observable<ProductDTO[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<ProductDTO[]>(
      `${this.baseUrl}/category/${categoryId}`,
      { headers }
    );
  }

  uploadProductsCsv(formData: FormData): Observable<BulkUploadResultDTO>{
    // Note: The Content-Type header is not set for FormData as the browser handles it.
    const accessToken = this.authService.getToken();
    let headers = new HttpHeaders();
    if (accessToken) {
      headers = headers.set('Authorization', `Bearer ${accessToken}`);
    }
    return this.http.post<BulkUploadResultDTO>(`${this.baseUrl}/upload-csv`, formData, { headers });
  }

  creareMultipleProducts(products: ProductDTO[]): Observable<ProductDTO[]> {
    const headers = this.getAuthHeaders();
    return this.http.post<ProductDTO[]>(`${this.baseUrl}/batch`, products, { headers });
  }

  // ✨ Business Logic: Add the searchProducts method for Elasticsearch integration
  /**
   * Searches for products using the backend's Elasticsearch endpoint.
   * @param query The search term entered by the user.
   * @returns An observable of a ProductDTO array.
   */
  searchProducts(query: string): Observable<ProductDTO[]> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams().set('searchTerm', query);
    return this.http.get<ProductDTO[]>(`${this.baseUrl}/search`, { headers, params });
  }

}