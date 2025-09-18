import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { HttpClientModule, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AuthService } from '../../../services/auth.service';
import { ProductService } from '../../../services/product.service';
import { ProductDTO } from '../../../models/product.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterOutlet,
    HttpClientModule,
    FormsModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  username: string | null = null;

  featuredProducts: ProductDTO[] = [];
  searchResults: ProductDTO[] = [];
  searchQuery: string = '';

  loadingProducts = true;
  loadingSearch = false;
  productsError: string | null = null;
  searchError: string | null = null;

  private authService = inject(AuthService);
  private productService = inject(ProductService);
  private router = inject(Router);

  ngOnInit(): void {
    this.username = this.authService.getCurrentUsername();
    this.fetchFeaturedProducts();
  }

  /**
   * Business Logic: Fetches all products, sorts them by ID in descending order
   * to find the latest products, and displays the top 4 as "featured".
   */
  fetchFeaturedProducts(): void {
    this.loadingProducts = true;
    this.productsError = null;

    this.productService.getAllProducts().subscribe({
      next: (products: ProductDTO[]) => {
        // Corrected line: Sort by ID descending (latest first) and take top 4
        this.featuredProducts = products
          .sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
          .slice(0, 4);
        this.loadingProducts = false;
      },
      error: (error: HttpErrorResponse) => {
        this.productsError = 'Failed to load products. Please try again later.';
        this.loadingProducts = false;
        console.error('Error fetching featured products:', error);
        if (error.error?.message) {
          this.productsError = `Failed to load products: ${error.error.message}`;
        }
      }
    });
  }

  /**
   * Business Logic: Handles the search functionality. If the search query is empty,
   * it clears the search results. Otherwise, it calls the ProductService to
   * search for products using the backend's Elasticsearch endpoint.
   */
  onSearch(): void {
    if (!this.searchQuery || this.searchQuery.trim() === '') {
      this.searchResults = [];
      return;
    }

    this.loadingSearch = true;
    this.searchError = null;

    this.productService.searchProducts(this.searchQuery).subscribe({
      next: (results: ProductDTO[]) => {
        this.searchResults = results;
        this.loadingSearch = false;
      },
      error: (error: HttpErrorResponse) => {
        this.searchError = 'Search failed. Please try again later.';
        this.loadingSearch = false;
        console.error('Error searching products:', error);
        if (error.error?.message) {
          this.searchError = `Search failed: ${error.error.message}`;
        }
      }
    });
  }

  /**
   * Business Logic: Navigates to the product details page. It first checks
   * if the product ID is valid before routing.
   */
  goToProduct(productId: number | undefined): void {
    if (productId !== undefined) {
      this.router.navigate(['/home/products', productId]);
    } else {
      console.warn('Cannot view product details: Product ID is undefined.');
    }
  }

  /**
   * Business Logic: Logs the user out by calling the AuthService and
   * then redirects them to the login page.
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}