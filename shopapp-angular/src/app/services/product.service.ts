import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product } from '../models/product';
import { UpdateProductDTO } from '../dtos/product/update.product.dto';
import { InsertProductDTO } from '../dtos/product/insert.product.dto';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  // Fetch products with filters
  getProducts(
    keyword: string,
    categoryId: number,
    page: number,
    limit: number,
    visible?: boolean
  ): Observable<Product[]> {
    const params: HttpParams = new HttpParams()
      .set('keyword', keyword)
      .set('category_id', categoryId.toString())
      .set('page', page.toString())
      .set('limit', limit.toString())
      .set('visible', visible?.toString() || '');

    return this.http.get<Product[]>(`${this.apiBaseUrl}/products`, { params });
  }

  // Update product visibility
  updateVisibility(productId: number, visible: boolean): Observable<Product> {
    return this.http.patch<Product>(`${this.apiBaseUrl}/products/${productId}/visibility`, null, {
      params: { visible: visible.toString() }
    });
  }

  // Fetch product details by ID
  getDetailProduct(productId: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiBaseUrl}/products/${productId}`);
  }

  // Fetch products by a list of IDs
  getProductsByIds(productIds: number[]): Observable<Product[]> {
    const params: HttpParams = new HttpParams().set('ids', productIds.join(','));
    return this.http.get<Product[]>(`${this.apiBaseUrl}/products/by-ids`, { params });
  }

  // Delete a product by ID
  deleteProduct(productId: number): Observable<string> {
    return this.http.delete<string>(`${this.apiBaseUrl}/products/${productId}`);
  }

  // Update a product
  updateProduct(productId: number, updatedProduct: UpdateProductDTO): Observable<Product> {
    return this.http.put<Product>(`${this.apiBaseUrl}/products/${productId}`, updatedProduct);
  }

  // Insert a new product
  insertProduct(insertProductDTO: InsertProductDTO): Observable<Product> {
    return this.http.post<Product>(`${this.apiBaseUrl}/products`, insertProductDTO);
  }

  //update thumbnail
  updateThumbnail(index: number, idSP: number){
    return this.http.post(`${this.apiBaseUrl}/products/updateThumbnail/${index}/${idSP}`, null);
  }

  uploadImages(productId: number, images: File[], numberImg: number) {
    
    const formData = new FormData();
    images.forEach((file) => {
      formData.append('files', file); // Thêm từng ảnh vào FormData
      formData.append('numberImg', numberImg.toString());
    });
  
    return this.http.post(`${this.apiBaseUrl}/products/uploads/${productId}`, formData);
  }

  uploadImages1(productId: number, images: File[], numberImg: number, numberImg1: number) {
  
    const formData = new FormData();
    images.forEach((file) => {
      formData.append('files', file); // Thêm từng ảnh vào FormData
      formData.append('numberImg', numberImg.toString());
    });
  
    return this.http.post(`${this.apiBaseUrl}/products/uploads/${productId}`, formData);
  }
  

  // Delete a product image by ID
  deleteProductImage(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiBaseUrl}/product_images/${id}`);
  }
}
