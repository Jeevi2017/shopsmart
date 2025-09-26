import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { OrderDTO } from '../models/order-models';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private baseUrl = 'http://localhost:8080/api/orders';

  private http = inject(HttpClient);
  private authService = inject(AuthService);

  constructor() {}

  /**
   * Build Authorization headers with JWT token
   */
  private getAuthHeaders(): HttpHeaders {
    const accessToken = this.authService.getToken();
    if (!accessToken) {
      throw new Error('Access token not found. User not authenticated.');
    }
    return new HttpHeaders({
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    });
  }

  /**
   * Get all orders (Admin use)
   */
  getAllOrders(): Observable<OrderDTO[]> {
    return this.http
      .get<OrderDTO[]>(this.baseUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get all orders for a specific customer
   */
  getOrdersByCustomerId(customerId: number): Observable<OrderDTO[]> {
    return this.http
      .get<OrderDTO[]>(`${this.baseUrl}/customer/${customerId}`, {
        headers: this.getAuthHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get single order by its ID
   */
  getOrderById(orderId: number): Observable<OrderDTO> {
    return this.http
      .get<OrderDTO>(`${this.baseUrl}/${orderId}`, {
        headers: this.getAuthHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create a new order
   */
  createOrder(orderDTO: OrderDTO): Observable<OrderDTO> {
    return this.http
      .post<OrderDTO>(this.baseUrl, orderDTO, {
        headers: this.getAuthHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Update an existing order
   */
  updateOrder(orderId: number, orderDTO: OrderDTO): Observable<OrderDTO> {
    return this.http
      .put<OrderDTO>(`${this.baseUrl}/${orderId}`, orderDTO, {
        headers: this.getAuthHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete an order
   */
  deleteOrder(orderId: number): Observable<void> {
    return this.http
      .delete<void>(`${this.baseUrl}/${orderId}`, {
        headers: this.getAuthHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Create order from customer's cart
   */
  createOrderFromCart(customerId: number): Observable<OrderDTO> {
    return this.http
      .post<OrderDTO>(`${this.baseUrl}/from-cart/${customerId}`, null, {
        headers: this.getAuthHeaders(),
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Generate PDF report for an order
   */
  generateOrderReportPdf(orderId: number): Observable<Blob> {
    return this.http
      .get(`${this.baseUrl}/${orderId}/report/pdf`, {
        headers: this.getAuthHeaders(),
        responseType: 'blob',
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Generic HTTP error handler
   */
  private handleError(error: any) {
    console.error('OrderService HTTP error:', error);
    if (error.status === 403) {
      return throwError(() => new Error('Access denied to this order.'));
    } else if (error.status === 404) {
      return throwError(() => new Error('Order not found.'));
    } else {
      return throwError(
        () => new Error(error?.error?.message || 'Server error')
      );
    }
  }
}
