import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrdersComponent } from './orders.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { OrderService } from '../../../services/order.service';
import { AuthService } from '../../../services/auth.service';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { OrderDTO } from '../../../models/order-models';

describe('OrdersComponent', () => {
  let component: OrdersComponent;
  let fixture: ComponentFixture<OrdersComponent>;
  let mockOrderService: jasmine.SpyObj<OrderService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    mockOrderService = jasmine.createSpyObj('OrderService', [
      'getOrdersByCustomerId',
      'generateOrderReportPdf'
    ]);
    mockAuthService = jasmine.createSpyObj('AuthService', ['getCurrentUserId']);

    await TestBed.configureTestingModule({
      declarations: [OrdersComponent],
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        { provide: OrderService, useValue: mockOrderService },
        { provide: AuthService, useValue: mockAuthService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    mockAuthService.getCurrentUserId.and.returnValue(1);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load orders successfully', () => {
    const dummyOrders: OrderDTO[] = [
      {
        id: 1,
        customerId: 1,
        orderDate: '2025-09-23T12:00:00Z', // <-- Use string instead of Date
        status: 'PENDING',
        totalAmount: 500,
        shippingAddress: 'Test Address',
        orderItems: []
      }
    ];

    mockAuthService.getCurrentUserId.and.returnValue(1);
    mockOrderService.getOrdersByCustomerId.and.returnValue(of(dummyOrders));

    component.loadOrders();

    expect(mockOrderService.getOrdersByCustomerId).toHaveBeenCalledWith(1);
    expect(component.orders).toEqual(dummyOrders);
    expect(component.loadingOrders).toBeFalse();
    expect(component.ordersError).toBeNull();
  });

  it('should handle order loading error', () => {
    const error = { status: 500, error: { message: 'Server error' } };
    mockAuthService.getCurrentUserId.and.returnValue(1);
    mockOrderService.getOrdersByCustomerId.and.returnValue(throwError(() => error));

    component.loadOrders();

    expect(component.ordersError).toBe('Failed to load orders: Server error');
    expect(component.loadingOrders).toBeFalse();
  });

  it('should handle no customer ID', () => {
    mockAuthService.getCurrentUserId.and.returnValue(null);
    spyOn(component['router'], 'navigate');

    component.loadOrders();

    expect(component.ordersError).toBe('Customer ID not available. Please log in.');
    expect(component.loadingOrders).toBeFalse();
    expect(component['router'].navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should download PDF report successfully', () => {
    const dummyBlob = new Blob(['PDF content'], { type: 'application/pdf' });
    mockOrderService.generateOrderReportPdf.and.returnValue(of(dummyBlob));
    const spyCreateElement = spyOn(document, 'createElement').and.callThrough();

    component.downloadPdfReport(1);

    expect(mockOrderService.generateOrderReportPdf).toHaveBeenCalledWith(1);
    expect(spyCreateElement).toHaveBeenCalledWith('a');
  });

  it('should handle PDF download error', () => {
    const error = { status: 500 };
    mockOrderService.generateOrderReportPdf.and.returnValue(throwError(() => error));
    mockAuthService.getCurrentUserId.and.returnValue(1);

    component.downloadPdfReport(1);

    expect(component.ordersError).toBe('Failed to download PDF. Please try again.');
  });
});
