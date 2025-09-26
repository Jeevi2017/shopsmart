import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';
import { OrderService } from '../../../services/order.service';
import { OrderDTO } from '../../../models/order-models';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterLink, HttpClientModule, DecimalPipe],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css'],
})
export class OrdersComponent implements OnInit {
  orders: OrderDTO[] = [];
  loadingOrders = true;
  ordersError: string | null = null;
  downloadingPdfOrderId: number | null = null; // track downloading state

  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor() {}

  ngOnInit(): void {
    this.loadOrders();
  }

  /** Load all orders for the currently logged-in customer */
  loadOrders(): void {
    this.loadingOrders = true;
    this.ordersError = null;
    const customerId = this.authService.getCurrentUserId();

    if (customerId) {
      this.orderService.getOrdersByCustomerId(customerId).subscribe({
        next: (data: OrderDTO[]) => {
          this.orders = data;
          this.loadingOrders = false;
        },
        error: (error: HttpErrorResponse) => {
          this.loadingOrders = false;
          this.ordersError = error.status === 404 
            ? 'No orders found for this customer.' 
            : 'Failed to load orders. Please try again.';
          console.error('OrdersComponent: Error fetching orders:', error);
        },
      });
    } else {
      this.ordersError = 'Customer ID not available. Please log in.';
      this.loadingOrders = false;
      console.warn('OrdersComponent: No customer ID found from AuthService.');
      this.router.navigate(['/login']);
    }
  }

  /** Navigate to detailed order view */
  viewOrderDetails(orderId: number | undefined): void {
    if (orderId !== undefined) {
      this.router.navigate(['/home/orders', orderId]);
    } else {
      console.warn('Cannot view order details: Order ID is undefined.');
    }
  }

  /** Download PDF report for a specific order */
  downloadPdfReport(orderId: number | undefined): void {
    if (orderId === undefined) {
      console.warn('Cannot download PDF: Order ID is undefined.');
      return;
    }

    this.downloadingPdfOrderId = orderId;
    this.orderService.generateOrderReportPdf(orderId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `order_report_${orderId}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.downloadingPdfOrderId = null;
      },
      error: (error: HttpErrorResponse) => {
        console.error('OrdersComponent: Error downloading PDF:', error);
        this.ordersError = 'Failed to download PDF. Please try again.';
        this.downloadingPdfOrderId = null;
      },
    });
  }
}
