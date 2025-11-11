import {useCartContext} from "../../context/CartContext";
import {ShoppingCart} from "./ShoppingCart";

export const ShoppingCartWrapper = () => {
  const {isCartOpen, closeCart} = useCartContext();

  return <ShoppingCart isOpen={isCartOpen} onClose={closeCart}/>;
};
