import { ProductImage } from "./product.image";
export interface Product {
  id: number;
  name: string;
  price: number;
  thumbnail: string;
  description: string;
  category_id: number;
  url: string;
  quantity: number;
  visible: boolean;
  product_images: ProductImage[];
  numberImg?: number;
  numberImg1?: number;
}


