import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { PaymentService } from '../services/payment.service';

@Component({
  selector: 'app-qr-code-display',
  templateUrl: './qr-code-display.component.html',
  styleUrls: ['./qr-code-display.component.css']
})
export class QrCodeDisplayComponent implements OnInit {

  qrCodeImageUrl: SafeUrl | undefined;
  errorMessage: string | undefined;

  constructor(
    private paymentService: PaymentService,
    private sanitizer: DomSanitizer,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Get the orderId from the URL route parameter
    const orderId = this.route.snapshot.paramMap.get('orderId');
    if (orderId) {
      this.fetchQrCode(+orderId);
    } else {
      this.errorMessage = "Order ID not found.";
    }
  }

  /**
   * Fetches the QR code from the backend and displays it.
   * @param orderId The ID of the order for which to generate the QR code.
   */
  fetchQrCode(orderId: number): void {
    this.paymentService.getQrCode(orderId).subscribe(
      (imageData: ArrayBuffer) => {
        // Convert the binary ArrayBuffer to a Blob (a file-like object)
        const blob = new Blob([imageData], { type: 'image/png' });
        // Create a temporary URL for the Blob
        const objectUrl = URL.createObjectURL(blob);
        // Sanitize the URL to bypass Angular's security and allow it in the template
        this.qrCodeImageUrl = this.sanitizer.bypassSecurityTrustUrl(objectUrl);
      },
      error => {
        console.error('Error fetching QR code:', error);
        this.errorMessage = "Could not generate QR code. Please try again.";
      }
    );
  }
}