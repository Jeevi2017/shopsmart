import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { PaymentDTO } from '../models/payment-models';
import {
  RazorpayOrderRequestDTO,
  RazorpayOrderResponseDTO,
  RazorpayPaymentCaptureRequestDTO,
} from '../models/razorpay-models';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private baseUrl = 'http://localhost:8080/api/payments';
  
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  
  constructor() {}

  /**
   * Generates and returns HttpHeaders with the JWT access token.
   * Throws an error if the token is not found.
   */
  private getAuthHeaders(contentType = 'application/json'): HttpHeaders {
    const accessToken = this.authService.getToken();
    if (!accessToken) {
      throw new Error('Access token not found. User not authenticated.');
    }
    return new HttpHeaders({
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': contentType,
    });
  }

  // ==================== Business Logic for Payments ====================

  /**
   * Processes a manual payment for a given order.
   * @param orderId The ID of the order to process the payment for.
   * @param paymentDTO The payment details (e.g., payment method).
   * @returns An observable of the processed PaymentDTO.
   */
  processPayment(
    orderId: number,
    paymentDTO: PaymentDTO
  ): Observable<PaymentDTO> {
    const headers = this.getAuthHeaders();
    return this.http.post<PaymentDTO>(
      `${this.baseUrl}/order/${orderId}`,
      paymentDTO,
      { headers }
    );
  }

  /**
   * Creates a Razorpay order to initiate an online payment.
   * @param orderId The internal order ID.
   * @param amount The payment amount.
   * @param currency The currency code (e.g., 'INR').
   * @param receipt The order receipt string.
   * @returns An observable of the Razorpay order response.
   */
  createRazorpayOrder(
    orderId: number,
    amount: number,
    currency: string,
    receipt: string
  ): Observable<RazorpayOrderResponseDTO> {
    const headers = this.getAuthHeaders();
    const requestBody: RazorpayOrderRequestDTO = {
      orderId: orderId, // Use the correct field name from RazorpayOrderRequestDTO
      amount,
      currency,
      receipt
    };
    return this.http.post<RazorpayOrderResponseDTO>(
      `${this.baseUrl}/razorpay/order`,
      requestBody,
      { headers }
    );
  }

  /**
   * Captures a Razorpay payment after client-side verification.
   * @param captureRequest The Razorpay payment capture details.
   * @returns An observable of the PaymentDTO for the captured payment.
   */
  captureRazorpayPayment(
    captureRequest: RazorpayPaymentCaptureRequestDTO
  ): Observable<PaymentDTO> {
    const headers = this.getAuthHeaders();
    return this.http.post<PaymentDTO>(
      `${this.baseUrl}/razorpay/capture`,
      captureRequest,
      { headers }
    );
  }

  /**
   * Fetches a QR code image for a specific order.
   * This is the core business logic for QR code generation.
   * @param orderId The ID of the order for which to generate the QR code.
   * @returns An observable of the QR code image as a binary ArrayBuffer.
   */
  getQrCode(orderId: number): Observable<ArrayBuffer> {
    // The Content-Type is not 'application/json' for this request, as we're not sending JSON.
    const headers = this.getAuthHeaders('image/png').set('Accept', 'image/png');
    
    // Make an HTTP GET request to the Spring Boot endpoint.
    // The responseType is set to 'arraybuffer' to handle the binary image data.
    return this.http.get(`${this.baseUrl}/generate-qr/${orderId}`, {
      headers,
      responseType: 'arraybuffer'
    });
  }

  // ==================== Data Retrieval APIs ====================

  /**
   * Retrieves a single payment by its ID.
   * @param paymentId The ID of the payment.
   * @returns An observable of the PaymentDTO.
   */
  getPaymentById(paymentId: number): Observable<PaymentDTO> {
    const headers = this.getAuthHeaders();
    return this.http.get<PaymentDTO>(`${this.baseUrl}/${paymentId}`, {
      headers,
    });
  }

  /**
   * Retrieves all payments (typically for an admin user).
   * @returns An observable of a list of all PaymentDTOs.
   */
  getAllPayments(): Observable<PaymentDTO[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<PaymentDTO[]>(this.baseUrl, { headers });
  }

  /**
   * Retrieves all payments for a specific order.
   * @param orderId The ID of the order.
   * @returns An observable of a list of PaymentDTOs for the given order.
   */
  getPaymentsByOrderId(orderId: number): Observable<PaymentDTO[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<PaymentDTO[]>(`${this.baseUrl}/order/${orderId}`, {
      headers,
    });
  }
}