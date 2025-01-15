import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { Product } from '../../../../models/product';
import { Category } from '../../../../models/category';
import { ProductService } from '../../../../services/product.service';
import { CategoryService } from '../../../../services/category.service';
import { environment } from '../../../../../environments/environment';
import { ProductImage } from '../../../../models/product.image';
import { UpdateProductDTO } from '../../../../dtos/product/update.product.dto';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-detail.product.admin',
  templateUrl: './update.product.admin.component.html',
  styleUrls: ['./update.product.admin.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ]
})

export class UpdateProductAdminComponent implements OnInit {
  productId: number;
  product: Product;
  updatedProduct: Product;
  categories: Category[] = []; // Dữ liệu động từ categoryService
  currentImageIndex: number = 0;
  images: File[] = [];

  constructor(
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router,
    private categoryService: CategoryService,
    private location: Location,
  ) {
    this.productId = 0;
    this.product = {} as Product;
    this.updatedProduct = {} as Product;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.productId = Number(params.get('id'));
      this.getProductDetails();
    });
    this.getCategories(1, 100);
  }
  getCategories(page: number, limit: number) {
    this.categoryService.getCategories(page, limit).subscribe({
      next: (categories: Category[]) => {
        debugger
        this.categories = categories;
      },
      complete: () => {
        debugger;
      },
      error: (error: any) => {
        console.error('Error fetching categories:', error);
      }
    });
  }
  getProductDetails(): void {
    this.productService.getDetailProduct(this.productId).subscribe({
      next: (product: Product) => {
        this.product = product;
        this.updatedProduct = { ...product };
        this.updatedProduct.product_images.forEach((product_image: ProductImage) => {
          product_image.image_url = `${environment.apiBaseUrl}/products/images/${product_image.image_url}`;
        });
      },
      complete: () => {

      },
      error: (error: any) => {

      }
    });
  }
  updateProduct() {
    // Implement your update logic here
    const updateProductDTO: UpdateProductDTO = {
      name: this.updatedProduct.name,
      price: this.updatedProduct.price,
      quantity: this.updatedProduct.quantity,
      description: this.updatedProduct.description,
      category_id: this.updatedProduct.category_id
    };
    this.productService.updateProduct(this.product.id, updateProductDTO).subscribe({
      next: (response: any) => {
        debugger
      },
      complete: () => {
        debugger;
        this.router.navigate(['/admin/products']);
      },
      error: (error: any) => {
        debugger;
        console.error('Error fetching products:', error);
      }
    });
  }
  showImage(index: number): void {
    debugger
    if (this.product && this.product.product_images &&
      this.product.product_images.length > 0) {
      // Đảm bảo index nằm trong khoảng hợp lệ        
      if (index < 0) {
        index = 0;
      } else if (index >= this.product.product_images.length) {
        index = this.product.product_images.length - 1;
      }
      // Gán index hiện tại và cập nhật ảnh hiển thị
      this.currentImageIndex = index;
    }
  }
  thumbnailClick(index: number) {
    debugger
    // Gọi khi một thumbnail được bấm
    this.currentImageIndex = index; // Cập nhật currentImageIndex
    this.productService.updateThumbnail(index, this.product.id).subscribe({
      next: (imageResponse:any) => {
        alert("Update thumbnail success!");
      },
      error: (error:any) => {
        // Handle the error while uploading images
        alert(error.error)
        console.error('Error uploading thumbnail:', error);
      }
    })
  }
  nextImage(): void {
    debugger
    this.showImage(this.currentImageIndex + 1);
  }

  previousImage(): void {
    debugger
    this.showImage(this.currentImageIndex - 1);
  }
  onFileChange(event: any) {
    // Retrieve selected files from input element
    const files = event.target.files;
    // Limit the number of selected files to 5
    const lengthImg:number = files.length + this.product.product_images.length;
    if (lengthImg > 5) {
      alert('Please select a maximum of 5 images.');
      return;
    }
    
    // Store the selected files in the newProduct object
    this.images = Array.from(files);
    if(!this.updatedProduct.numberImg)
      this.updatedProduct.numberImg = 0;
    //index img ko nam trong so img hien co
    if(this.updatedProduct.numberImg! < 0 || this.updatedProduct.numberImg! > lengthImg - 1){
      alert("Please choose 1 image among the images as a thumbnail!");
      return;
    }

    if(this.updatedProduct.numberImg < this.product.product_images.length){
      this.updatedProduct.numberImg1 = this.updatedProduct.numberImg;
      this.updatedProduct.numberImg = -1;
      this.productService.uploadImages1(this.productId, this.images, this.updatedProduct.numberImg!, this.updatedProduct.numberImg1!).subscribe({
        next: (imageResponse:any) => {
          debugger
          // Handle the uploaded images response if needed              
          console.log('Images uploaded successfully:', imageResponse);
          this.images = [];
          // Reload product details to reflect the new images
          this.getProductDetails();
        },
        error: (error:any) => {
          // Handle the error while uploading images
          // alert(error.error)
          this.images = [];
          // Reload product details to reflect the new images
          this.getProductDetails();
          console.error('Error uploading images:', error);
        }
      })
    } else {
      this.updatedProduct.numberImg = this.updatedProduct.numberImg - this.product.product_images.length + 1;
      this.productService.uploadImages(this.productId, this.images, this.updatedProduct.numberImg!).subscribe({
        next: (imageResponse) => {
          debugger
          // Handle the uploaded images response if needed              
          console.log('Images uploaded successfully:', imageResponse);
          this.images = [];
          // Reload product details to reflect the new images
          this.getProductDetails();
        },
        error: (error) => {
          // Handle the error while uploading images
          alert(error.error)
          console.error('Error uploading images:', error);
        }
      })
    }
    
  }
  productImages: ProductImage[] = [];

  deleteImage(productImage: ProductImage) {
    if (confirm('Are you sure you want to remove this image?')) {
      this.productService.deleteProductImage(productImage.id).subscribe({
        next: (response: string) => {
          // Handle the success response
          alert(response); // Hiển thị thông báo thành công từ API
          this.productImages = this.productImages.filter(
            (img) => img.id !== productImage.id
          ); // Cập nhật danh sách hình ảnh
        },
        error: (error) => {
          // Handle the error
          alert(error.error || 'Error occurred while deleting the image.');
          console.error('Error deleting image:', error);
        },
      });
    }
  }
  
}
