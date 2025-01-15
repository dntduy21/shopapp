import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OnInit } from '@angular/core';
import { InsertProductDTO } from '../../../../dtos/product/insert.product.dto';
import { Category } from '../../../../models/category';
import { CategoryService } from '../../../../services/category.service';
import { ProductService } from '../../../../services/product.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-insert.product.admin',
  templateUrl: './insert.product.admin.component.html',
  styleUrls: ['./insert.product.admin.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ]
})
export class InsertProductAdminComponent implements OnInit {
  insertProductDTO: InsertProductDTO = {
    name: '',
    price: 0,
    description: '',
    quantity: 0,
    numberImg: 0,
    category_id: 1,
    images: []
  };
  categories: Category[] = []; // Dữ liệu động từ categoryService
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private categoryService: CategoryService,
    private productService: ProductService,
  ) {

  }
  ngOnInit() {
    this.getCategories(1, 100)
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

  onFileChange(event: any) {
    // Retrieve selected files from input element
    const files = event.target.files;
    // Limit the number of selected files to 5
    if (files.length > 5) {
      alert('Please select a maximum of 5 images.');
      return;
    }
    // Store the selected files in the newProduct object
    this.insertProductDTO.images = Array.from(files);
  }

  

  insertProduct() {
    if(this.insertProductDTO.images.length == 0){
      alert("Please select 1 image!");
      return;
    }
    if(this.insertProductDTO.numberImg < 0 || this.insertProductDTO.numberImg > this.insertProductDTO.images.length - 1){
      alert("Please choose 1 image among the images as a thumbnail!");
      return;
    }
    this.productService.insertProduct(this.insertProductDTO).subscribe({
      next: (response) => {
        if (this.insertProductDTO.images.length > 0) {
          const productId = response.id; // Assuming response contains the newly created product ID
          this.productService.uploadImages(productId, this.insertProductDTO.images, this.insertProductDTO.numberImg).subscribe({
            next: (imageResponse) => {
              console.log('Images uploaded successfully:', imageResponse);
              this.router.navigate(['../'], { relativeTo: this.route });
            },
            error: (error) => {
              console.error('Error uploading images:', error);
              alert('Failed to upload images.');
            },
          });
        } else {
          this.router.navigate(['../'], { relativeTo: this.route });
        }
      },
      error: (error) => {
        console.error('Error inserting product:', error);
        alert('Failed to insert product.');
      },
    });
  }
  
}
