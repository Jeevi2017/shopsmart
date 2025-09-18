import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HomeComponent,          // standalone component
        HttpClientTestingModule // for HTTP mocks
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Simple featured product fetch test
  it('should fetch featured products', () => {
    const mockProducts = [
      { id: 1, name: 'Single Product', price: 1000, createdAt: new Date() }
    ];

    component.fetchFeaturedProducts();

    const req = httpMock.expectOne('http://localhost:8080/api/products');
    expect(req.request.method).toBe('GET');
    req.flush(mockProducts);

    expect(component.featuredProducts.length).toBe(1);
    expect(component.featuredProducts[0].name).toBe('Single Product');
  });

  // Test: search with empty query
  it('should not search if query is empty', () => {
    component.searchQuery = '';
    component.onSearch();

    expect(component.searchResults.length).toBe(0);
    httpMock.expectNone('http://localhost:9200/products/_search?q=');
  });

  // Test: search with valid query
  it('should search products with query', () => {
    component.searchQuery = 'laptop';
    component.onSearch();

    const req = httpMock.expectOne('http://localhost:9200/products/_search?q=laptop');
    expect(req.request.method).toBe('GET');

    // Mock response as ProductDTO[]
    req.flush([
      { id: 1, name: 'Laptop Pro', price: 45000, createdAt: new Date() },
      { id: 2, name: 'Laptop Air', price: 60000, createdAt: new Date() }
    ]);

    expect(component.searchResults.length).toBe(2);
    expect(component.searchResults[0].name).toBe('Laptop Pro');
    expect(component.searchResults[1].name).toBe('Laptop Air');
  });

  // Test: handle search error
  it('should handle error while searching products', () => {
    component.searchQuery = 'phone';
    component.onSearch();

    const req = httpMock.expectOne('http://localhost:9200/products/_search?q=phone');
    req.flush({ message: 'Elasticsearch Down' }, { status: 500, statusText: 'Internal Server Error' });

    expect(component.searchError).toBe('Search failed: Elasticsearch Down');
    expect(component.loadingSearch).toBeFalse();
    expect(component.searchResults.length).toBe(0);
  });
});
