import { Component, inject } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { OrderDTO } from '../../../models/order-models';
import { OrderService } from '../../../services/order.service';
import { PaymentService } from '../../../services/payment.service';
import { AuthService } from '../../../services/auth.service';
import { RazorpayOrderResponseDTO } from '../../../models/razorpay-models';
import { PaymentDTO } from '../../../models/payment-models';

declare var Razorpay: any;

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule, DecimalPipe],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css'],
})
export class PaymentComponent {
  orderId: number | null = null;
  order: OrderDTO | null = null;

  loadingOrder = true;
  orderError: string | null = null;
  selectedPaymentMethod: string = 'RAZORPAY';
  processingPayment = false;
  paymentSuccessMessage: string | null = null;
  paymentErrorMessage: string | null = null;

  qrCodeUrl: string | null = null;
  loadingQrCode = false;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private paymentService = inject(PaymentService);
  private authService = inject(AuthService);

  constructor() {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('orderId');
      if (id) {
        this.orderId = +id;
        this.loadOrderDetails(this.orderId);
      } else {
        this.orderError = 'Order ID not provided. Cannot proceed to payment.';
        this.loadingOrder = false;
        console.error('PaymentComponent: No orderId found in route params.');
        this.router.navigate(['/cart']);
      }
    });
  }

  /**
   * Load order details from backend
   */
  loadOrderDetails(orderId: number): void {
    this.loadingOrder = true;
    this.orderError = null;

    this.orderService.getOrderById(orderId).subscribe({
      next: (data: OrderDTO) => {
        this.order = data;
        this.loadingOrder = false;
        console.log('PaymentComponent: Order loaded:', this.order);

        const currentCustomerId = this.authService.getCurrentUserId();
        const userRoles = this.authService.getUserRoles();
        const isAdmin = userRoles.includes('ROLE_ADMIN');

        if (this.order.customerId !== currentCustomerId && !isAdmin) {
          this.orderError = 'You do not have permission to view this order.';
          this.order = null;
          this.router.navigate(['/orders']);
        } else if (this.order.status === 'PAID') {
          this.paymentSuccessMessage = 'This order has already been paid.';
          this.processingPayment = false;
        } else {
          // Generate QR code automatically if order is ready to be paid
          this.generateQRCode();
        }
      },
      error: (error: HttpErrorResponse) => {
        this.loadingOrder = false;
        if (error.status === 404) {
          this.orderError = 'Order not found for the provided ID.';
        } else if (error.status === 403) {
          this.orderError = 'Access denied to this order.';
        } else {
          this.orderError = error.error?.message || 'Failed to load order.';
        }
        console.error('PaymentComponent: Error fetching order:', error);
      },
    });
  }

  /**
   * Calculate subtotal of order items
   */
  getOrderSubtotal(): number {
    return this.order?.orderItems?.reduce(
      (acc, item) => acc + item.quantity * item.price,
      0
    ) ?? 0;
  }

  /**
   * Process payment
   */
  processPayment(): void {
    this.processingPayment = true;
    this.paymentSuccessMessage = null;
    this.paymentErrorMessage = null;

    if (!this.orderId || !this.order || !this.order.totalAmount) {
      this.paymentErrorMessage =
        'Order details are missing. Cannot process payment.';
      this.processingPayment = false;
      return;
    }

    if (this.selectedPaymentMethod === 'RAZORPAY') {
      this.initiateRazorpayPayment();
    } else {
      this.paymentErrorMessage =
        'Only Razorpay is supported currently. Please select Razorpay.';
      this.processingPayment = false;
    }
  }

  /**
   * Create order on Razorpay
   */
  private initiateRazorpayPayment(): void {
    if (!this.order?.id || !this.order?.totalAmount) {
      this.paymentErrorMessage =
        'Order details incomplete for Razorpay payment.';
      this.processingPayment = false;
      return;
    }

    this.paymentService
      .createRazorpayOrder(
        this.order.id,
        this.order.totalAmount,
        'INR',
        `order_rcptid_${this.order.id}`
      )
      .subscribe({
        next: (razorpayOrder: RazorpayOrderResponseDTO) => {
          console.log('PaymentComponent: Razorpay order created:', razorpayOrder);
          this.openRazorpayCheckout(razorpayOrder);
        },
        error: (error: HttpErrorResponse) => {
          this.paymentErrorMessage =
            'Failed to create Razorpay order. Please try again.';
          this.processingPayment = false;
          console.error('PaymentComponent: Razorpay order error:', error);
        },
      });
  }

  /**
   * Open Razorpay checkout
   */
  private openRazorpayCheckout(razorpayOrder: RazorpayOrderResponseDTO): void {
    if (typeof Razorpay === 'undefined') {
      this.paymentErrorMessage =
        'Razorpay SDK not loaded. Check your internet connection.';
      this.processingPayment = false;
      console.error('PaymentComponent: Razorpay SDK missing.');
      return;
    }

    const customerName = this.authService.getCurrentUsername() || 'Customer';
    const customerEmail = this.authService.getCurrentUserEmail() || '';

    const options = {
      key: 'rzp_test_RIDbIkMuMUis8Y',
      amount: razorpayOrder.amount,
      currency: razorpayOrder.currency,
      name: 'E-commerce App',
      description: `Payment for Order #${this.order?.id}`,
      image: 'https://placehold.co/100x100/007bff/ffffff?text=E',
      order_id: razorpayOrder.id,
      handler: (response: any) => {
        this.captureRazorpayPayment(response);
      },
      prefill: { name: customerName, email: customerEmail, contact: '' },
      notes: { internal_order_id: this.order?.id },
      theme: { color: '#3399FF' },
    };

    const rzp = new Razorpay(options);

    rzp.on('payment.failed', (response: any) => {
      this.paymentErrorMessage = `Payment failed: ${
        response.error.description || 'Unknown error'
      }`;
      this.processingPayment = false;
      console.error('PaymentComponent: Razorpay failed:', response);
    });

    rzp.open();
  }

  /**
   * Capture payment on backend
   */
  private captureRazorpayPayment(response: any): void {
    if (!this.orderId || !this.order?.totalAmount) {
      this.paymentErrorMessage =
        'Order details missing for payment capture.';
      this.processingPayment = false;
      return;
    }

    this.paymentService
      .captureRazorpayPayment({
        razorpayPaymentId: response.razorpay_payment_id,
        razorpayOrderId: response.razorpay_order_id,
        razorpaySignature: response.razorpay_signature,
        amount: this.order.totalAmount,
        internalOrderId: this.orderId,
      })
      .subscribe({
        next: (payment: PaymentDTO) => {
          this.paymentSuccessMessage = `Payment successful! Order ${payment.orderId} confirmed.`;
          this.processingPayment = false;
          console.log('PaymentComponent: Payment captured:', payment);

          setTimeout(() => {
            this.router.navigate(['/home/orders', this.orderId]);
          }, 3000);
        },
        error: (error: HttpErrorResponse) => {
          this.paymentErrorMessage =
            'Payment verification failed. Please contact support.';
          this.processingPayment = false;
          console.error('PaymentComponent: Capture error:', error);
        },
      });
  }

  /**
   * Generate QR Code URL from backend API
   */
  generateQRCode(): void {
    if (!this.order?.id) {
      this.qrCodeUrl = null;
      return;
    }
    
    this.qrCodeUrl = null;
    this.loadingQrCode = true;
    this.paymentErrorMessage = null;

    this.paymentService.getQrCode(this.order.id).subscribe({
      next: (data: ArrayBuffer) => {
        console.log('PaymentComponent: QR code fetched from backend.');
        const blob = new Blob([data], { type: 'image/png' });
        this.qrCodeUrl = URL.createObjectURL(blob);
        this.loadingQrCode = false;
      },
      error: (error: HttpErrorResponse) => {
        this.paymentErrorMessage = `Failed to generate QR code: ${error.error?.message || 'Unknown error'}`;
        this.loadingQrCode = false;
        console.error('PaymentComponent: QR code error:', error);
      },
    });
  }
}
